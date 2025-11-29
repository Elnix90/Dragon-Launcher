package org.elnix.dragonlauncher.utils.actions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.SwipeActionSerializable

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


//fun actionIconBitmap(action: SwipeActionSerializable, context: Context): ImageBitmap {
//
//
//    return when (action) {
//        is SwipeActionSerializable.LaunchApp -> {
//            try {
//                val drawable = pm.getApplicationIcon(action.packageName)
//                drawable.toBitmap(48, 48).asImageBitmap()
//            } catch (e: Exception) {
//                vectorToBitmap(context, R.drawable.ic_app_default)
//            }
//        }
//
//        is SwipeActionSerializable.OpenUrl ->
//            vectorToBitmap(context, R.drawable.ic_action_web)
//
//        SwipeActionSerializable.NotificationShade ->
//            vectorToBitmap(context, R.drawable.ic_action_notification)
//
//        SwipeActionSerializable.ControlPanel ->
//            vectorToBitmap(context, R.drawable.ic_action_grid)
//
//        SwipeActionSerializable.OpenAppDrawer ->
//            vectorToBitmap(context, R.drawable.ic_action_drawer)
//    }
//}

fun actionIconBitmap(
    action: SwipeActionSerializable,
    context: Context,
    tintColor: Color // Add this!
): ImageBitmap {
    val bitmap = createUntintedBitmap(action, context)
    return if (action is SwipeActionSerializable.LaunchApp) {
        bitmap
    } else {
        tintBitmap(bitmap, tintColor)
    }
}

private fun createUntintedBitmap(action: SwipeActionSerializable, context: Context): ImageBitmap {
    return when (action) {
        is SwipeActionSerializable.LaunchApp -> {
            try {
                val drawable = context.packageManager.getApplicationIcon(action.packageName)
                drawable.toBitmap(48, 48).asImageBitmap()
            } catch (_: Exception) {
                vectorToBitmap(context, R.drawable.ic_app_default)
            }
        }

        is SwipeActionSerializable.OpenUrl ->
            vectorToBitmap(context, R.drawable.ic_action_web)

        SwipeActionSerializable.NotificationShade ->
            vectorToBitmap(context, R.drawable.ic_action_notification)

        SwipeActionSerializable.ControlPanel ->
            vectorToBitmap(context, R.drawable.ic_action_grid)

        SwipeActionSerializable.OpenAppDrawer ->
            vectorToBitmap(context, R.drawable.ic_action_drawer)
    }
}

private fun tintBitmap(original: ImageBitmap, color: Color): ImageBitmap {
    val bitmap = Bitmap.createBitmap(
        original.width, original.height, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    val paint = android.graphics.Paint().apply {
        colorFilter = android.graphics.PorterDuffColorFilter(
            color.toArgb(),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }
    canvas.drawBitmap(original.asAndroidBitmap(), 0f, 0f, paint)
    return bitmap.asImageBitmap()
}


// Fallback: Create a simple colored square if all else fails
private fun createDefaultBitmap(): ImageBitmap {
    val bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.Gray.toArgb())
    return bitmap.asImageBitmap()
}

private fun vectorToBitmap(context: Context, drawableResId: Int): ImageBitmap {
    val drawable = ContextCompat.getDrawable(context, drawableResId) ?: return createDefaultBitmap()

    // Create fixed-size bitmap (48x48 for icons)
    val bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Scale vector to fit bitmap
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap.asImageBitmap()
}