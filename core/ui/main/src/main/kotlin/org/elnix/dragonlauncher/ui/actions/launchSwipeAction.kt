package org.elnix.dragonlauncher.ui.actions

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Build
import android.os.Process
import android.os.UserManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.utils.Constants.Logging.APP_LAUNCH_TAG
import org.elnix.dragonlauncher.common.utils.Constants.Logging.TAG
import org.elnix.dragonlauncher.common.utils.closeApp
import org.elnix.dragonlauncher.common.utils.expandQuickActionsDrawer
import org.elnix.dragonlauncher.common.utils.getMobileDataStatus
import org.elnix.dragonlauncher.common.utils.hasUriReadPermission
import org.elnix.dragonlauncher.common.utils.isBluetoothEnabled
import org.elnix.dragonlauncher.common.utils.isWifiEnabled
import org.elnix.dragonlauncher.common.utils.launchShortcut
import org.elnix.dragonlauncher.common.utils.showToast
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
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
    onOpenPrivateSpaceApp: (SwipeActionSerializable) -> Unit,
    digitalPauseLauncher: ActivityResultLauncher<Intent>,
    onReloadApps: () -> Unit,
    onReselectFile: () -> Unit,
    onAppSettings: (String) -> Unit,
    onAppDrawer: (workspaceId: String?) -> Unit,
    onShizukuCommand: (SwipeActionSerializable.RunAdbCommand) -> Unit
) {
    if (action == null) return

    when (action) {

        is SwipeActionSerializable.LaunchApp -> {

            try {

                logD(APP_LAUNCH_TAG) { "Launching action: $action" }

                /*  ─────────────  1. Private Space Check ─────────────  */
                if (action.isPrivateSpace) {
                    onOpenPrivateSpaceApp(action)
                    return
                }

                /*  ─────────────  2. Wellbeing Pause Check  ─────────────  */
                if (socialMediaPauseEnabled && action.packageName in pausedApps) {
                    val intent = Intent(ctx, DigitalPauseActivity::class.java).apply {
                        putExtra(DigitalPauseActivity.EXTRA_PACKAGE_NAME, action.packageName)
                        putExtra(DigitalPauseActivity.EXTRA_APP_NAME, appName)
                        putExtra(DigitalPauseActivity.EXTRA_PAUSE_DURATION, pauseDuration)
                        putExtra(DigitalPauseActivity.EXTRA_GUILT_MODE, guiltModeEnabled)
                        putExtra(DigitalPauseActivity.EXTRA_REMINDER_ENABLED, reminderEnabled)
                        putExtra(
                            DigitalPauseActivity.EXTRA_REMINDER_INTERVAL,
                            reminderIntervalMinutes
                        )
                        putExtra(DigitalPauseActivity.EXTRA_REMINDER_MODE, reminderMode)
                        putExtra(
                            DigitalPauseActivity.EXTRA_RETURN_TO_LAUNCHER,
                            returnToLauncherEnabled
                        )
                    }
                    digitalPauseLauncher.launch(intent)
                    return
                }


                // If app has no wellbeing checks to do; it launches directly
                launchAppDirectly(appsViewModel, ctx, action.packageName, action.userId ?: 0)
            } catch (e: AppLaunchException) {
                logE(APP_LAUNCH_TAG, e) { e.toString() }
            } catch (e: Exception) {
                logE(APP_LAUNCH_TAG, e) { e.toString() }
                e.printStackTrace()
            }
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
                ctx.showToast(ctx.getString(R.string.please_enable_accessibility_services_to_use_that_feature))
                SystemControl.openServiceSettings(ctx)
                return
            }
            SystemControl.expandNotifications()
        }

        SwipeActionSerializable.ControlPanel -> {
            if (useAccessibilityInsteadOfContextToExpandActionPanel) {
                SystemControl.expandQuickSettings(
                    ctx
                )
            } else expandQuickActionsDrawer(ctx)
        }

        is SwipeActionSerializable.OpenAppDrawer -> {
            onAppDrawer(action.workspaceId)
        }

        is SwipeActionSerializable.OpenDragonLauncherSettings -> {
            onAppSettings(action.route)
        }

        SwipeActionSerializable.Lock -> {
            if (!SystemControl.isServiceEnabled(ctx)) {
                ctx.showToast("Please enable accessibility settings to use that feature")
                SystemControl.openServiceSettings(ctx)
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                SystemControl.lockScreen(ctx)
            } else {
                ctx.showToast(ctx.getString(R.string.not_supported_in_this_android_version))
            }
        }

        is SwipeActionSerializable.OpenFile -> {
            try {
                val uri = action.uri.toUri()

                if (!ctx.hasUriReadPermission(uri)) {
                    ctx.showToast("Please reselect the file to allow access")
                    onReselectFile()
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
                ctx.showToast("Unable to open file")
                logE(TAG, e) { "Unable to open file" }
            }
        }

        SwipeActionSerializable.ReloadApps -> onReloadApps()
        SwipeActionSerializable.OpenRecentApps -> {
            if (!SystemControl.isServiceEnabled(ctx)) {
                ctx.showToast("Please enable accessibility settings to use that feature")
                SystemControl.openServiceSettings(ctx)
                return
            }
            SystemControl.openRecentApps(ctx)
        }


        is SwipeActionSerializable.RunAdbCommand -> {
            onShizukuCommand(action)
        }

        is SwipeActionSerializable.ToggleBluetooth -> {
            onShizukuCommand(
                SwipeActionSerializable.RunAdbCommand(
                    command = if (ctx.isBluetoothEnabled()) {
                        action.command.commandDisable
                    } else {
                        action.command.commandEnable
                    },
                    toast = action.toast == true
                )
            )
        }

        is SwipeActionSerializable.ToggleData -> {
            onShizukuCommand(
                SwipeActionSerializable.RunAdbCommand(
                    command = if (ctx.getMobileDataStatus().first) {
                        action.command.commandDisable
                    } else {
                        action.command.commandEnable
                    },
                    toast = action.toast == true
                )
            )
        }

        is SwipeActionSerializable.ToggleWifi -> {
            onShizukuCommand(
                SwipeActionSerializable.RunAdbCommand(
                    command = if (ctx.isWifiEnabled()) {
                        action.command.commandDisable
                    } else {
                        action.command.commandEnable
                    },
                    toast = action.toast == true
                )
            )
        }

        SwipeActionSerializable.KillLauncher -> {
            closeApp(ctx as ComponentActivity)
        }

        is SwipeActionSerializable.OpenCircleNest, SwipeActionSerializable.GoParentNest -> {} // Handled by the main screen / settings
        is SwipeActionSerializable.OpenWidget -> {} // The widget action isn't meant to be part of the choosable actions, so nothing on launch

        SwipeActionSerializable.None -> {}
    }
}

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

    logD(APP_LAUNCH_TAG) { "pkg: $packageName; userId: $userId: handle: $targetUserHandle" }

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
        logE(APP_LAUNCH_TAG, e) { "Security error launching $packageName" }
        throw AppLaunchException("Security error launching $packageName", e)
    } catch (e: NullPointerException) {
        logE(APP_LAUNCH_TAG, e) { "App component not found for $packageName" }
        throw AppLaunchException("App component not found for $packageName", e)
    } catch (e: Exception) {
        logE(APP_LAUNCH_TAG, e) { "Failed to launch $packageName" }
        throw AppLaunchException("Failed to launch $packageName", e)
    }
}
