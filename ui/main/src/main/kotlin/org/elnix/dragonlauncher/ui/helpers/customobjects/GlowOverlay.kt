@file:Suppress("NOTHING_TO_INLINE")

package org.elnix.dragonlauncher.ui.helpers.customobjects

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.Constants
import org.elnix.dragonlauncher.base.DragonCache
import org.elnix.dragonlauncher.base.model.serializables.CustomGlow
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Composable
fun GlowOverlay(
	center: Offset,
	progress: Float
) {
	val primaryColor = MaterialTheme.colorScheme.primary

	Canvas(
		Modifier
			.fillMaxSize()
			.scale(progress)
	) {
		val glowRadius = Constants.Settings.HOVER_GRADIENT_RADIUS.toPx()

		drawIntoCanvas { canvas ->
			val paint = customGlowPaint(primaryColor, glowRadius)
			canvas.nativeCanvas.drawCircle(
				center.x,
				center.y,
				glowRadius,
				paint
			)
		}
	}
}

fun DrawScope.drawNeonGlowLine(
	start: Offset,
	end: Offset,
	color: Color,
	lineStrokeWidth: Float,
	glow: CustomGlow?,
	erase: Boolean,
	eraseColor: Color?
) {
	glowLine(glow, color, start, end)
	line(lineStrokeWidth, erase, eraseColor, start, end, color)
}

private fun DrawScope.line(
	lineStrokeWidth: Float,
	erase: Boolean,
	eraseColor: Color?,
	start: Offset,
	end: Offset,
	color: Color
) {
	val width = lineStrokeWidth.dp.toPxOrNull(this) ?: return
	if (erase) {
		drawLine(
			color = eraseColor ?: Color.Transparent,
			start = start,
			end = end,
			strokeWidth = width,
			cap = StrokeCap.Round,
			blendMode = BlendMode.Dst
		)
	}

	drawLine(
		color = color,
		start = start,
		end = end,
		strokeWidth = width,
		cap = StrokeCap.Round
	)
}

private inline fun DrawScope.glowLine(
	glow: CustomGlow?,
	color: Color,
	start: Offset,
	end: Offset
) {
	if (glow == null) return
	val glowRadius = glow.radius?.toPxOrNull(this) ?: return
	val glowColor = glow.color ?: color

	drawIntoCanvas { canvas ->
		val paint = customGlowPaint(glowColor, glowRadius)

		canvas.nativeCanvas.drawLine(
			start.x,
			start.y,
			end.x,
			end.y,
			paint
		)
	}
}

fun DrawScope.drawPathGlow(
	path: Path,
	color: Color,
	lineStrokeWidth: Dp,
	glow: CustomGlow?,
	erase: Boolean,
	eraseColor: Color?,
	cacheKey: Any? = null
) {
	glow(glow, path, color, cacheKey)
	if (erase) erasePath(path, lineStrokeWidth, eraseColor)
	path(lineStrokeWidth, path, color)
}

inline fun DrawScope.erasePath(
	path: Path,
	lineStrokeWidth: Dp,
	eraseColor: Color?
) {
	val color = eraseColor ?: Color.Transparent
	drawPath(
		path = path,
		color = color,
		style = Fill,
		blendMode = BlendMode.Src
	)

	val style =
		if (lineStrokeWidth.value > 0f) {
			Stroke(lineStrokeWidth.toPx(), cap = StrokeCap.Round)
		} else {
			return
		}
	drawPath(
		path = path,
		color = color,
		style = style,
		blendMode = BlendMode.Src
	)
}

private inline fun DrawScope.glow(
	glow: CustomGlow?,
	path: Path,
	color: Color,
	cacheKey: Any?
) {
	val glowRadius = glow?.radius.toPxOrNull(this) ?: return
	val glowColor = glow.color ?: color
	val cached = cacheKey?.let { PathGlowCache.get(it, path, glowColor, glowRadius) }
	if (cached != null) {
		drawImage(cached.image, topLeft = cached.offset)
	} else {
		drawIntoCanvas { canvas ->
			val paint = customGlowPaint(glowColor, glowRadius)
			canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
		}
	}
}

private data class PathGlowKey(
	val geometry: Any,
	val color: Color,
	val radius: Float
)

private data class CachedPathGlow(
	val image: androidx.compose.ui.graphics.ImageBitmap,
	val offset: Offset
)

private const val PATH_GLOW_CACHE_MAX_BYTES = 64 * 1024 * 1024
private const val PATH_GLOW_CACHE_MAX_ENTRY_BYTES = 24 * 1024 * 1024

/** Reuses static point/shape glow rasterizations; dynamic paths are uncached. */
private object PathGlowCache :
	DragonCache<PathGlowKey, CachedPathGlow>(
		initialMaxSize = PATH_GLOW_CACHE_MAX_BYTES,
		sizeOf = { _, value ->
			(value.image.width.toLong() * value.image.height * 4).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
		}
	) {
	fun get(geometry: Any, path: Path, color: Color, radius: Float): CachedPathGlow? {
		val key = PathGlowKey(geometry, color, radius)
		get(key)?.let { return it }

		val bounds = path.getBounds()
		val padding = kotlin.math.ceil(radius.toDouble() * 3)
		val width = kotlin.math
			.ceil(bounds.width.toDouble() + padding * 2)
			.toLong()
			.coerceAtLeast(1L)
		val height = kotlin.math
			.ceil(bounds.height.toDouble() + padding * 2)
			.toLong()
			.coerceAtLeast(1L)
		val maxPixels = PATH_GLOW_CACHE_MAX_ENTRY_BYTES / 4L
		if (height > maxPixels || width > maxPixels / height) return null

		return getOrCompute(key) {
			val bitmap = createBitmap(width.toInt(), height.toInt())
			val canvas = android.graphics.Canvas(bitmap)
			canvas.translate(padding.toFloat() - bounds.left, padding.toFloat() - bounds.top)
			canvas.drawPath(path.asAndroidPath(), customGlowPaint(color, radius))
			CachedPathGlow(
				image = bitmap.asImageBitmap(),
				offset = Offset(bounds.left - padding.toFloat(), bounds.top - padding.toFloat())
			)
		}
	}
}

internal suspend fun prewarmPathGlows(paths: List<Pair<Any, Path>>, color: Color, radius: Float) {
	withContext(Dispatchers.Default) {
		paths.forEach { (geometry, path) -> PathGlowCache.get(geometry, path, color, radius) }
	}
}

private inline fun DrawScope.path(lineStrokeWidth: Dp, path: Path, color: Color) {
	val style =
		when {
			lineStrokeWidth.value == -1f -> return
			lineStrokeWidth.value < 0f -> Fill
			lineStrokeWidth.value == 0.0f -> Stroke(Stroke.HairlineWidth, cap = StrokeCap.Round)
			else -> Stroke(lineStrokeWidth.toPx(), cap = StrokeCap.Round)
		}

	drawPath(
		path = path,
		color = color,
		style = style
	)
}

private inline fun customGlowPaint(
	glowColor: Color,
	glowPx: Float
): Paint =
	PaintCache.getOrCompute(glowColor to glowPx) {
		Paint().apply {
			this.color = glowColor.copy(alpha = 0.7f).toArgb()
			style = Paint.Style.STROKE
			strokeWidth = glowPx
			maskFilter =
				BlurMaskFilter(
					glowPx,
					BlurMaskFilter.Blur.NORMAL
				)
			isAntiAlias = true
		}
	}

/**
 * A [DragonCache] instance to cache [Paint] instances and avoid recomputing them all the time
 *
 * Normally in the code I use:
 *  - `1+` or more paints for the points glow (usually only one (by default))
 *  - `1+` or more paints for the intersection shapes (usually only one (by default))
 *  - `1` paint for the red cross in the middle of the nests
 *  - `1` paint for the primary colored overlay when merging points in points settings
 *  - `360 * 4 = 1440` potential glows for the line, if user uses lots of different angles, with the color etc...
 *
 *  This makes a total **about < 1500**. So the cache size will be 1500. hopefully that works and helps to cache all the requested paints.
 */
private object PaintCache : DragonCache<Pair<Color, Float>, Paint>(1500)

/**
 * Converts the [Dp] to pixels only if it is higher than 0 or returns `null`
 * @param density the [Density] to pass from the drawscope
 * @return the [value] in pixels of `null`
 */
@OptIn(ExperimentalContracts::class)
inline fun Dp?.toPxOrNull(density: Density): Float? {
	contract {
		returnsNotNull() implies (this@toPxOrNull != null)
	}

	if (this == null) return null
	if (this.isUnspecified) return null
	if (this.value <= 0) return null

	return with(density) { toPx() }
}
