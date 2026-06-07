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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.elnix.dragonlauncher.StringNormalizer
import org.elnix.dragonlauncher.appoverrides.AppOverridesManager
import org.elnix.dragonlauncher.base.model.models.AppCategory.Companion.mapAppToSection
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.models.Application.Companion.getPackageVersionName
import org.elnix.dragonlauncher.base.model.models.LauncherApp
import org.elnix.dragonlauncher.base.model.models.ResultScore
import org.elnix.dragonlauncher.base.model.models.SystemApp
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.base.model.serializables.Workspace
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType.ALL
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType.CUSTOM
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType.PRIVATE
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType.SYSTEM
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType.USER
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType.WORK
import org.elnix.dragonlauncher.compat.PackageManagerCompat
import org.elnix.dragonlauncher.profiles.ProfileManager

interface AppRepository {
    fun findOne(packageName: String, user: UserHandle): Flow<Application?>
    fun getAllApps(): Flow<ImmutableList<Application>>
    fun getLaunchableApps(): Flow<ImmutableList<Application>>
    fun getSystemApps(): Flow<ImmutableList<Application>>
    fun search(
        query: String,
        workspace: Workspace?, // Null means all of them
        getOnlyAdded: Boolean = false,
        getOnlyRemoved: Boolean = false
    ): Flow<ImmutableList<Application>>

    fun queryAppShortcuts(packageName: String): List<ShortcutInfo>
    fun loadShortcutIcon(packageName: String, shortcutId: String, widthPx: Int = 48, heightPx: Int = 48): Bitmap?
}

internal class AppRepositoryImpl(
    private val ctx: Context,
    profileManager: ProfileManager,
    private val packageManagerCompat: PackageManagerCompat,
    private val appOverridesManager: AppOverridesManager,
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


    private suspend fun refreshApps() {
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
            category = category
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
        )
    }


    override fun getAllApps(): Flow<ImmutableList<Application>> {
        return installedApps.map { it.toImmutableList() }.withCustomLabels(appOverridesManager)
    }

    override fun getLaunchableApps(): Flow<ImmutableList<Application>> {
        return launchableApps.map { it.toImmutableList() }.withCustomLabels(appOverridesManager)
    }

    override fun getSystemApps(): Flow<ImmutableList<Application>> {
        return systemApps.map { it.toImmutableList() }.withCustomLabels(appOverridesManager)
    }


    override fun findOne(
        packageName: String,
        user: UserHandle,
    ): Flow<Application?> {
        return installedApps.map { apps ->
            apps.firstOrNull {
                it.componentName.packageName == packageName && it.user == user
            }
        }
        // TODO add custom label
    }

    /**
     * Returns a filtered and sorted list of apps for the specified workspace as a reactive Flow.
     *
     * @param workspace The target workspace configuration defining app filtering rules
     * @param getOnlyAdded If true, returns ONLY apps explicitly added to this workspace [default: false]
     * @param getOnlyRemoved If true, returns ONLY apps hidden/removed from this workspace [default: false]
     * @return Flow of filtered, sorted, and resolved [Application] list
     *
     * @throws IllegalArgumentException if both [getOnlyAdded] and [getOnlyRemoved] are true
     *
     * @see WorkspaceType for base filtering behavior
     */
    private fun appsForWorkspace(
        workspace: Workspace?,
        getOnlyAdded: Boolean,
        getOnlyRemoved: Boolean
    ): Flow<List<Application>> {
        require(!(getOnlyAdded && getOnlyRemoved)) {
            "Cannot ask for only added AND only removed at the same time"
        }

        return installedApps.map { apps ->
            if (workspace == null) return@map apps

            val appIds = workspace.appIds ?: emptySet()
            val removedAppIds = workspace.removedAppIds ?: emptySet()

            when {
                getOnlyAdded -> apps.filter { it.key in appIds }
                getOnlyRemoved -> apps.filter { it.key in removedAppIds }
                else -> {
                    val base = when (workspace.type) {
                        ALL -> apps
                        CUSTOM -> emptyList()
                        USER -> apps.filter { !it.isWork && !it.isPrivate && it.isLaunchable }
                        SYSTEM -> apps.filter { it.isSystem }
                        WORK -> apps.filter { it.isWork && it.isLaunchable }
                        PRIVATE -> {
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
        }
    }

    override fun search(
        query: String,
        workspace: Workspace?, // Null means all of them
        getOnlyAdded: Boolean,
        getOnlyRemoved: Boolean
    ): Flow<ImmutableList<Application>> {

        val normalizedQuery = stringNormalizer.normalize(query)

        return appsForWorkspace(workspace, getOnlyAdded, getOnlyRemoved).map { apps ->

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
                        secondaryFields = appOverridesManager.getAliasesForApp(app)
                    )
                    if (score.score < 0.8f) return@mapNotNull null
                    app
                })
            }
            appResults.sort()
            appResults.toImmutableList()
        }.withCustomLabels(appOverridesManager)
    }

    override fun queryAppShortcuts(packageName: String): List<ShortcutInfo> =
        packageManagerCompat.queryAppShortcuts(packageName)

    override fun loadShortcutIcon(packageName: String, shortcutId: String, widthPx: Int, heightPx: Int): Bitmap? =
        packageManagerCompat.loadShortcutIcon(packageName, shortcutId, widthPx, heightPx)
}