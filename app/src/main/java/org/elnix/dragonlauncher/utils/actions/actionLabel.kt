package org.elnix.dragonlauncher.utils.actions

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.elnix.dragonlauncher.data.SwipeActionSerializable

@Composable
fun actionLabel(action: SwipeActionSerializable): String {
    val ctx = LocalContext.current
    val pm = ctx.packageManager

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

        is SwipeActionSerializable.OpenUrl ->
            action.url

        SwipeActionSerializable.NotificationShade ->
            "Notifications"

        SwipeActionSerializable.ControlPanel ->
            "Control Panel"

        SwipeActionSerializable.OpenAppDrawer ->
            "App Drawer"

        SwipeActionSerializable.OpenDragonLauncherSettings ->
            "Dragon Launcher Settings"
    }
}
