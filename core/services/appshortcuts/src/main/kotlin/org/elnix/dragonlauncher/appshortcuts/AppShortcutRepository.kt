package org.elnix.dragonlauncher.appshortcuts

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Process
import android.os.UserHandle
import androidx.core.content.getSystemService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.elnix.dragonlauncher.StringNormalizer
import org.elnix.dragonlauncher.base.model.models.ResultScore
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.permissions.PermissionGroup
import org.elnix.dragonlauncher.permissions.PermissionsManager
import org.elnix.dragonlauncher.profiles.ProfileManager

public interface AppShortcutRepository {

//    public fun findMany(
//        componentName: ComponentName? = null,
//        user: UserHandle = Process.myUserHandle(),
//        manifest: Boolean = false,
//        dynamic: Boolean = false,
//        pinned: Boolean = false,
//        cached: Boolean = false,
//        limit: Int = 5,
//    ): Flow<ImmutableList<ShortcutInfo>>

    public fun search(query: String): Flow<ImmutableList<ShortcutInfo>>

    public fun findOne(action: Action.LaunchShortcut): Flow<ShortcutInfo?>
    public suspend fun fromAction(action: Action.LaunchShortcut): ShortcutInfo?

    public suspend fun getShortcutsConfigActivities(): List<AppShortcutConfigActivity>
}

internal class AppShortcutRepositoryImpl(
    private val context: Context,
    private val permissionsManager: PermissionsManager,
    private val profileManager: ProfileManager,
    private val stringNormalizer: StringNormalizer,
) : AppShortcutRepository {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val mutex = Mutex()

    private val installedShortcuts = MutableStateFlow<List<ShortcutInfo>>(emptyList())
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    init {
        // FUUUUUUCK!! Basically this function was never called. Idk if the callback below works, but at least it is called at least once!
        refreshShortcuts()
        launcherApps.registerCallback(
            object : LauncherApps.Callback() {
                override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
                }

                override fun onPackageAdded(packageName: String?, user: UserHandle?) {
                }

                override fun onPackageChanged(packageName: String?, user: UserHandle?) {
                }

                override fun onPackagesAvailable(
                    packageNames: Array<out String>?,
                    user: UserHandle?,
                    replacing: Boolean
                ) {
                }

                override fun onPackagesUnavailable(
                    packageNames: Array<out String>?,
                    user: UserHandle?,
                    replacing: Boolean
                ) {
                }

                override fun onShortcutsChanged(
                    packageName: String,
                    shortcuts: MutableList<ShortcutInfo>,
                    user: UserHandle
                ) {
                    super.onShortcutsChanged(packageName, shortcuts, user)
                    refreshShortcuts()
                }
            }
        )
    }


//    override fun findMany(
//        componentName: ComponentName?,
//        user: UserHandle,
//        manifest: Boolean,
//        dynamic: Boolean,
//        pinned: Boolean,
//        cached: Boolean,
//        limit: Int
//    ): Flow<ImmutableList<ShortcutInfo>> = flow {
//        val shortcuts = withContext(Dispatchers.IO) {
//            val launcherApps = context.getSystemService<LauncherApps>()!!
//            if (!launcherApps.hasShortcutHostPermission()) return@withContext emptyList()
//            val query = LauncherApps.ShortcutQuery()
//                .setActivity(componentName)
//                .setQueryFlags(
//                    buildQueryFlags(manifest, dynamic, pinned, cached)
//                )
//
//            try {
//                launcherApps.getShortcuts(query, user) ?: emptyList()
//            } catch (e: IllegalStateException) {
//                emptyList()
//            }
//        }
//        emit(shortcuts.toImmutableList())
//    }
//
//    @SuppressLint("InlinedApi")
//    private fun buildQueryFlags(
//        manifest: Boolean,
//        dynamic: Boolean,
//        pinned: Boolean,
//        cached: Boolean,
//    ): Int {
//        var flags = 0
//        if (manifest) flags = flags or LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
//        if (dynamic) flags = flags or LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC
//        if (pinned) flags = flags or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
//        if (cached) flags = flags or LauncherApps.ShortcutQuery.FLAG_MATCH_CACHED
//        return flags
//    }
//


    @SuppressLint("InlinedApi")
    private fun refreshShortcuts() {
        scope.launch {
            mutex.withLock {
                if (!permissionsManager.hasPermission(PermissionGroup.AppShortcuts).first()) return@launch
                val launcherApps = context.getSystemService<LauncherApps>() ?: return@launch

                val shortcutQuery = LauncherApps.ShortcutQuery()
                shortcutQuery.setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_CACHED or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED_BY_ANY_LAUNCHER
                )
                val shortcuts = launcherApps.getShortcuts(shortcutQuery, Process.myUserHandle())?.mapNotNull { it } ?: return@launch
                installedShortcuts.value = shortcuts
            }
        }
    }

    @SuppressLint("InlinedApi")
    override fun search(query: String): Flow<ImmutableList<ShortcutInfo>> {
        val normalizedQuery = stringNormalizer.normalize(query)

        return installedShortcuts.map { shortcuts ->
            shortcuts
                .sortedBy { shortcutInfo ->
                    ResultScore.from(
                        query = normalizedQuery,
                        primaryFields = listOfNotNull(
                            shortcutInfo.longLabel?.toString()
                                ?.let { stringNormalizer.normalize(it) },
                            shortcutInfo.shortLabel?.toString()
                                ?.let { stringNormalizer.normalize(it) },
                        )
                    )
                }
                .toImmutableList()
        }.flowOn(Dispatchers.Default)
    }

    override fun findOne(action: Action.LaunchShortcut): Flow<ShortcutInfo?> {
        return installedShortcuts.map { shortcuts ->
            shortcuts.firstOrNull {
                it.userHandle == action.user &&
                        it.`package` == action.packageName &&
                        it.id == action.shortcutId
            }
        }
    }

    override suspend fun fromAction(action: Action.LaunchShortcut): ShortcutInfo? {
        return findOne(action).first()
    }

    override suspend fun getShortcutsConfigActivities(): List<AppShortcutConfigActivity> {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        if (!launcherApps.hasShortcutHostPermission()) return emptyList()
        val results = mutableListOf<AppShortcutConfigActivity>()
        val profiles = profileManager.activeProfiles.first()
        for (profile in profiles) {
            val activities = launcherApps.getShortcutConfigActivityList(null, profile.userHandle)
            results.addAll(
                activities.map {
                    AppShortcutConfigActivity(it)
                }
            )
        }
        return results.sorted()
    }
}