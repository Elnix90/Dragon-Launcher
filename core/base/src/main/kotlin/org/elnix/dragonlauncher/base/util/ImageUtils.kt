package org.elnix.dragonlauncher.base.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import org.elnix.dragonlauncher.base.model.serializables.CustomIconProperties
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.resolveShape

public object ImageUtils {

//    public fun loadBitmap(ctx: Context, uri: Uri): Bitmap {
//        ctx.contentResolver.openInputStream(uri).use {
//            return BitmapFactory.decodeStream(it!!)
//        }
//    }

    public fun loadDrawableAsBitmap(
        drawable: Drawable,
        width: Int,
        height: Int
    ): Bitmap {
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)

        if (drawable is AdaptiveIconDrawable) {
            val bg = drawable.background
            val fg = drawable.foreground


            if (bg != null) {
                // Draw background first, scaled to full bounds
                bg.setBounds(0, 0, width, height)
                bg.draw(canvas)
            }

            if (fg != null) {
                // Draw foreground with inset scaling

                // THIS IS CRITICAL AND HANDLES HOW THE ICONS ARE DRAWN!
                val scale = 2f
                val inset = ((width - width / scale) / 2).toInt()
                fg.setBounds(-inset, -inset, width + inset, height + inset)
                fg.draw(canvas)
            }

            // Fallback if BOTH are null (yes, it happens)
            if (bg == null && fg == null) {
                drawable.setBounds(0, 0, width, height)
                drawable.draw(canvas)
            }

        } else {
            // Non-adaptive drawable
            drawable.setBounds(0, 0, width, height)
            drawable.draw(canvas)
        }

        return bitmap
    }

//    public fun cropCenterSquare(src: Bitmap): Bitmap {
//        val size = minOf(src.width, src.height)
//        val left = (src.width - size) / 2
//        val top = (src.height - size) / 2
//
//        return Bitmap.createBitmap(src, left, top, size, size)
//    }

//    public fun resize(src: Bitmap, size: Int): Bitmap =
//        src.scale(size, size)


//    public fun base64ToImageBitmap(base64: String?): ImageBitmap? {
//        return try {
//            base64?.let {
//                val bytes = Base64.decode(it, Base64.DEFAULT)
//                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//                bitmap?.asImageBitmap()
//            }
//        } catch (_: Exception) {
//            null
//        }
//    }

//    private fun bitmapToBase64(bitmap: Bitmap): String? {
//        return try {
//            val output = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
//            Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
//        } catch (e: Exception) {
//            logE(IMAGE_TAG, e) { e.toString() }
//            null
//        }
//    }

//    public fun uriToBase64(ctx: Context, uri: Uri): String? {
//        return try {
//            val bmp = loadBitmap(ctx, uri)
//                .let(ImageUtils::cropCenterSquare)
//                .let { resize(it, 256) }
//
//            bitmapToBase64(bmp)
//        } catch (e: Exception) {
//            logE(IMAGE_TAG, e) { e.toString() }
//            null
//        }
//    }

//    public fun imageBitmapToBase64(imageBitmap: ImageBitmap): String? {
//        return try {
//            val androidBitmap = imageBitmap.asAndroidBitmap()
//            bitmapToBase64(androidBitmap)
//        } catch (e: Exception) {
//            logE(IMAGE_TAG, e) { e.toString() }
//            null
//        }
//    }


//    public fun blurBitmap(ctx: Context, bitmap: Bitmap, radius: Float): Bitmap {
//        if (radius <= 0f) return bitmap
//
//        val scaleFactor = (25f - radius) / 25f.coerceAtLeast(0.1f)
//        val scaledWidth = (bitmap.width * scaleFactor).toInt().coerceAtLeast(100)
//        val scaledHeight = (bitmap.height * scaleFactor).toInt().coerceAtLeast(100)
//
//        val scaledBitmap = bitmap.scale(scaledWidth, scaledHeight, false)
//        val output = createBitmap(scaledWidth, scaledHeight)
//
//        val rs = RenderScript.create(ctx)
//        val input = Allocation.createFromBitmap(rs, scaledBitmap)
//        val outputAlloc = Allocation.createFromBitmap(rs, output)
//
//        val blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
//        blur.setRadius(radius.coerceIn(1f, 25f))
//        blur.setInput(input)
//        blur.forEach(outputAlloc)
//        outputAlloc.copyTo(output)
//
//        rs.destroy()
//        input.destroy()
//        outputAlloc.destroy()
//        scaledBitmap.recycle()
//
//        return output
//    }

//
//    public fun textToBitmap(
//        text: String,
//        sizePx: Int,
//        color: Int = 0xFFFFFFFF.toInt()
//    ): ImageBitmap {
//        val paint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
//            textSize = sizePx.toFloat()
//            this.color = color
//            isSubpixelText = true
//            isLinearText = true
//        }
//
//        val maxWidth = ceil(paint.measureText(text)).toInt().coerceAtLeast(1)
//
//        val layout = StaticLayout.Builder
//            .obtain(text, 0, text.length, paint, maxWidth)
//            .setAlignment(Layout.Alignment.ALIGN_CENTER)
//            .setIncludePad(false)
//            .build()
//
//        val bitmap = createBitmap(
//            layout.width.coerceAtLeast(1),
//            layout.height.coerceAtLeast(1)
//        )
//
//        val canvas = Canvas(bitmap)
//        layout.draw(canvas)
//
//        return bitmap.asImageBitmap()
//    }

//    public fun Bitmap.tintedWith(color: Int?): Bitmap {
//        if (color == null) return this
//        val bitmap = createBitmap(width, height)
//        val canvas = Canvas(bitmap)
//        val paint = Paint().apply {
//            this.colorFilter = PorterDuffColorFilter(
//                color,
//                PorterDuff.Mode.SRC_IN
//            )
//        }
//        canvas.drawBitmap(bitmap, 0f, 0f, paint)
//        return bitmap
//    }

    private fun createDefaultBitmap(
        width: Int,
        height: Int
    ): Bitmap {
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.Gray.toArgb())
        return bitmap
    }

//    public fun Context.loadDrawableResAsBitmap(
//        resId: Int,
//        width: Int,
//        height: Int
//    ): Bitmap {
//        val drawable = ContextCompat.getDrawable(this, resId)
//            ?: return createDefaultBitmap(width, height)
//
//        return loadDrawableAsBitmap(drawable, width, height)
//    }


    public fun Context.loadDrawableResAsImageBitmap(
        resId: Int,
        width: Int,
        height: Int
    ): ImageBitmap {
        val drawable = ContextCompat.getDrawable(this, resId)
            ?: return createDefaultBitmap(width, height).asImageBitmap()

        return loadDrawableAsBitmap(drawable, width, height).asImageBitmap()
    }


//    public fun createUntintedBitmap(
//        action: Action,
//        ctx: Context,
//        width: Int,
//        height: Int
//    ): Bitmap {
//        return with(ctx) {
//            when (action) {
//                is Action.LaunchApp -> {
//                    null
////                    val dummyApplication = action.toApplication()
////
////                    val cacheKey = dummyApplication.key
////                    logD(ICONS_TAG) { "Searching in drawer cache (${icons.cacheUUID})\naction: $action\ndummyApp: $dummyApplication\n cacheKey: $cacheKey" }
////
////                    icons.getOrCompute(cacheKey) {
////
////                        logW(ICONS_TAG) { "Did not find the cacheKey `$cacheKey` in drawer ${icons.cacheUUID}" }
////
////                        val drawable = pmCompat.getAppIcon(
////                            packageName = action.packageName,
////                            userId = action.userId ?: 0,
////                            isPrivate = action.isPrivateSpace
////                        )
////
////                        loadDrawableAsBitmap(drawable, width, height)
////                    }
//                }
//
//                is Action.LaunchShortcut -> {
////                    loadShortcutIcon(
////                        ctx = ctx,
////                        packageName = action.packageName,
////                        shortcutId = action.shortcutId,
////                        widthPx = width,
////                        heightPx = height
////                    ) ?: loadDrawableResAsBitmap(
////                        R.drawable.ic_action_pinned_shortcut,
////                        width,
////                        height
////                    )
//                    null
//                }
//
//                is Action.OpenUrl -> loadDrawableResAsBitmap(R.drawable.web, width, height)
//
//                Action.NotificationShade -> loadDrawableResAsBitmap(R.drawable.notification, width, height)
//
//                Action.ControlPanel -> loadDrawableResAsBitmap(R.drawable.ic_action_grid, width, height)
//
//                is Action.OpenAppDrawer -> loadDrawableResAsBitmap(R.drawable.ic_action_drawer, width, height)
//
//                is Action.OpenDragonLauncherSettings -> loadDrawableResAsBitmap(R.drawable.dragon_launcher_foreground, width, height)
//
//                Action.Lock -> loadDrawableResAsBitmap(R.drawable.ic_action_lock, width, height)
//                is Action.OpenFile -> loadDrawableResAsBitmap(R.drawable.ic_action_open_file, width, height)
//
//                Action.ReloadApps -> loadDrawableResAsBitmap(R.drawable.ic_action_reload, width, height)
//
//                Action.OpenRecentApps -> loadDrawableResAsBitmap(R.drawable.ic_action_recent, width, height)
//
//                is Action.OpenCircleNest -> loadDrawableResAsBitmap(R.drawable.ic_action_target, width, height)
//
//                Action.GoParentNest -> loadDrawableResAsBitmap(R.drawable.fullscreen_exit, width, height)
//
//                is Action.OpenWidget -> loadDrawableResAsBitmap(R.drawable.ic_action_widgets, width, height)
//
//                is Action.RunAdbCommand -> loadDrawableResAsBitmap(R.drawable.adb_icon, width, height)
//
//                is Action.ToggleBluetooth -> loadDrawableResAsBitmap(R.drawable.bluetooth, width, height)
//
//                is Action.ToggleData -> loadDrawableResAsBitmap(R.drawable.cellular_icon, width, height)
//
//                is Action.ToggleWifi -> loadDrawableResAsBitmap(R.drawable.wifi, width, height)
//                Action.KillLauncher -> loadDrawableResAsBitmap(R.drawable.ic_action_kill, width, height)
//
//                Action.None -> null
//            } ?: loadDrawableResAsBitmap(R.drawable.ic_app_default, width, height)
//        }
//    }

//    public fun resolveCustomIconProperties(
//        base: Bitmap,
//        properties: CustomIconProperties,
//        sizePx: Int,
//        density: Density,
//        iconShape: IconShape
//    ): Bitmap {
//        // Step 1: choose source bitmap (override or base)
//        val sourceBitmap: ImageBitmap = when (icon.type) {
//            IconType.BITMAP -> {
//                icon.source
//                    ?.let { base64ToImageBitmap(it) }
//                    ?: base
//            }
//
//            IconType.ICON_PACK -> base
//            IconType.TEXT -> {
//                icon.source?.let {
//                    textToBitmap(
//                        text = it,
//                        sizePx = sizePx
//                    )
//                } ?: base
//            }
//
//            IconType.PLAIN_COLOR -> icon.source?.let {
//                try {
//                    val sourceColor = it.toInt()
//                    val bmp = createDefaultBitmap(sizePx, sizePx)
//                    bmp.tintedWith(sourceColor)
//                } catch (_: Exception) {
//                    base
//                }
//            } ?: base
//
//            null -> base
//        }

        // Step 2: prepare output bitmap
//        val outBitmap = createBitmap(sizePx, sizePx)
//        val canvas = Canvas(outBitmap)
//
//        // Step 3 & 4: opacity & color tint
//        val paint = Paint(
//            Paint.ANTI_ALIAS_FLAG
//        ).apply {
//            alpha = (properties.opacity
//                .coerceIn(0f, 1f) * 255).toInt()
//
//            properties.tint?.let {
//                colorFilter = PorterDuffColorFilter(
//                    it.toArgb(),
//                    PorterDuff.Mode.SRC_IN
//                )
//            }
//        }
//
//        canvas.withSave {
//            // Step 7: transform (scale + rotation)
//            val scaleX = properties.scaleX
//            val scaleY = properties.scaleY
//            val rotation = properties.rotationDeg
//
//            val half = sizePx / 2f
//
//            translate(half, half)
//            rotate(rotation.toFloat())
//            scale(scaleX, scaleY)
//            translate(-half, -half)
//
//
//            clipToShape(properties, iconShape, sizePx, density)
//
//            // Step 9: Save
//            drawBitmap(
//                base,
//                null,
//                Rect(0, 0, sizePx, sizePx),
//                paint
//            )
//        }
//
//        return outBitmap
//    }
}

/**
 * Clips the canvas to the [properties] shape, falling back to [iconShape] when
 * the properties do not override it. Applied per icon before drawing.
 */
public fun Canvas.clipToShape(
    properties: CustomIconProperties,
    iconShape: IconShape,
    sizePx: Int,
    density: Density
) {
    val shape = (properties.shape ?: iconShape).resolveShape()

    val outline = shape.createOutline(
        size = Size(sizePx.toFloat(), sizePx.toFloat()),
        layoutDirection = LayoutDirection.Ltr,
        density = density
    )

    when (outline) {

        is Outline.Rectangle -> {
            clipRect(
                0f,
                0f,
                sizePx.toFloat(),
                sizePx.toFloat()
            )
        }

        is Outline.Rounded -> {
            val rr = outline.roundRect

            val path = Path().apply {
                addRoundRect(
                    rr.left,
                    rr.top,
                    rr.right,
                    rr.bottom,
                    rr.topLeftCornerRadius.x,
                    rr.topLeftCornerRadius.y,
                    Path.Direction.CW
                )
            }

            clipPath(path)
        }

        is Outline.Generic -> {
            val path = outline.path.asAndroidPath()
            clipPath(path)
        }
    }
}