package org.elnix.dragonlauncher.ui.actions

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri
import org.elnix.dragonlauncher.common.logging.logD
import org.elnix.dragonlauncher.common.logging.logE
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.utils.Constants.Logging.APP_LAUNCH_TAG
import org.elnix.dragonlauncher.common.utils.expandQuickActionsDrawer
import org.elnix.dragonlauncher.common.utils.hasUriReadPermission
import org.elnix.dragonlauncher.common.utils.launchShortcut
import org.elnix.dragonlauncher.common.utils.showToast
import org.elnix.dragonlauncher.models.AppsViewModel
import org.elnix.dragonlauncher.services.SystemControl
import org.elnix.dragonlauncher.ui.wellbeing.DigitalPauseActivity


/**
 * Exception for app launch failures
 */
class AppLaunchException(message: String, cause: Throwable? = null) : Exception(message, cause)


fun launchSwipeAction(
    ctx: Context,
    appsViewModel: AppsViewModel,
    action: SwipeActionSerializable?,
    useAccessibilityInsteadOfContextToExpandActionPanel: Boolean = true,
    pausedApps: Set<String> = emptySet(),
    socialMediaPauseEnabled: Boolean = false,
    guiltModeEnabled: Boolean = false,
    pauseDuration: Int = 10,
    reminderEnabled: Boolean = false,
    reminderIntervalMinutes: Int = 5,
    reminderMode: String = "overlay",
    returnToLauncherEnabled: Boolean = false,
    appName: String = "",
    digitalPauseLauncher: ActivityResultLauncher<Intent>? = null,
    onReloadApps: (() -> Unit)? = null,
    onReselectFile: (() -> Unit)? = null,
    onAppSettings: ((String) -> Unit)? = null,
    onAppDrawer: ((workspaceId: String?) -> Unit)? = null,
    onParentNest: (() -> Unit)? = null,
    onOpenNestCircle: ((nestId: Int) -> Unit)? = null,
) {
    if (action == null) return

    when (action) {

        is SwipeActionSerializable.LaunchApp -> {
            /*  ─────────────  Wellbeing Pause Check  ─────────────  */
            if (socialMediaPauseEnabled && action.packageName in pausedApps && digitalPauseLauncher != null) {
                val intent = Intent(ctx, DigitalPauseActivity::class.java).apply {
                    putExtra(DigitalPauseActivity.EXTRA_PACKAGE_NAME, action.packageName)
                    putExtra(DigitalPauseActivity.EXTRA_APP_NAME, appName)
                    putExtra(DigitalPauseActivity.EXTRA_PAUSE_DURATION, pauseDuration)
                    putExtra(DigitalPauseActivity.EXTRA_GUILT_MODE, guiltModeEnabled)
                    putExtra(DigitalPauseActivity.EXTRA_REMINDER_ENABLED, reminderEnabled)
                    putExtra(DigitalPauseActivity.EXTRA_REMINDER_INTERVAL, reminderIntervalMinutes)
                    putExtra(DigitalPauseActivity.EXTRA_REMINDER_MODE, reminderMode)
                    putExtra(DigitalPauseActivity.EXTRA_RETURN_TO_LAUNCHER, returnToLauncherEnabled)
                }
                digitalPauseLauncher.launch(intent)
                return
            }

            launchAppDirectly(appsViewModel, ctx, action.packageName, action.userId ?: 0)
        }


        is SwipeActionSerializable.LaunchShortcut -> {
            if (action.packageName.isNotEmpty()) {
                launchShortcut(ctx, action.packageName, action.shortcutId)
            }
        }


        is SwipeActionSerializable.OpenUrl -> {
            val i = Intent(Intent.ACTION_VIEW, action.url.toUri())
            ctx.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        SwipeActionSerializable.NotificationShade -> {
            if (!SystemControl.isServiceEnabled(ctx)) {
                ctx.showToast("Please enable accessibility settings to use that feature")
                SystemControl.openServiceSettings(ctx)
                return
            }
            SystemControl.expandNotifications(ctx)
        }

        SwipeActionSerializable.ControlPanel -> {
            if (useAccessibilityInsteadOfContextToExpandActionPanel) {
                SystemControl.expandQuickSettings(
                    ctx
                )
            }
            else expandQuickActionsDrawer(ctx)
        }

        is SwipeActionSerializable.OpenAppDrawer -> {
            onAppDrawer?.invoke(action.workspaceId)
        }

        is SwipeActionSerializable.OpenDragonLauncherSettings -> {
            onAppSettings?.invoke(action.route)
        }

        SwipeActionSerializable.Lock -> {
            if (!SystemControl.isServiceEnabled(ctx)) {
                ctx.showToast("Please enable accessibility settings to use that feature")
                SystemControl.openServiceSettings(ctx)
                return
            }
            SystemControl.lockScreen(ctx)
        }

        is SwipeActionSerializable.OpenFile -> {
            try {
                val uri = action.uri.toUri()

                if (!ctx.hasUriReadPermission(uri)) {
                    ctx.showToast("Please reselect the file to allow access")
                    onReselectFile?.invoke()
                    return
                }

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, action.mimeType ?: "*/*")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                if (intent.resolveActivity(ctx.packageManager) != null) {
                    ctx.startActivity(intent)
                } else {
                    ctx.showToast("No app available to open this file")
                }

            } catch (e: Exception) {
                ctx.showToast("Unable to open file: ${e.message}")
                ctx.logE("OpenFile", e.toString())
            }
        }

        SwipeActionSerializable.ReloadApps -> onReloadApps?.invoke()
        SwipeActionSerializable.OpenRecentApps -> {
            SystemControl.openRecentApps(ctx)
        }

        is SwipeActionSerializable.OpenCircleNest -> onOpenNestCircle?.invoke(action.nestId)
        SwipeActionSerializable.GoParentNest -> onParentNest?.invoke()
        is SwipeActionSerializable.OpenWidget -> {} // The widget action isn't mean to be part of the choosable actions, so nothing on launch
        SwipeActionSerializable.None -> {}
    }
}



// TODO Here it may be a big bug related to launching package: if an app is in both normal  and work, it may launch the first that it finds
/**
 * Launch an app directly without any pause check.
 * Used both by launchSwipeAction and after the digital pause screen.
 */
fun launchAppDirectly(
    appsViewModel: AppsViewModel,
    ctx: Context,
    packageName: String,
    userId: Int
) {
    val userManager = ctx.getSystemService(Context.USER_SERVICE) as UserManager
    val launcherApps = ctx.getSystemService(LauncherApps::class.java)
        ?: throw AppLaunchException("LauncherApps unavailable")

    val allUsers = userManager.userProfiles

    // 1. Find the user profile that owns the package
    val targetUserHandle = allUsers.firstOrNull { userHandle ->

        // Selects the requested user handle, that corresponds to userId
        userHandle.hashCode() == userId


//        launcherApps
//            .getActivityList(null, userHandle)
//            .any { it.applicationInfo.packageName == packageName }
    } ?: Process.myUserHandle()

    ctx.logD(APP_LAUNCH_TAG, "pkg: $packageName; userId: $userId: handle: $targetUserHandle")

    // 2. Find the launcher activity in that profile
    val activity = launcherApps
        .getActivityList(null, targetUserHandle)
        .firstOrNull { it.applicationInfo.packageName == packageName }
        ?: throw AppLaunchException("Launcher activity not found for $packageName")

    // 3. Launch correctly (profile-aware)
    try {
        launcherApps.startMainActivity(
            activity.componentName,
            targetUserHandle,
            null,
            null
        )

        // Track recently used app
        appsViewModel.addRecentlyUsedApp(packageName)
    } catch (e: SecurityException) {
        throw AppLaunchException("Security error launching $packageName", e)
    } catch (e: NullPointerException) {
        throw AppLaunchException("App component not found for $packageName", e)
    } catch (e: Exception) {
        throw AppLaunchException("Failed to launch $packageName", e)
    }
}
