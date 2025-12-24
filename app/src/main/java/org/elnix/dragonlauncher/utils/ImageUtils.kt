@file:Suppress("DEPRECATION")

package org.elnix.dragonlauncher.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

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


    fun base64ToImageBitmap(base64: String?): androidx.compose.ui.graphics.ImageBitmap? {
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

}
