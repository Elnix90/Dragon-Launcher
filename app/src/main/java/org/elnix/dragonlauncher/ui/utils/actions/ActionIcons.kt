package org.elnix.dragonlauncher.ui.utils.actions

import androidx.core.graphics.drawable.toBitmap
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import org.elnix.dragonlauncher.R

@Composable
fun actionIcon(action: SwipeActionSerializable): Painter {
    val ctx = LocalContext.current
    val pm: PackageManager = ctx.packageManager

    return when (action) {

        // 1. App icon
        is SwipeActionSerializable.LaunchApp -> {
            try {
                val appIcon = pm.getApplicationIcon(action.packageName)
                BitmapPainter(appIcon.toBitmap().asImageBitmap())
            } catch (_: Exception) {
                painterResource(R.drawable.ic_app_default)
            }
        }

        // 2. URL → use a globe icon
        is SwipeActionSerializable.OpenUrl ->
            painterResource(R.drawable.ic_action_web)

        // 3. Built-in actions with custom icons
        SwipeActionSerializable.NotificationShade ->
            painterResource(R.drawable.ic_action_notification)

        SwipeActionSerializable.ControlPanel ->
            painterResource(R.drawable.ic_action_grid)

        SwipeActionSerializable.OpenAppDrawer ->
            painterResource(R.drawable.ic_action_drawer)
    }
}

//fun Drawable.toBitmap() = this.toBitmap(
//    width = intrinsicWidth.takeIf { it > 0 } ?: 128,
//    height = intrinsicHeight.takeIf { it > 0 } ?: 128
//)
//
//@Composable
//fun actionIconBitmap(action: SwipeActionSerializable?): ImageBitmap? {
//    val resId = when (action) {
//        is SwipeActionSerializable.LaunchApp -> R.drawable.ic_app_default
//        is SwipeActionSerializable.OpenUrl -> R.drawable.ic_action_web
//        SwipeActionSerializable.NotificationShade -> R.drawable.ic_action_notification
//        SwipeActionSerializable.ControlPanel -> R.drawable.ic_action_grid
//        SwipeActionSerializable.OpenAppDrawer -> R.drawable.ic_action_drawer
//        else -> null
//    } ?: return null
//
//    return ImageVector
//        .vectorResource(id = resId)
//        .asImageBitmap()
//}
