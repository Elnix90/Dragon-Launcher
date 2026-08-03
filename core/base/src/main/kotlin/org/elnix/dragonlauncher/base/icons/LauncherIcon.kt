package org.elnix.dragonlauncher.base.icons

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.model.models.IconSettings
import org.elnix.dragonlauncher.ktx.drawWithColorFilter
import java.lang.ref.WeakReference

public sealed interface LauncherIcon



public data class StaticLauncherIcon(
    val foregroundLayer: LauncherIconLayer,
    val backgroundLayer: LauncherIconLayer,
) : LauncherIcon {
    private var cachedBitmap: WeakReference<Bitmap>? = null
    private var cachedSize: Int? = null
    private var cachedSettings: IconSettings? = null
    private var renderSemaphore = Semaphore(1)

    public fun getCachedBitmap(size: Int, settings: IconSettings): Bitmap? {
        return if (cachedSettings == settings &&  cachedSize == size) cachedBitmap?.get() else null
    }

    /**
     * Render this icon to a bitmap.
     */
    public suspend fun render(size: Int, settings: IconSettings): Bitmap {
        val cachedBmp = cachedBitmap?.get()
        if (cachedSettings == settings && cachedSize == size && cachedBmp != null) return cachedBmp
        val bmp = withContext(Dispatchers.Default) {
            renderSemaphore.withPermit {
                if (settings.renderForeground || settings.renderBackground) {
                    val bmp =
                        if (cachedBmp == null || cachedBmp.width != size || cachedBmp.height != size) {
                            createBitmap(size, size)
                        } else cachedBmp
                    val canvas = Canvas(bmp)
                    canvas.drawRect(
                        Rect(0, 0, canvas.width, canvas.height), Paint().apply {
                            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                        })

                    if (settings.renderBackground) {
                        renderLayer(canvas, backgroundLayer)
                    }
                    if (settings.renderForeground) {
                        renderLayer(canvas, foregroundLayer)
                    }

                    cachedBitmap = WeakReference(bmp)
                    cachedSettings = settings
                    cachedSize = size
                    bmp
                } else {
                    createBitmap(1,1)
                }
            }
        }
        return bmp
    }

    private fun renderLayer(canvas: Canvas, layer: LauncherIconLayer) {
        when (layer) {
            is ColorLayer -> {
                val paint = Paint()
                paint.color = layer.tint
                canvas.drawRect(Rect(0, 0, canvas.width, canvas.height), paint)
            }

            is StaticIconLayer -> {
                canvas.withScale(
                    layer.scale,
                    layer.scale,
                    canvas.width / 2f,
                    canvas.height / 2f,
                ) {
                    layer.icon.bounds = Rect(0, 0, canvas.width, canvas.height)
                    if (layer.tint != null) {
                        layer.icon.drawWithColorFilter(
                            canvas,
                            PorterDuffColorFilter(layer.tint, PorterDuff.Mode.SRC_IN)
                        )
                    } else {
                        layer.icon.draw(canvas)
                    }
                }
            }

            else -> {}
        }
    }
}

public interface DynamicLauncherIcon : LauncherIcon {
    public suspend fun getIcon(time: Long): StaticLauncherIcon
}