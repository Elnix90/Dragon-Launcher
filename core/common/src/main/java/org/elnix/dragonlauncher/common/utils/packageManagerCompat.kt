package org.elnix.dragonlauncher.common.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.content.ContextCompat
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.logging.logD
import org.elnix.dragonlauncher.common.logging.logE
import org.elnix.dragonlauncher.common.logging.logI
import org.elnix.dragonlauncher.common.serializables.AppModel
import org.elnix.dragonlauncher.common.serializables.mapAppToSection
import org.elnix.dragonlauncher.common.utils.ImageUtils.loadDrawableAsBitmap

class PackageManagerCompat(private val pm: PackageManager, private val ctx: Context) {

    fun getInstalledPackages(flags: Int = 0): List<PackageInfo> {
        return pm.getInstalledPackages(flags)
    }

    fun getAllApps(): List<AppModel> {
        val userManager = ctx.getSystemService(Context.USER_SERVICE) as UserManager
        val launcherApps = ctx.getSystemService(LauncherApps::class.java)

        val result = mutableListOf<AppModel>()

        userManager.userProfiles.forEach { userHandle ->
            val userId = userHandle.hashCode()
            val isMainProfile = userHandle == Process.myUserHandle()
            
            var isWorkProfile = false
            var isPrivateProfile = false
            
            if (!isMainProfile) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    try {
                        val userInfo = launcherApps?.getLauncherUserInfo(userHandle)
                        val userType = userInfo?.userType
                        
                        isPrivateProfile = userType == "android.os.usertype.profile.PRIVATE"
                        isWorkProfile = !isPrivateProfile
                    } catch (e: Exception) {
                        isWorkProfile = true
                    }
                } else {
                    isWorkProfile = true
                }
            }

            val activities = launcherApps
                ?.getActivityList(null, userHandle)
                ?: emptyList()
            
            logD(TAG, "Loading ${activities.size} apps for userId: $userId (Private: $isPrivateProfile)")

            activities.forEach { activity ->
                val appInfo = activity.applicationInfo
                val category = mapAppToSection(appInfo)
                val pkg = appInfo.packageName

                if (!isAppEnabled(pkg)) return@forEach
                
                result += AppModel(
                    name = activity.label?.toString() ?: pkg,
                    packageName = pkg,
                    userId = userId,
                    isEnabled = true,
                    isSystem = isSystemApp(appInfo),
                    isWorkProfile = isWorkProfile,
                    isPrivateProfile = isPrivateProfile,
                    isLaunchable = true,
                    category = category
                )
            }

            if (isMainProfile) {
                pm.getInstalledApplications(PackageManager.GET_META_DATA)
                    .forEach { appInfo ->
                        val pkg = appInfo.packageName
                        val category = mapAppToSection(appInfo)

                        if (result.any { it.packageName == pkg && it.userId == userId }) return@forEach
                        if (!isSystemApp(appInfo)) return@forEach
                        if (!appInfo.enabled) return@forEach

                        result += AppModel(
                            name = pm.getApplicationLabel(appInfo).toString(),
                            packageName = pkg,
                            userId = userId,
                            isEnabled = true,
                            isSystem = true,
                            isWorkProfile = false,
                            isPrivateProfile = false,
                            isLaunchable = false,
                            category = category
                        )
                    }
            }
        }

        val privateCount = result.count { it.isPrivateProfile }
        val workCount = result.count { it.isWorkProfile }
        val userCount = result.count { !it.isPrivateProfile && !it.isWorkProfile }
        logI(TAG, "Apps loaded: $userCount user, $workCount work, $privateCount private (total: ${result.size})")

        return result.distinctBy { "${it.packageName}_${it.userId}" }
    }

    private fun isAppEnabled(pkgName: String): Boolean {
        return try {
            pm.getApplicationEnabledSetting(pkgName) !=
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } catch (_: Exception) {
            true
        }
    }

    private fun isSystemApp(appInfo: ApplicationInfo): Boolean {
        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val isUpdatedSystem = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        return isSystem && !isUpdatedSystem &&
                (appInfo.packageName.startsWith("com.android.") || appInfo.packageName.startsWith("android"))
    }

    /**
     * Return a snapshot of packages visible on the main profile (User 0).
     * Used for differential detection when unlocking Private Space: capture before and after
     * and compute the difference.
     */
    fun snapshotMainProfilePackageNames(): Set<String> {
        val packages = mutableSetOf<String>()
        try {
            val launcherApps = ctx.getSystemService(LauncherApps::class.java)
            val userHandle = Process.myUserHandle()
            val activities = launcherApps?.getActivityList(null, userHandle) ?: emptyList()
            activities.forEach { act -> packages.add(act.applicationInfo.packageName) }

            // Include installed applications list to be robust (main profile only)
            pm.getInstalledApplications(PackageManager.GET_META_DATA).forEach { appInfo ->
                packages.add(appInfo.packageName)
            }

            logD(TAG, "snapshotMainProfilePackageNames: found ${packages.size} packages")
        } catch (e: Exception) {
            logE(TAG, "Error snapshotting main profile packages: ${e.message}")
        }
        return packages
    }

    /**
     * Check whether a package is visible/launchable for a given userId.
     * Returns true if LauncherApps reports an activity for that package in the specified user.
     */
    fun isPackageVisibleForUser(packageName: String, userId: Int): Boolean {
        try {
            val launcherApps = ctx.getSystemService(LauncherApps::class.java) ?: return false
            val userManager = ctx.getSystemService(UserManager::class.java) ?: return false
            val userHandle = userManager.userProfiles.firstOrNull { it.hashCode() == userId } ?: return false
            val activities = launcherApps.getActivityList(packageName, userHandle)
            return !activities.isNullOrEmpty()
        } catch (e: Exception) {
            logE(TAG, "Error checking package visibility for user $userId: ${e.message}")
            return false
        }
    }

    fun getAppIcon(packageName: String, userId: Int, isPrivateProfile: Boolean = false): Drawable {
        val launcherApps = ctx.getSystemService(LauncherApps::class.java)
        val userManager = ctx.getSystemService(UserManager::class.java)

        val userHandle = userManager.userProfiles
            .firstOrNull { it.hashCode() == userId }
            ?: Process.myUserHandle()

        return try {
            val isMainProfile = userHandle == Process.myUserHandle()
            
            if (!isMainProfile && !isPrivateProfile && launcherApps != null) {
                val activities = launcherApps.getActivityList(packageName, userHandle)
                if (!activities.isNullOrEmpty()) {
                    return activities[0].getBadgedIcon(0)
                }
                val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    launcherApps.getApplicationInfo(packageName, 0, userHandle)
                } else {
                    pm.getApplicationInfo(packageName, 0)
                }
                return appInfo.loadIcon(pm)
            }
            
            if (isPrivateProfile && launcherApps != null) {
                val activities = launcherApps.getActivityList(packageName, userHandle)
                if (!activities.isNullOrEmpty()) {
                    return activities[0].getIcon(0)
                }
                val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    launcherApps.getApplicationInfo(packageName, 0, userHandle)
                } else {
                    pm.getApplicationInfo(packageName, 0)
                }
                return appInfo.loadIcon(pm)
            }

            val appInfo = pm.getApplicationInfo(packageName, 0)
            appInfo.loadIcon(pm)

        } catch (e: Exception) {
            ContextCompat.getDrawable(ctx, R.drawable.ic_app_default)!!
        }
    }

    fun getResourcesForApplication(pkgName: String): Resources {
        return pm.getResourcesForApplication(pkgName)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun queryAppShortcuts(packageName: String): List<ShortcutInfo> {
        try {
            val launcherApps = ctx.getSystemService(LauncherApps::class.java)
            if (launcherApps == null) return emptyList()

            val query = LauncherApps.ShortcutQuery()
                .setPackage(packageName)
                .setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_CACHED
                )
            
            val userHandle = Process.myUserHandle()
            val shortcuts = launcherApps.getShortcuts(query, userHandle)

            return shortcuts ?: emptyList()

        } catch (e: Exception) {
            return emptyList()
        }
    }
}

fun launchShortcut(ctx: Context, pkg: String, id: String) {
    val launcherApps = ctx.getSystemService(LauncherApps::class.java) ?: return
    try {
        launcherApps.startShortcut(pkg, id, null, null, Process.myUserHandle())
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun loadShortcutIcon(
    ctx: Context,
    packageName: String,
    shortcutId: String
): ImageBitmap? {
    try {
        val launcherApps = ctx.getSystemService(LauncherApps::class.java) ?: return null
        val user = Process.myUserHandle()

        val query = LauncherApps.ShortcutQuery()
            .setPackage(packageName)
            .setQueryFlags(
                LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
            )

        val shortcuts = launcherApps.getShortcuts(query, user) ?: return null
        val shortcut = shortcuts.firstOrNull { it.id == shortcutId } ?: return null

        val drawable = launcherApps.getShortcutIconDrawable(shortcut, 0) ?: return null

        return loadDrawableAsBitmap(drawable, 48, 48)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}
