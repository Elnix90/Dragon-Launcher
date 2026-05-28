package org.elnix.dragonlauncher.ui.actions

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.PackageManagerCompat
import androidx.core.net.toUri
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.common.navigaton.NavigationRoute.Settings.routeResId
import org.elnix.dragonlauncher.common.serializables.SwipeAction
import org.elnix.dragonlauncher.common.messyfolder.getFilePathFromUri
import org.elnix.dragonlauncher.ui.composition.LocalNests

@Composable
fun actionLabel(action: SwipeAction): String {
    val ctx = LocalContext.current
    val nests = LocalNests.current

    val pm = ctx.packageManager
    val packageManagerCompat = PackageManagerCompat(pm, ctx)

    return when (action) {

        is SwipeAction.LaunchApp -> {
            try {
                pm.getApplicationLabel(
                    pm.getApplicationInfo(action.packageName, 0)
                ).toString()
            } catch (_: Exception) {
                action.packageName
            }
        }

        is SwipeAction.LaunchShortcut -> {
            // Empty package = sentinel for "Pinned Shortcuts" chooser entry
            if (action.packageName.isEmpty()) {
                return stringResource(R.string.pinned_shortcuts)
            }

            val appLabel = try {
                pm.getApplicationLabel(
                    pm.getApplicationInfo(action.packageName, 0)
                ).toString()
            } catch (_: Exception) {
                action.packageName
            }

            val shortcutLabel = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    packageManagerCompat.queryAppShortcuts(action.packageName)
                        .firstOrNull { it.id == action.shortcutId }
                        ?.shortLabel
                        ?.toString()
                } else null
            } catch (_: Exception) {
                null
            }

            when {
                !shortcutLabel.isNullOrBlank() -> "$appLabel: $shortcutLabel"
                else -> appLabel
            }
        }


        is SwipeAction.OpenUrl -> action.url

        SwipeAction.NotificationShade -> stringResource(R.string.notifications)

        SwipeAction.ControlPanel -> stringResource(R.string.control_panel)

        is SwipeAction.OpenAppDrawer -> stringResource(R.string.app_drawer)

        is SwipeAction.OpenDragonLauncherSettings -> "${stringResource(R.string.dragon_launcher_settings)} (${stringResource(routeResId(action.route))})"

        SwipeAction.Lock -> stringResource(R.string.lock)

        is SwipeAction.OpenFile ->
            getFilePathFromUri(ctx, action.uri.toUri())

        SwipeAction.ReloadApps -> stringResource(R.string.reload_apps)

        SwipeAction.OpenRecentApps -> stringResource(R.string.recent_apps)

        is SwipeAction.OpenCircleNest -> {
            nests
                .find { it.id == action.nestId }
                ?.name
                ?.takeIf { it.trim().isNotEmpty() }
                ?: stringResource(R.string.open_nest_circle)
        }

        SwipeAction.GoParentNest -> stringResource(R.string.go_parent_nest)
        is SwipeAction.OpenWidget -> stringResource(R.string.widgets)
        is SwipeAction.RunAdbCommand -> action.command.trim().takeIf { it.isNotEmpty() } ?: stringResource(R.string.run_adb_command)
        is SwipeAction.ToggleBluetooth -> stringResource(R.string.toggle_bluetooth)
        is SwipeAction.ToggleData -> stringResource(R.string.toggle_mobile_data)
        is SwipeAction.ToggleWifi -> stringResource(R.string.toggle_wifi)
        SwipeAction.None -> "None"
        SwipeAction.KillLauncher -> stringResource(R.string.kill_launcher)
    }
}
