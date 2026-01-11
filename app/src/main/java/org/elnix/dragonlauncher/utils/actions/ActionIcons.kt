package org.elnix.dragonlauncher.utils.actions

import android.content.Context
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withSave
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.data.helpers.CustomIconSerializable
import org.elnix.dragonlauncher.data.helpers.IconType
import org.elnix.dragonlauncher.data.targetPackage
import org.elnix.dragonlauncher.ui.theme.LocalExtraColors
import org.elnix.dragonlauncher.utils.ImageUtils
import org.elnix.dragonlauncher.utils.PackageManagerCompat
import org.elnix.dragonlauncher.utils.loadShortcutIcon

@Composable
fun appIcon(
    packageName: String,
    icons: Map<String, ImageBitmap>? = null
): Painter {
    val cached = icons?.get(packageName)
    return if (cached != null) {
        BitmapPainter(cached)
    } else {
        painterResource(R.drawable.ic_app_default)
    }
}


@Composable
fun ActionIcon(
    action: SwipeActionSerializable,
    icons: Map<String, ImageBitmap>,
    modifier: Modifier = Modifier,
    size: Int = 64,
    showLaunchAppVectorGrid: Boolean = false
) {
    val ctx = LocalContext.current
    val extraColors = LocalExtraColors.current


    val bitmap: ImageBitmap? = when {
        action is SwipeActionSerializable.LaunchApp && showLaunchAppVectorGrid ->
            loadDrawableResAsBitmap(ctx, R.drawable.ic_app_grid, size, size)
        else -> {
            createUntintedBitmap(
                icons = icons,
                action = action,
                ctx = ctx,
                width = size,
                height = size
            )
        }
    }

    if (bitmap == null) return


    Image(
        bitmap = bitmap,
        contentDescription = null,
        colorFilter = if (
            action !is SwipeActionSerializable.LaunchApp &&
            action !is SwipeActionSerializable.LaunchShortcut &&
            action !is SwipeActionSerializable.OpenDragonLauncherSettings
        ) ColorFilter.tint(actionColor(action, extraColors))
        else null,
        modifier = modifier
    )
}

fun resolveCustomIconBitmap(
    base: ImageBitmap,
    icon: CustomIconSerializable,
    sizePx: Int
): ImageBitmap {
    // Step 1: choose source bitmap (override or base)
    val sourceBitmap: ImageBitmap = when (icon.type) {
        IconType.BITMAP,
        IconType.ICON_PACK -> {
            icon.source
                ?.let { ImageUtils.base64ToImageBitmap(it) }
                ?: base
        }

        IconType.TEXT -> {
            icon.source?.let {
                ImageUtils.textToBitmap(
                    text = it,
                    sizePx = sizePx
                )
            } ?: base
        }

        IconType.PLAIN_COLOR -> icon.source?.let {
            try {
                val sourceColor = Color(it.toInt())
                val bmp = createDefaultBitmap(sizePx, sizePx)
                tintBitmap(bmp, sourceColor)
            } catch (_: Exception) {
                base
            }
        } ?: base

        IconType.SHAPE,
        null -> base
    }

    // Step 2: prepare output bitmap
    val outBitmap = createBitmap(sizePx, sizePx)
    val canvas = Canvas(outBitmap)

    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

    // Step 3: opacity
    paint.alpha = ((icon.opacity ?: 1f).coerceIn(0f, 1f) * 255).toInt()

    // Step 4: color tint
    icon.tint?.let {
        paint.colorFilter = android.graphics.PorterDuffColorFilter(
            it.toInt(),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }

    // Step 5: blend mode (best-effort)
    icon.blendMode?.let {
        paint.xfermode = when (it.uppercase()) {
            "MULTIPLY" -> android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.MULTIPLY)
            "SCREEN" -> android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SCREEN)
            "OVERLAY" -> android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.OVERLAY)
            else -> null
        }
    }

    // Step 6: shadow
    if (icon.shadowRadius != null) {
        paint.setShadowLayer(
            icon.shadowRadius,
            icon.shadowOffsetX ?: 0f,
            icon.shadowOffsetY ?: 0f,
            (icon.shadowColor ?: 0x55000000).toInt()
        )
    }

    // Step 7: transform (scale + rotation)
    canvas.withSave {

        val scaleX = icon.scaleX ?: 1f
        val scaleY = icon.scaleY ?: 1f
        val rotation = icon.rotationDeg ?: 0f

        canvas.translate(sizePx / 2f, sizePx / 2f)
        canvas.rotate(rotation)
        canvas.scale(scaleX, scaleY)
        canvas.translate(-sizePx / 2f, -sizePx / 2f)

        // Step 8: draw bitmap
        canvas.drawBitmap(
            sourceBitmap.asAndroidBitmap(),
            null,
            android.graphics.Rect(0, 0, sizePx, sizePx),
            paint
        )

    }

    // Step 9: stroke (rectangular, corner clipping is UI-level)
    if (icon.strokeWidth != null && icon.strokeColor != null) {
        val strokePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = icon.strokeWidth
            color = icon.strokeColor.toInt()
        }
        canvas.drawRect(
            0f,
            0f,
            sizePx.toFloat(),
            sizePx.toFloat(),
            strokePaint
        )
    }

    return outBitmap.asImageBitmap()
}


fun createUntintedBitmap(
    action: SwipeActionSerializable,
    ctx: Context,
    icons: Map<String, ImageBitmap>,
    width: Int,
    height: Int
): ImageBitmap {
    val pm = ctx.packageManager
    val pmCompat = PackageManagerCompat(pm, ctx)

    return when (action) {
        is SwipeActionSerializable.LaunchApp,
        is SwipeActionSerializable.LaunchShortcut -> {
            val pkg = action.targetPackage()!!

            if (action is SwipeActionSerializable.LaunchShortcut) {
                val shortcutIcon = loadShortcutIcon(ctx, pkg, action.shortcutId)
                if (shortcutIcon != null) return shortcutIcon
            }

            pmCompat.getAppIcon(pkg, 0)
            icons[pkg] ?: loadDrawableResAsBitmap(ctx, R.drawable.ic_app_default, width, height)

        }

        is SwipeActionSerializable.OpenUrl ->
            loadDrawableResAsBitmap(ctx, R.drawable.ic_action_web, width, height)

        SwipeActionSerializable.NotificationShade ->
            loadDrawableResAsBitmap(ctx, R.drawable.ic_action_notification, width, height)

        SwipeActionSerializable.ControlPanel ->
            loadDrawableResAsBitmap(ctx, R.drawable.ic_action_grid, width, height)

        SwipeActionSerializable.OpenAppDrawer ->
            loadDrawableResAsBitmap(ctx, R.drawable.ic_action_drawer, width, height)

        SwipeActionSerializable.OpenDragonLauncherSettings ->
            loadDrawableResAsBitmap(ctx, R.drawable.dragon_launcher_foreground, width, height)

        SwipeActionSerializable.Lock -> loadDrawableResAsBitmap(ctx, R.drawable.ic_action_lock, width, height)
        is SwipeActionSerializable.OpenFile -> loadDrawableResAsBitmap(ctx, R.drawable.ic_action_open_file, width, height)
        SwipeActionSerializable.ReloadApps -> loadDrawableResAsBitmap(ctx, R.drawable.ic_action_reload, width, height)
        SwipeActionSerializable.OpenRecentApps -> loadDrawableResAsBitmap(ctx, R.drawable.ic_action_recent, width, height)
        is SwipeActionSerializable.OpenCircleNest -> loadDrawableResAsBitmap(ctx, R.drawable.ic_action_target, width, height)
        SwipeActionSerializable.GoParentNest -> loadDrawableResAsBitmap(ctx, R.drawable.ic_icon_go_parent_nest, width, height)
        is SwipeActionSerializable.OpenWidget -> loadDrawableResAsBitmap(ctx, R.drawable.ic_action_widgets, width, height)
    }
}


private fun tintBitmap(original: ImageBitmap, color: Color): ImageBitmap {
    val bitmap = createBitmap(original.width, original.height)
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
fun createDefaultBitmap(
    width: Int,
    height: Int
): ImageBitmap {
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.Gray.toArgb())
    return bitmap.asImageBitmap()
}


fun loadDrawableResAsBitmap(
    context: Context,
    resId: Int,
    width: Int,
    height: Int
): ImageBitmap {
    val drawable = ContextCompat.getDrawable(context, resId)
        ?: return createDefaultBitmap(width, height)

    return loadDrawableAsBitmap(drawable, width, height)
}

@Suppress("SameParameterValue")
fun loadDrawableAsBitmap(
    drawable: android.graphics.drawable.Drawable,
    width: Int,
    height: Int
): ImageBitmap {
    // Adaptive icon support
    val bmp = if (drawable is android.graphics.drawable.AdaptiveIconDrawable) {
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    } else {
        drawable.toBitmap(width, height)
    }
    return bmp.asImageBitmap()
}
