package org.elnix.dragonlauncher.base.icons

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.model.models.IconSettings
import org.elnix.dragonlauncher.base.model.serializables.CustomIconProperties
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.util.clipToShape
import org.elnix.dragonlauncher.ktx.drawWithColorFilter
import java.lang.ref.WeakReference

public sealed interface LauncherIcon


public data class StaticLauncherIcon(
    val foregroundLayer: LauncherIconLayer,
    val backgroundLayer: LauncherIconLayer,

    /** Customization properties applied at render time. Defaults to empty (no-op). */
    val properties: CustomIconProperties = CustomIconProperties(),
) : LauncherIcon {
    private var cachedBitmap: WeakReference<Bitmap>? = null
    private var cachedSize: Int? = null
    private var cachedSettings: IconSettings? = null
    private var cachedProperties: CustomIconProperties? = null
    private var renderSemaphore = Semaphore(1)

    public fun getCachedBitmap(size: Int, settings: IconSettings): Bitmap? {
        return if (cachedSettings == settings && cachedSize == size && cachedProperties == properties) {
            cachedBitmap?.get()
        } else null
    }

    /**
     * Render this icon to a bitmap.
     *
     * Applies [properties] (opacity, tint, rotation, scale and shape) on a single
     * clean canvas. The fast path (empty properties) keeps the previous behavior.
     */
    public suspend fun render(size: Int, settings: IconSettings): Bitmap {
        val cachedBmp = cachedBitmap?.get()
        if (cachedSettings == settings && cachedSize == size && cachedProperties == properties && cachedBmp != null) {
            return cachedBmp
        }
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

                    val hasProperties = properties.isNotEmpty
                    val saved = if (hasProperties) {
                        applyPropertyCanvasState(canvas, size)
                    } else false

                    val propertyPaint = properties.tint?.let {
                        Paint().apply {
                            colorFilter = PorterDuffColorFilter(it.toArgb(), PorterDuff.Mode.SRC_IN)
                        }
                    }

                    if (settings.renderBackground) {
                        renderLayer(canvas, backgroundLayer, propertyPaint)
                    }
                    if (settings.renderForeground) {
                        renderLayer(canvas, foregroundLayer, propertyPaint)
                    }

                    if (saved) {
                        canvas.restore()
                    }

                    cachedBitmap = WeakReference(bmp)
                    cachedSettings = settings
                    cachedSize = size
                    cachedProperties = properties
                    bmp
                } else {
                    createBitmap(1, 1)
                }
            }
        }
        return bmp
    }

    /**
     * Applies opacity (via a layer), then rotation/scale around the center and
     * the per-icon shape clip. All applied before drawing, in a single canvas.
     *
     * @return true when a canvas layer was saved and must be restored by the caller
     */
    private fun applyPropertyCanvasState(canvas: Canvas, size: Int): Boolean {
        val opacity = properties.opacity?.coerceIn(0f, 1f) ?: 1f
        val needsTransform = properties.rotationDeg != null ||
                properties.scaleX != null ||
                properties.scaleY != null
        val needsClip = properties.shape != null

        val saved = if (opacity < 1f) {
            canvas.saveLayerAlpha(0f, 0f, size.toFloat(), size.toFloat(), (opacity * 255).toInt())
            true
        } else if (needsTransform || needsClip) {
            canvas.save()
            true
        } else {
            false
        }

        if (needsTransform) {
            val half = size / 2f
            canvas.translate(half, half)
            canvas.rotate(properties.rotationDeg?.toFloat() ?: 0f)
            canvas.scale(properties.scaleX ?: 1f, properties.scaleY ?: 1f)
            canvas.translate(-half, -half)
        }

        if (needsClip) {
            canvas.clipToShape(properties, IconShape.PlatformDefault, size, Density(1f))
        }

        return saved
    }

    private fun renderLayer(canvas: Canvas, layer: LauncherIconLayer, propertyPaint: Paint?) {
        when (layer) {
            is ColorLayer -> {
                val paint = Paint()
                paint.color = layer.tint
                propertyPaint?.colorFilter?.let { paint.colorFilter = it }
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
                    val filter = propertyPaint?.colorFilter
                        ?: layer.tint?.let { PorterDuffColorFilter(it, PorterDuff.Mode.SRC_IN) }
                    if (filter != null) {
                        layer.icon.drawWithColorFilter(canvas, filter)
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
