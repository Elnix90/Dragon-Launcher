package org.elnix.dragonlauncher.applications

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.elnix.dragonlauncher.StringNormalizer
import org.elnix.dragonlauncher.appoverrides.AppOverridesManager
import org.elnix.dragonlauncher.base.model.enumsui.select.WorkspaceViewMode
import org.elnix.dragonlauncher.base.model.models.AppCategory.Companion.mapAppToSection
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.models.Application.Companion.getPackageVersionName
import org.elnix.dragonlauncher.base.model.models.LauncherApp
import org.elnix.dragonlauncher.base.model.models.ResultScore
import org.elnix.dragonlauncher.base.model.models.SystemApp
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.base.model.serializables.Workspace
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType.All
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType.Custom
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType.Private
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType.System
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType.User
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType.Work
import org.elnix.dragonlauncher.compat.PackageManagerCompat
import org.elnix.dragonlauncher.profiles.ProfileManager
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.workspaces.WorkspacesManager

public interface AppRepository {
    public fun getAllApps(): Flow<ImmutableList<Application>>
    public fun search(
        query: String,
        workspace: Workspace,
        workspaceViewMode: WorkspaceViewMode
    ): Flow<ImmutableList<Application>>

    public suspend fun refreshApps()


    /**
     * Finds an app from a [Action.LaunchApp].
     *
     * The package name and the profile are compared.
     * The profile if passed through [ProfileManager.resolveProfile] in order to correct any profile serialization issues
     * After correction, both package name and profile must match exactly
     *
     * @param action the requested action to convert to application
     * @return A flow of the nullable app
     */
    public fun findOne(action: Action.LaunchApp): Flow<Application?>

    /**
     * Retrieve the actual app from a [Action.LaunchApp].
     * Same function as findOne but returns an instant [Application] not a [Flow]
     * @see findOne
     *
     * @param action the requested action to convert to application
     * @return the found corresponding application or null if none matches     *
     */
    public suspend fun fromAction(action: Action.LaunchApp): Application?

    public fun queryAppShortcuts(packageName: String): List<ShortcutInfo>
    public fun loadShortcutIcon(packageName: String, shortcutId: String, sizePx: Int = 48): Bitmap?
}

internal class AppRepositoryImpl(
    private val ctx: Context,
    private val profileManager: ProfileManager,
    private val packageManagerCompat: PackageManagerCompat,
    private val appOverridesManager: AppOverridesManager,
    private val workspacesManager: WorkspacesManager,
    private val stringNormalizer: StringNormalizer,
) : AppRepository {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val launcherApps =
        ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    private val installedApps = MutableStateFlow<List<Application>>(emptyList())
    private val launchableApps = MutableStateFlow<List<Application>>(emptyList())
    private val systemApps = MutableStateFlow<List<Application>>(emptyList())

    private val profiles = profileManager.activeProfiles

    private val mutex = Mutex()

    init {
        launcherApps.registerCallback(object : LauncherApps.Callback() {

            override fun onShortcutsChanged(packageName: String, shortcuts: MutableList<ShortcutInfo>, user: UserHandle) {
                scope.launch { refreshApps() }
            }

            override fun onPackagesSuspended(packageNames: Array<out String>?, user: UserHandle?) {
                scope.launch { refreshApps() }
            }

            override fun onPackagesUnsuspended(packageNames: Array<out String>?, user: UserHandle?) {
                scope.launch { refreshApps() }
            }

            override fun onPackageAdded(packageName: String, user: UserHandle) {
                scope.launch { refreshApps() }
            }

            override fun onPackageRemoved(packageName: String, user: UserHandle) {
                scope.launch { refreshApps() }
            }

            override fun onPackageChanged(packageName: String, user: UserHandle) {
                scope.launch { refreshApps() }
            }

            override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
                scope.launch { refreshApps() }
            }

            override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
                scope.launch { refreshApps() }
            }
        }, Handler(Looper.getMainLooper()))

        // Initial load
        scope.launch {
            refreshApps()
        }

        // Listen for profile changes
        scope.launch {
            profiles.runningFold<List<Profile>, Pair<List<Profile>?, List<Profile>?>>(null to null) { acc, value ->
                acc.second to value
            }.collectLatest { (prev, curr) ->
                if (curr != null) {
                    refreshApps()
                }
            }
        }
    }


    override suspend fun refreshApps() {
        mutex.withLock {
            val allApps = mutableListOf<Application>()
            val launchable = mutableListOf<Application>()
            val system = mutableListOf<Application>()

            val launchableActivityInfos = packageManagerCompat.getLaunchableApps()
            val allApplicationInfos = packageManagerCompat.getAllApplications()

            val profileMap = profiles.first().associateBy { it.userHandle.hashCode() }

            launchableActivityInfos.forEach { activityInfo ->
                val app = createLauncherApp(activityInfo, profileMap)
                if (app != null) {
                    allApps.add(app)
                    launchable.add(app)
                }
            }

            allApplicationInfos.forEach { appInfo ->
                val isLaunchable = launchableActivityInfos.any {
                    it.componentName.packageName == appInfo.packageName
                }

                if (!isLaunchable && appInfo.packageName != ctx.packageName) {
                    val app = createSystemApp(appInfo, profileMap)
                    if (app != null) {
                        allApps.add(app)
                        system.add(app)
                    }
                }
            }

            installedApps.value = allApps
            launchableApps.value = launchable
            systemApps.value = system
        }
    }

    private fun createLauncherApp(
        activityInfo: LauncherActivityInfo,
        profileMap: Map<Int, Profile>,
    ): Application? {
        val applicationInfo = activityInfo.applicationInfo ?: return null

        try {
            activityInfo.componentName.packageName
        } catch (_: Exception) {
            return null
        }

        if (applicationInfo.packageName == ctx.packageName && !ctx.packageName.endsWith(".debug")) {
            return null
        }

        val profile = profileMap[activityInfo.user.hashCode()] ?: return null
        val versionName = getPackageVersionName(ctx, applicationInfo.packageName)
        val category = mapAppToSection(applicationInfo)

        return LauncherApp(
            launcherActivityInfo = activityInfo,
            versionName = versionName,
            profile = profile,
            category = category,
            isSuspended = activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SUSPENDED != 0,
        )
    }

    private fun createSystemApp(
        appInfo: ApplicationInfo,
        profileMap: Map<Int, Profile>,
    ): Application? {
        if (appInfo.packageName == ctx.packageName) return null

        val profile = profileMap.values.firstOrNull() ?: return null
        val versionName = getPackageVersionName(ctx, appInfo.packageName)

        return SystemApp(
            ctx = ctx,
            applicationInfo = appInfo,
            versionName = versionName,
            profile = profile,
            isSuspended = appInfo.flags and ApplicationInfo.FLAG_SUSPENDED != 0,
        )
    }


    override fun getAllApps(): Flow<ImmutableList<Application>> {
        return installedApps.map { it.toImmutableList() }.withCustomLabels(appOverridesManager)
    }


    override fun findOne(
        action: Action.LaunchApp
    ): Flow<Application?> {
        return getAllApps().map { apps ->
            // Resolve the stored profile to the live one, as the persisted userHandle
            // may have been serialized incorrectly (e.g. by older versions).
            val correctedActionProfile = profileManager.resolveProfile(action.profile)
            apps.firstOrNull { app ->
                app.packageName == action.packageName && app.profile == correctedActionProfile
            }
        }
    }

    override suspend fun fromAction(action: Action.LaunchApp): Application? = findOne(action).first()

    /**
     * Returns a filtered and sorted list of apps for the specified workspace as a reactive Flow.
     *
     * @param workspaces The target workspaces to search in
     * @param workspaceViewMode To pick what kind of apps are shown (All, Only Added or Only Removed), used in the Workspace details screen.
     * @return Flow of filtered, sorted, and resolved [Application] list
     *
     * @see WorkspaceType for base filtering behavior
     */
    private fun appsForWorkspaces(
        workspaces: Array<Workspace>,
        workspaceViewMode: WorkspaceViewMode
    ): Flow<List<Application>> = getAllApps().map { apps ->
        val allFinal = mutableSetOf<Application>()

        for (workspace in workspaces) {

            val appIds = workspace.appIds ?: emptySet()
            val removedAppIds = workspace.removedAppIds ?: emptySet()

            val workspaceFiltered = when (workspaceViewMode) {
                WorkspaceViewMode.Added -> apps.filter { it.key in appIds }
                WorkspaceViewMode.Removed -> apps.filter { it.key in removedAppIds }
                WorkspaceViewMode.Default -> {
                    val base = when (workspace.type) {
                        All -> apps
                        Custom -> emptyList()
                        User -> apps.filter { !it.isWork && !it.isPrivate && it.isLaunchable }
                        System -> apps.filter { it.isSystem }
                        Work -> apps.filter { it.isWork && it.isLaunchable }
                        Private -> {
                            apps.filter { it.isPrivate && it.isLaunchable }
                        }
                    }

                    val added = apps.filter { it.key in appIds }

                    // Use the base list, and add the filtered manually-added apps, then remove explicitly removed ones
                    (base + added)
                        .distinctBy { it.key }
                        .filter { it.key !in removedAppIds }
                        .sortedBy { it.label.lowercase() }
                }
            }
            allFinal.addAll(workspaceFiltered)
        }
        allFinal.toList()
    }

    private val searchAllWorkspacesOnlyWhenFirstCharIs = DrawerSettingsStore.searchAllWorkspacesOnlyWhenFirstCharIs.flow(ctx)
    private val disableAutoLaunchWhenFirstCharIs = DrawerSettingsStore.disableAutoLaunchWhenFirstCharIs.flow(ctx)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun search(
        query: String,
        workspace: Workspace,
        workspaceViewMode: WorkspaceViewMode
    ): Flow<ImmutableList<Application>> {


        return combine(
            searchAllWorkspacesOnlyWhenFirstCharIs,
            disableAutoLaunchWhenFirstCharIs,
            workspacesManager.workspaces.flow
        ) { workspaceFirstChar, disableFirstChar, workspaces ->

            val workspaces = if (workspaceFirstChar.isNotEmpty() && query.isNotEmpty() && workspaceFirstChar.first() == query.first()) {
                workspaces.filter { it.enabled }.toTypedArray()
            } else arrayOf(workspace)

            var normalizedQuery = stringNormalizer.normalize(query)

            if (workspaceFirstChar.isNotEmpty() && normalizedQuery.startsWith(workspaceFirstChar)) {
                normalizedQuery = normalizedQuery.drop(1)
            }

            if (disableFirstChar.isNotEmpty() && normalizedQuery.startsWith(disableFirstChar)) {
                normalizedQuery = normalizedQuery.drop(1)
            }

            workspaces to normalizedQuery

        }.flatMapLatest { pair ->
            val workspace = pair.first
            val normalizedQuery = pair.second

            appsForWorkspaces(workspace, workspaceViewMode).map { apps ->
                val normalizerId = stringNormalizer.id
                val appResults = mutableListOf<Application>()

                if (query.isEmpty()) {
                    appResults.addAll(apps)
                } else {
                    appResults.addAll(apps.mapNotNull { app ->
                        val cachedLabel = app.cachedNormalizerResult
                        val score = ResultScore.from(
                            query = normalizedQuery,
                            primaryFields = listOf(
                                if (cachedLabel?.first == normalizerId) {
                                    cachedLabel.second
                                } else {
                                    stringNormalizer.normalize(app.label).also {
                                        app.cachedNormalizerResult = normalizerId to it
                                    }
                                }
                            ),
                            secondaryFields = appOverridesManager.getAliasesForApp(app).first()
                        )
                        if (score.score < 0.8f) return@mapNotNull null
                        app
                    })
                }
                appResults.sort()
                appResults.toImmutableList()
            }.withCustomLabels(appOverridesManager)
        }
    }

    override fun queryAppShortcuts(packageName: String): List<ShortcutInfo> =
        packageManagerCompat.queryAppShortcuts(packageName)

    override fun loadShortcutIcon(packageName: String, shortcutId: String, sizePx: Int): Bitmap? =
        packageManagerCompat.loadShortcutIcon(packageName, shortcutId, sizePx)
}