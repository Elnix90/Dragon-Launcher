package org.elnix.dragonlauncher.utils.actions

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
    val pm = ctx.packageManager

    return when (action) {

        is SwipeActionSerializable.LaunchApp -> {
            try {
                val icon = pm.getApplicationIcon(action.packageName)
                BitmapPainter(icon.toBitmap().asImageBitmap())
            } catch (_: Exception) {
                painterResource(R.drawable.ic_app_default)
            }
        }

        is SwipeActionSerializable.OpenUrl ->
            painterResource(R.drawable.ic_action_web)

        SwipeActionSerializable.NotificationShade ->
            painterResource(R.drawable.ic_action_notification)

        SwipeActionSerializable.ControlPanel ->
            painterResource(R.drawable.ic_action_grid)

        SwipeActionSerializable.OpenAppDrawer ->
            painterResource(R.drawable.ic_action_drawer)
    }
}
