@file:Suppress("DEPRECATION")

package org.elnix.dragonlauncher.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import org.elnix.dragonlauncher.utils.logs.logE
import java.io.ByteArrayOutputStream
import kotlin.math.ceil

object ImageUtils {

    fun loadBitmap(ctx: Context, uri: Uri): Bitmap {
        ctx.contentResolver.openInputStream(uri).use {
            return BitmapFactory.decodeStream(it!!)
        }
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
            logE(IMAGE_TAG, e.toString())
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
            logE(IMAGE_TAG, e.toString())
            null
        }
    }

    fun imageBitmapToBase64(imageBitmap: ImageBitmap): String? {
        return try {
            val androidBitmap = imageBitmap.asAndroidBitmap()
            bitmapToBase64(androidBitmap)
        } catch (e: Exception) {
            logE(IMAGE_TAG, e.toString())
            null
        }
    }


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
}
