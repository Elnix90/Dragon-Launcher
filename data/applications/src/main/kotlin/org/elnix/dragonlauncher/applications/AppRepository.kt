package org.elnix.dragonlauncher.applications

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
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
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.StringNormalizer
import org.elnix.dragonlauncher.appoverrides.AppOverridesManager
import org.elnix.dragonlauncher.base.model.models.AppCategory.Companion.mapAppToSection
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.models.Application.Companion.getPackageVersionName
import org.elnix.dragonlauncher.base.model.models.LauncherApp
import org.elnix.dragonlauncher.base.model.models.ResultScore
import org.elnix.dragonlauncher.base.model.models.SystemApp
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.compat.PackageManagerCompat
import org.elnix.dragonlauncher.profiles.ProfileManager

interface AppRepository {
    fun findOne(packageName: String, user: UserHandle): Flow<Application?>
    fun getAllApps(): Flow<ImmutableList<Application>>
    fun getLaunchableApps(): Flow<ImmutableList<Application>>
    fun getSystemApps(): Flow<ImmutableList<Application>>
    fun search(query: String): Flow<ImmutableList<Application>>

    fun queryAppShortcuts(packageName: String): List<ShortcutInfo>
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
        val applicationInfo = activityInfo.applicationInfo

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
    }

    override fun search(query: String): Flow<ImmutableList<Application>> {
        val normalizedQuery = stringNormalizer.normalize(query)

        return installedApps.map { apps ->
            withContext(Dispatchers.Default) {
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
                            )
                        )
                        if (score.score < 0.8f) return@mapNotNull null
                        app
                    })
                }
                appResults.sort()
                appResults.toImmutableList()
            }
        }
    }

    override fun queryAppShortcuts(packageName: String): List<ShortcutInfo> = packageManagerCompat.queryAppShortcuts(packageName)
}