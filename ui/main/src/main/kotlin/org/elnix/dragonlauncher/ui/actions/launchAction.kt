package org.elnix.dragonlauncher.ui.actions

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import androidx.core.net.toUri
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.common.utils.ConnectivityUtils.getMobileDataStatus
import org.elnix.dragonlauncher.common.utils.ConnectivityUtils.isBluetoothEnabled
import org.elnix.dragonlauncher.common.utils.ConnectivityUtils.isWifiEnabled
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.expandQuickActionsDrawer
import org.elnix.dragonlauncher.ktx.hasUriReadPermission
import org.elnix.dragonlauncher.ktx.showToast
import io.github.elnix90.logging.TAG
import io.github.elnix90.logging.logE
import org.elnix.dragonlauncher.models.AppLaunchViewModel
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.services.SystemControl


internal fun launchAction(
    ctx: Context,
    appLaunchViewModel: AppLaunchViewModel,
    drawerViewModel: DrawerViewModel,
    action: Action,
    useAccessibilityInsteadOfContextToExpandActionPanel: Boolean = true,
    onReselectFile: () -> Unit,
    onAppSettings: (NavigationRoute) -> Unit,
    onAppDrawer: (workspaceId: String?) -> Unit,
    onShizukuCommand: (Action.RunAdbCommand) -> Unit
) {
    when (action) {

        is Action.LaunchApp -> appLaunchViewModel.requestAppLaunch(action)

        is Action.LaunchShortcut -> appLaunchViewModel.launchShortcut(action)

        is Action.OpenUrl -> {
            val i = Intent(Intent.ACTION_VIEW, action.url.toUri())
            ctx.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        Action.NotificationShade -> {
            if (!SystemControl.isServiceEnabled(ctx)) {
                ctx.showToast(ctx.getString(R.string.please_enable_accessibility_services_to_use_that_feature))
                SystemControl.openServiceSettings(ctx)
                return
            }
            SystemControl.expandNotifications()
        }

        Action.ControlPanel -> {
            if (useAccessibilityInsteadOfContextToExpandActionPanel) {
                SystemControl.expandQuickSettings(
                    ctx
                )
            } else ctx.expandQuickActionsDrawer()
        }

        is Action.OpenAppDrawer -> onAppDrawer(action.workspaceId)

        is Action.OpenDragonLauncherSettings -> onAppSettings(action.route)

        Action.Lock -> {
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

        is Action.OpenFile -> {
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

        Action.ReloadApps -> drawerViewModel.reloadApps()

        Action.OpenRecentApps -> {
            if (!SystemControl.isServiceEnabled(ctx)) {
                ctx.showToast("Please enable accessibility settings to use that feature")
                SystemControl.openServiceSettings(ctx)
                return
            }
            SystemControl.openRecentApps(ctx)
        }


        is Action.RunAdbCommand -> onShizukuCommand(action)

        is Action.ToggleBluetooth -> {
            onShizukuCommand(
                Action.RunAdbCommand(
                    command = if (ctx.isBluetoothEnabled()) {
                        action.command.commandDisable
                    } else {
                        action.command.commandEnable
                    },
                    toast = action.toast == true
                )
            )
        }

        is Action.ToggleData -> {
            onShizukuCommand(
                Action.RunAdbCommand(
                    command = if (ctx.getMobileDataStatus().first) {
                        action.command.commandDisable
                    } else {
                        action.command.commandEnable
                    },
                    toast = action.toast == true
                )
            )
        }

        is Action.ToggleWifi -> {
            onShizukuCommand(
                Action.RunAdbCommand(
                    command = if (ctx.isWifiEnabled()) {
                        action.command.commandDisable
                    } else {
                        action.command.commandEnable
                    },
                    toast = action.toast == true
                )
            )
        }

        Action.KillLauncher -> Process.killProcess(Process.myPid())

        // Handled by the main screen / settings
        // The widget action isn't meant to be part of the choosable actions, so nothing on launch
        // None do nothing, pretty straightforward
        is Action.OpenCircleNest, is Action.GoParentNest, is Action.OpenWidget, Action.None -> error("Action $action shouldn't be handled here")
    }
}


