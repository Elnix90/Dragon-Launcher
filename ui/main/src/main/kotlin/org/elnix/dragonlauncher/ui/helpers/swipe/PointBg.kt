package org.elnix.dragonlauncher.ui.helpers.swipe

import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.util.LruCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withClip
import org.elnix.dragonlauncher.base.cache.DrawPathCache
import org.elnix.dragonlauncher.base.cache.PointStableCache
import org.elnix.dragonlauncher.base.cache.StablePointValues
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.ktx.rect
import org.elnix.dragonlauncher.ktx.toPath
import org.elnix.dragonlauncher.ui.helpers.customobjects.drawPathGlow
import kotlin.math.roundToInt

/** Draws a point's border, glow, icon, and badge. */
@Suppress("FunctionName")
fun DrawScope.PointBg(
	point: Point,
	depth: Int,
	selected: Boolean,
	center: Offset,
	drawParams: DrawParams,
	stableValues: StablePointValues? = null
) {
	val extraColors = drawParams.extraColors
	val colorScheme = drawParams.colorScheme
	val defaultPoint = drawParams.pointsService.defaultPoint.value

	val cached = stableValues ?: PointStableCache[point.id]
	if (cached == null) {
		missingPoint(drawParams, center)
		return
	}

	val iconSize = cached.iconSize

	if (depth > 1 || !drawParams.isTopPoint) {
		val borderColor = point.getBorderColor(selected, defaultPoint, extraColors, drawParams.isDefaultEditing)
		val backgroundColor = point.getBackgroundColor(selected, defaultPoint, drawParams.isDefaultEditing)
		val glow = point.getGlow(selected, defaultPoint, drawParams.isDefaultEditing)

		val borderIconShape = point.getBorderShape(selected, defaultPoint, drawParams.isDefaultEditing)
		val borderShape = borderIconShape.resolveShape()
		val borderStroke = point.getBorderStroke(selected, defaultPoint, drawParams.isDefaultEditing)

		val path =
			DrawPathCache.getOrCompute(Pair(borderIconShape, iconSize)) {
				toPath(borderShape, iconSize)
			}

		translate(
			left = center.x + iconSize.width / -2f,
			top = center.y + iconSize.height / -2f
		) {
			drawPathGlow(
				path = path,
				color = borderColor,
				lineStrokeWidth = borderStroke,
				glow = glow,
				erase = true,
				eraseColor = drawParams.eraseColor,
				cacheKey = borderIconShape to iconSize
			)

			drawPath(
				path = path,
				color = backgroundColor,
				style = Fill
			)
		}
	}

	if (drawParams.showOnlyQuestionMarkIcons) {
		missingPoint(drawParams, center)
		return
	}

	val iconBitmap = cached.imageBitmap
	val badgeBitmap = cached.badgeBitmap

	if (iconBitmap != null) {
		val sizePx = cached.sizePx

		val size = Size.rect(sizePx)
		val iconShape = cached.iconShape ?: drawParams.iconShape
		val iconPath =
			DrawPathCache.getOrCompute(
				Pair(iconShape, size)
			) { toPath(iconShape.resolveShape(), size) }
		val iconWithBadge =
			PointIconBitmapCache.get(
				shape = iconShape,
				path = iconPath,
				sizePx = sizePx,
				icon = iconBitmap,
				badge = badgeBitmap,
				badgeColor = colorScheme.tertiary,
				badgeTint = colorScheme.onTertiary
			)

		withTransform(
			{
				if (drawParams.isTopPoint && depth == 1) {
					scale(
						scaleX = drawParams.appIconOverlayScale,
						scaleY = drawParams.appIconOverlayScale,
						pivot = center
					)
				}

				translate(
					left = center.x + sizePx / -2f,
					top = center.y + sizePx / -2f
				)
			}
		) {
			drawImage(iconWithBadge)
		}
	} else {
		unavailableAction(point, drawParams, center)
	}

	if (drawParams.pointSettingsDisplay) {
		DecorationIcons(
			ctx = drawParams.ctx,
			center = center,
			iconSize = iconSize,
			point = point
		)
	}
}

private data class PointIconBitmapKey(
	val shape: IconShape,
	val sizePx: Float,
	val icon: ImageBitmap,
	val badge: ImageBitmap?,
	val badgeColor: androidx.compose.ui.graphics.Color,
	val badgeTint: androidx.compose.ui.graphics.Color
)

/** Bounds cached point icon and badge composites to 12 MiB. */
private object PointIconBitmapCache : LruCache<PointIconBitmapKey, ImageBitmap>(12 * 1024 * 1024) {
	override fun sizeOf(key: PointIconBitmapKey, value: ImageBitmap): Int =
		(value.width.toLong() * value.height * 4).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

	fun get(
		shape: IconShape,
		path: Path,
		sizePx: Float,
		icon: ImageBitmap,
		badge: ImageBitmap?,
		badgeColor: androidx.compose.ui.graphics.Color,
		badgeTint: androidx.compose.ui.graphics.Color
	): ImageBitmap {
		val key = PointIconBitmapKey(shape, sizePx, icon, badge, badgeColor, badgeTint)
		return this[key] ?: run {
			val size = sizePx.roundToInt().coerceAtLeast(1)
			val bitmap = createBitmap(size, size)
			val canvas = android.graphics.Canvas(bitmap)
			val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
			val dst = RectF(0f, 0f, size.toFloat(), size.toFloat())

			canvas.withClip(path.asAndroidPath()) {
				drawBitmap(icon.asAndroidBitmap(), null, dst, paint)
			}

			if (badge != null && badge.width > 0) {
				val diameter = sizePx / 3f
				val radius = diameter / 2f
				val cx = sizePx - radius
				val cy = cx
				canvas.drawCircle(cx, cy, radius, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = badgeColor.toArgb() })

				val scale = diameter / badge.width
				val width = badge.width * scale
				val height = badge.height * scale
				paint.colorFilter = PorterDuffColorFilter(badgeTint.toArgb(), PorterDuff.Mode.SRC_IN)
				canvas.drawBitmap(
					badge.asAndroidBitmap(),
					null,
					RectF(cx - width / 2f, cy - height / 2f, cx + width / 2f, cy + height / 2f),
					paint
				)
			}

			bitmap.asImageBitmap().also { put(key, it) }
		}
	}
}
