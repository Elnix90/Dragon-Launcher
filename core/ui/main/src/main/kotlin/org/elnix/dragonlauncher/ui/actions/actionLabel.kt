package org.elnix.dragonlauncher.ui.actions

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.navigaton.routeResId
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.utils.PackageManagerCompat
import org.elnix.dragonlauncher.common.utils.getFilePathFromUri
import org.elnix.dragonlauncher.ui.composition.LocalNests

@Composable
fun actionLabel(action: SwipeActionSerializable): String {
    val ctx = LocalContext.current
    val nests = LocalNests.current

    val pm = ctx.packageManager
    val packageManagerCompat = PackageManagerCompat(pm, ctx)

    return when (action) {

        is SwipeActionSerializable.LaunchApp -> {
            try {
                pm.getApplicationLabel(
                    pm.getApplicationInfo(action.packageName, 0)
                ).toString()
            } catch (_: Exception) {
                action.packageName
            }
        }

        is SwipeActionSerializable.LaunchShortcut -> {
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


        is SwipeActionSerializable.OpenUrl -> action.url

        SwipeActionSerializable.NotificationShade -> stringResource(R.string.notifications)

        SwipeActionSerializable.ControlPanel -> stringResource(R.string.control_panel)

        is SwipeActionSerializable.OpenAppDrawer -> stringResource(R.string.app_drawer)

        is SwipeActionSerializable.OpenDragonLauncherSettings -> "${stringResource(R.string.dragon_launcher_settings)} (${stringResource(routeResId(action.route))})"

        SwipeActionSerializable.Lock -> stringResource(R.string.lock)

        is SwipeActionSerializable.OpenFile ->
            getFilePathFromUri(ctx, action.uri.toUri())

        SwipeActionSerializable.ReloadApps -> stringResource(R.string.reload_apps)

        SwipeActionSerializable.OpenRecentApps -> stringResource(R.string.recent_apps)

        is SwipeActionSerializable.OpenCircleNest -> {
            nests
                .find { it.id == action.nestId }
                ?.name
                ?.takeIf { it.trim().isNotEmpty() }
                ?: stringResource(R.string.open_nest_circle)
        }

        SwipeActionSerializable.GoParentNest -> stringResource(R.string.go_parent_nest)
        is SwipeActionSerializable.OpenWidget -> stringResource(R.string.widgets)
        is SwipeActionSerializable.RunAdbCommand -> action.command.trim().takeIf { it.isNotEmpty() } ?: stringResource(R.string.run_adb_command)
        is SwipeActionSerializable.ToggleBluetooth -> stringResource(R.string.toggle_bluetooth)
        is SwipeActionSerializable.ToggleData -> stringResource(R.string.toggle_mobile_data)
        is SwipeActionSerializable.ToggleWifi -> stringResource(R.string.toggle_wifi)
        SwipeActionSerializable.None -> "None"
        SwipeActionSerializable.KillLauncher -> stringResource(R.string.kill_launcher)
    }
}
