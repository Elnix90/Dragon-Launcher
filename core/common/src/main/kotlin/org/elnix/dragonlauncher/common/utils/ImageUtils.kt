@file:Suppress("DEPRECATION")

package org.elnix.dragonlauncher.common.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Base64
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.withSave
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.serializables.CacheKey
import org.elnix.dragonlauncher.common.serializables.CustomIconSerializable
import org.elnix.dragonlauncher.common.serializables.IconShape
import org.elnix.dragonlauncher.common.serializables.IconType
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.serializables.toAppModel
import org.elnix.dragonlauncher.common.utils.Constants.Logging.ICONS_TAG
import org.elnix.dragonlauncher.common.utils.Constants.Logging.IMAGE_TAG
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logW
import java.io.ByteArrayOutputStream
import kotlin.math.ceil

object ImageUtils {

    fun loadBitmap(ctx: Context, uri: Uri): Bitmap {
        ctx.contentResolver.openInputStream(uri).use {
            return BitmapFactory.decodeStream(it!!)
        }
    }


    fun loadDrawableAsBitmap(
        drawable: Drawable,
        width: Int,
        height: Int,
        tint: Int? = null
    ): ImageBitmap {
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

        val imageBitmap = bitmap.asImageBitmap()

        // If tint is not unspecified (transparent)
        return tint?.takeIf { it != 0 }?.let {
            tintBitmap(imageBitmap, tint)
        } ?: imageBitmap
    }

    fun cropCenterSquare(src: Bitmap): Bitmap {
        val size = minOf(src.width, src.height)
        val left = (src.width - size) / 2
        val top = (src.height - size) / 2

        return Bitmap.createBitmap(src, left, top, size, size)
    }

    fun resize(src: Bitmap, size: Int): Bitmap =
        src.scale(size, size)


    fun base64ToImageBitmap(base64: String?): ImageBitmap? {
        return try {
            base64?.let {
                val bytes = Base64.decode(it, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                bitmap?.asImageBitmap()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String? {
        return try {
            val output = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            logE(IMAGE_TAG, e) { e.toString() }
            null
        }
    }

    fun uriToBase64(ctx: Context, uri: Uri): String? {
        return try {
            val bmp = loadBitmap(ctx, uri)
                .let(ImageUtils::cropCenterSquare)
                .let { resize(it, 256) }

            bitmapToBase64(bmp)
        } catch (e: Exception) {
            logE(IMAGE_TAG, e) { e.toString() }
            null
        }
    }

    @Suppress("Unused")
    fun imageBitmapToBase64(imageBitmap: ImageBitmap): String? {
        return try {
            val androidBitmap = imageBitmap.asAndroidBitmap()
            bitmapToBase64(androidBitmap)
        } catch (e: Exception) {
            logE(IMAGE_TAG, e) { e.toString() }
            null
        }
    }


    @Suppress("Unused")
    fun blurBitmap(ctx: Context, bitmap: Bitmap, radius: Float): Bitmap {
        if (radius <= 0f) return bitmap

        val scaleFactor = (25f - radius) / 25f.coerceAtLeast(0.1f)
        val scaledWidth = (bitmap.width * scaleFactor).toInt().coerceAtLeast(100)
        val scaledHeight = (bitmap.height * scaleFactor).toInt().coerceAtLeast(100)

        val scaledBitmap = bitmap.scale(scaledWidth, scaledHeight, false)
        val output = createBitmap(scaledWidth, scaledHeight)

        val rs = RenderScript.create(ctx)
        val input = Allocation.createFromBitmap(rs, scaledBitmap)
        val outputAlloc = Allocation.createFromBitmap(rs, output)

        val blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        blur.setRadius(radius.coerceIn(1f, 25f))
        blur.setInput(input)
        blur.forEach(outputAlloc)
        outputAlloc.copyTo(output)

        rs.destroy()
        input.destroy()
        outputAlloc.destroy()
        scaledBitmap.recycle()

        return output
    }


    fun textToBitmap(
        text: String,
        sizePx: Int,
        color: Int = 0xFFFFFFFF.toInt()
    ): ImageBitmap {
        val paint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
            textSize = sizePx.toFloat()
            this.color = color
            isSubpixelText = true
            isLinearText = true
        }

        val maxWidth = ceil(paint.measureText(text)).toInt().coerceAtLeast(1)

        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, maxWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setIncludePad(false)
            .build()

        val bitmap = createBitmap(
            layout.width.coerceAtLeast(1),
            layout.height.coerceAtLeast(1)
        )

        val canvas = Canvas(bitmap)
        layout.draw(canvas)

        return bitmap.asImageBitmap()
    }

    fun tintBitmap(original: ImageBitmap, color: Int): ImageBitmap {
        val bitmap = createBitmap(original.width, original.height)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            colorFilter = PorterDuffColorFilter(
                color,
                PorterDuff.Mode.SRC_IN
            )
        }
        canvas.drawBitmap(original.asAndroidBitmap(), 0f, 0f, paint)
        return bitmap.asImageBitmap()
    }

    fun createDefaultBitmap(
        width: Int,
        height: Int
    ): ImageBitmap {
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.Gray.toArgb())
        return bitmap.asImageBitmap()
    }

    fun Context.loadDrawableResAsBitmap(
        resId: Int,
        width: Int,
        height: Int
    ): ImageBitmap {
        val drawable = ContextCompat.getDrawable(this, resId)
            ?: return createDefaultBitmap(width, height)

        return loadDrawableAsBitmap(drawable, width, height)
    }

    fun createUntintedBitmap(
        action: SwipeActionSerializable,
        ctx: Context,
        icons: IconsCache<CacheKey>,
        width: Int,
        height: Int
    ): ImageBitmap {

        val pm = ctx.packageManager
        val pmCompat = PackageManagerCompat(pm, ctx)

        return with(ctx) {
            when (action) {
                is SwipeActionSerializable.LaunchApp -> {
                    val dummyAppModel = action.toAppModel()

                    val cacheKey = dummyAppModel.iconCacheKey
                    logD(ICONS_TAG) { "Searching in drawer cache (${icons.cacheUUID})\naction: $action\ndummyApp: $dummyAppModel\n cacheKey: $cacheKey" }

                    icons.getOrCompute(cacheKey) {

                        logW(ICONS_TAG) { "Did not find the cacheKey `$cacheKey` in drawer ${icons.cacheUUID}" }

                        val drawable = pmCompat.getAppIcon(
                            packageName = action.packageName,
                            userId = action.userId ?: 0,
                            isPrivateProfile = action.isPrivateSpace
                        )

                        loadDrawableAsBitmap(drawable, width, height)
                    }
                }

                is SwipeActionSerializable.LaunchShortcut -> {
                    loadShortcutIcon(
                        ctx = ctx,
                        packageName = action.packageName,
                        shortcutId = action.shortcutId,
                        widthPx = width,
                        heightPx = height
                    ) ?: loadDrawableResAsBitmap(
                        R.drawable.ic_action_pinned_shortcut,
                        width,
                        height
                    )
                }

                is SwipeActionSerializable.OpenUrl -> loadDrawableResAsBitmap(R.drawable.ic_action_web, width, height)

                SwipeActionSerializable.NotificationShade -> loadDrawableResAsBitmap(R.drawable.ic_action_notification, width, height)

                SwipeActionSerializable.ControlPanel -> loadDrawableResAsBitmap(R.drawable.ic_action_grid, width, height)

                is SwipeActionSerializable.OpenAppDrawer -> loadDrawableResAsBitmap(R.drawable.ic_action_drawer, width, height)

                is SwipeActionSerializable.OpenDragonLauncherSettings -> loadDrawableResAsBitmap(R.drawable.dragon_launcher_foreground, width, height)

                SwipeActionSerializable.Lock -> loadDrawableResAsBitmap(R.drawable.ic_action_lock, width, height)
                is SwipeActionSerializable.OpenFile -> loadDrawableResAsBitmap(R.drawable.ic_action_open_file, width, height)

                SwipeActionSerializable.ReloadApps -> loadDrawableResAsBitmap(R.drawable.ic_action_reload, width, height)

                SwipeActionSerializable.OpenRecentApps -> loadDrawableResAsBitmap(R.drawable.ic_action_recent, width, height)

                is SwipeActionSerializable.OpenCircleNest -> loadDrawableResAsBitmap(R.drawable.ic_action_target, width, height)

                SwipeActionSerializable.GoParentNest -> loadDrawableResAsBitmap(R.drawable.ic_icon_go_parent_nest, width, height)

                is SwipeActionSerializable.OpenWidget -> loadDrawableResAsBitmap(R.drawable.ic_action_widgets, width, height)

                is SwipeActionSerializable.RunAdbCommand -> loadDrawableResAsBitmap(R.drawable.adb_icon, width, height)

                is SwipeActionSerializable.ToggleBluetooth -> loadDrawableResAsBitmap(R.drawable.bluetooth_icon, width, height)

                is SwipeActionSerializable.ToggleData -> loadDrawableResAsBitmap(R.drawable.cellular_icon, width, height)

                is SwipeActionSerializable.ToggleWifi -> loadDrawableResAsBitmap(R.drawable.wifi_icon, width, height)
                SwipeActionSerializable.KillLauncher -> loadDrawableResAsBitmap(R.drawable.ic_action_kill, width, height)

                SwipeActionSerializable.None -> null
            } ?: loadDrawableResAsBitmap(R.drawable.ic_app_default, width, height)
        }
    }

    fun resolveCustomIconBitmap(
        base: ImageBitmap,
        icon: CustomIconSerializable,
        sizePx: Int,
        density: Density,
        iconShape: IconShape
    ): ImageBitmap {
        // Step 1: choose source bitmap (override or base)
        val sourceBitmap: ImageBitmap = when (icon.type) {
            IconType.BITMAP -> {
                icon.source
                    ?.let { base64ToImageBitmap(it) }
                    ?: base
            }

            IconType.ICON_PACK -> base
            IconType.TEXT -> {
                icon.source?.let {
                    textToBitmap(
                        text = it,
                        sizePx = sizePx
                    )
                } ?: base
            }

            IconType.PLAIN_COLOR -> icon.source?.let {
                try {
                    val sourceColor = it.toInt()
                    val bmp = createDefaultBitmap(sizePx, sizePx)
                    tintBitmap(bmp, sourceColor)
                } catch (_: Exception) {
                    base
                }
            } ?: base

            null -> base
        }

        // Step 2: prepare output bitmap
        val outBitmap = createBitmap(sizePx, sizePx)
        val canvas = Canvas(outBitmap)

        // Step 3 & 4: opacity & color tint
        val paint = Paint(
            Paint.ANTI_ALIAS_FLAG
        ).apply {
            alpha = ((icon.opacity ?: 1f)
                .coerceIn(0f, 1f) * 255).toInt()

            icon.tint?.let {
                colorFilter = PorterDuffColorFilter(
                    it,
                    PorterDuff.Mode.SRC_IN
                )
            }
        }

        canvas.withSave {
            // Step 7: transform (scale + rotation)
            val scaleX = icon.scaleX ?: 1f
            val scaleY = icon.scaleY ?: 1f
            val rotation = icon.rotationDeg ?: 0f

            val half = sizePx / 2f

            translate(half, half)
            rotate(rotation)
            scale(scaleX, scaleY)
            translate(-half, -half)


            clipToShape(icon, iconShape, sizePx, density)

            // Step 9: Save
            drawBitmap(
                sourceBitmap.asAndroidBitmap(),
                null,
                Rect(0, 0, sizePx, sizePx),
                paint
            )
        }

        return outBitmap.asImageBitmap()
    }

    private fun Canvas.clipToShape(
        icon: CustomIconSerializable,
        iconShape: IconShape,
        sizePx: Int,
        density: Density
    ) {
        // Step 8: CLip to shape
        val shape = (icon.shape ?: iconShape).resolveShape()

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
}
