package org.elnix.dragonlauncher.ui.helpers.swipe

import android.graphics.Picture
import android.util.LruCache
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withTranslation
import org.elnix.dragonlauncher.base.cache.NestIntersectionShapesPathCache
import org.elnix.dragonlauncher.base.cache.PointStableCache
import org.elnix.dragonlauncher.base.cache.StablePointValues
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.ui.helpers.customobjects.toPxOrNull
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Canvas as ComposeCanvas

/**
 * Composable wrapper that renders a single point icon inside a [Canvas].
 *
 * Use this from composable context to draw a point's background, border,
 * action icon, and badges. Prefer the [DrawScope.PointIcon] version when
 * you are already inside a Canvas.
 */
@Composable
fun PointIcon(
	point: Point,
	center: Offset,
	eraseColor: Color,
	modifier: Modifier = Modifier,
	depth: Int = 1,
	selected: Boolean = false,
	pointSettingsDisplay: Boolean = false,
	isTopPoint: Boolean = false,
	hideShapes: Boolean = false
) {
	val drawParams =
		rememberDrawParams(
			eraseColor = eraseColor,
			isDefaultEditing = false,
			isTopPoint = isTopPoint,
			pointSettingsDisplay = pointSettingsDisplay,
			showCancelZone = false,
			allowShowPointCenter = false,
			hideShapes = hideShapes,
			skipSelected = false
		)

	Canvas(
		modifier =
			modifier
				.fillMaxSize()
				.graphicsLayer {
					compositingStrategy = CompositingStrategy.Offscreen
				}
	) {
		this.PointIcon(
			point = point,
			depth = depth,
			center = center,
			selected = selected,
			drawParams = drawParams
		)
	}
}

@Suppress("FunctionName")
fun DrawScope.PointIcon(
	point: Point,
	depth: Int,
	center: Offset,
	selected: Boolean,
	drawParams: DrawParams,
	stableValues: StablePointValues? = null
) {
	require(depth > 0)

	val action = point.action

	val hasReachedMaxDepth = when {
		drawParams.isTopPoint && depth < drawParams.maxTopBarDepth -> false
		!drawParams.isTopPoint && depth < drawParams.maxNestsDepth -> false
		else -> true
	}

	if (
		action is Action.OpenNest &&
		point.customIcon == null &&
		!hasReachedMaxDepth &&
		!drawParams.preventDrawingSubNests
	) {
		val nest = drawParams.pointsService.findNestById(action.nestId)
		val scaleFactor = nest.getPreviewScaleFactor(drawParams.pointsService.defaultNest.value, drawParams.isDefaultEditing)

		val newDepth = depth + 1
		var newScale = 1f / (newDepth * scaleFactor)

		if (drawParams.isTopPoint && depth == 1) {
			newScale *= drawParams.appIconOverlayScale
		}

		drawNestPreview(nest, newDepth, center, newScale, selected, drawParams)
	} else {
		PointBg(
			point = point,
			depth = depth,
			selected = selected,
			center = center,
			drawParams = drawParams,
			stableValues = stableValues
		)
	}
}

internal data class NestPreviewKey(
	val nestId: Int,
	val depth: Int,
	val selected: Boolean,
	val drawParamsStyle: NestPreviewStyleKey,
	val pointsServiceIdentity: Int,
	val renderingNestPreview: Boolean,
	val dataVersion: Int,
	val pointCacheVersion: Int,
	val shapeCacheVersion: Int,
	val selectedPointIds: List<Int>,
	val skippedPointIds: Set<Int>,
	val selectedShapeIds: Set<Int>,
	val hasSelectedPoints: Boolean,
	val pointCount: Int,
	val shapeCount: Int,
	val width: Int,
	val height: Int,
	val density: Float,
	val layoutDirection: LayoutDirection
)

internal data class NestPreview(
	val key: NestPreviewKey,
	val picture: Picture,
	val bitmap: ImageBitmap? = null
)

/** shortcut: Picture memory uses an estimate; bitmap memory uses Android's allocated byte count. */
private object NestPreviewCache : LruCache<NestPreviewKey, NestPreview>(32 * 1024 * 1024) {
	override fun sizeOf(key: NestPreviewKey, value: NestPreview): Int {
		val pictureBytes = key.pointCount.toLong() * 1024 + key.shapeCount.toLong() * 8192
		val bitmapBytes = value.bitmap
			?.asAndroidBitmap()
			?.allocationByteCount
			?.toLong() ?: 0L
		return (pictureBytes + bitmapBytes).coerceIn(1, Int.MAX_VALUE.toLong()).toInt()
	}
}

internal fun DrawScope.drawNestLayer(
	preview: NestPreview,
	center: Offset,
	rasterize: Boolean
) {
	val picture = preview.picture
	val pictureWidth = picture.width
	val pictureHeight = picture.height
	if (!rasterize) {
		drawIntoCanvas { canvas ->
			val nativeCanvas = canvas.nativeCanvas
			nativeCanvas.withTranslation(center.x - pictureWidth / 2f, center.y - pictureHeight / 2f) {
				nativeCanvas.drawPicture(picture)
			}
		}
		return
	}

	val bitmap = preview.bitmap ?: NestPreviewCache[preview.key]?.bitmap ?: run {
		val image = createBitmap(pictureWidth, pictureHeight)
		android.graphics.Canvas(image).drawPicture(picture)
		image.asImageBitmap().also {
			NestPreviewCache.put(preview.key, preview.copy(bitmap = it))
		}
	}
	drawImage(bitmap, topLeft = Offset(center.x - pictureWidth / 2f, center.y - pictureHeight / 2f))
}

internal fun nestPreviewExtent(
	nest: Nest,
	points: Collection<Point>,
	defaultPoint: Point,
	defaultNest: Nest,
	defaultShape: IntersectionShape,
	density: Density,
	isDefaultEditing: Boolean = false
): Float {
	var furthest = 0f
	for (point in points) {
		val radius =
			PointStableCache[point.id]?.iconSize?.let { maxOf(it.width, it.height) / 2f }
				?: with(density) {
					(point.getSize(defaultPoint, isDefaultEditing) + point.getInnerPadding(defaultPoint, isDefaultEditing) * 2).toPx() / 2f
				}
		val glow = maxOf(
			point.getGlow(false, defaultPoint, isDefaultEditing).radius.toPxOrNull(density) ?: 0f,
			point.getGlow(true, defaultPoint, isDefaultEditing).radius.toPxOrNull(density) ?: 0f
		)
		val stroke = maxOf(
			point.getBorderStroke(false, defaultPoint, isDefaultEditing).toPxOrNull(density) ?: 0f,
			point.getBorderStroke(true, defaultPoint, isDefaultEditing).toPxOrNull(density) ?: 0f
		)
		furthest = maxOf(furthest, point.getPos().getDistance() + radius + glow * 3f + stroke / 2f)
	}
	for (shape in nest.getInterSectionShapes(defaultNest, isDefaultEditing)) {
		val shapeSize = shape.getSize(density.density, defaultShape, isDefaultEditing)
		val glow = shape.getGlow(defaultShape, isDefaultEditing).radius.toPxOrNull(density) ?: 0f
		furthest = maxOf(
			furthest,
			shape.getOffset(defaultShape, isDefaultEditing).getDistance() + maxOf(shapeSize.width, shapeSize.height) / 2f + glow * 3f
		)
	}
	return maxOf(furthest, nest.getCancelZone(defaultNest, isDefaultEditing).toPxOrNull(density) ?: 0f, 64f) +
		with(density) { 64.dp.toPx() }
}

internal fun getNestPreviewPicture(
	nest: Nest,
	depth: Int,
	selected: Boolean,
	drawParams: DrawParams,
	width: Int,
	height: Int,
	density: Density,
	layoutDirection: LayoutDirection,
	selectedPointIds: List<Int>? = null,
	selectedShapeIds: Set<Int>? = null,
	hasSelectedPoints: Boolean? = null
): NestPreview {
	val pointsService = drawParams.pointsService
	val points = pointsService.getPointsForNest(nest.id)
	val shapes = nest.getInterSectionShapes(pointsService.defaultNest.value, drawParams.isDefaultEditing)
	val key =
		NestPreviewKey(
			nestId = nest.id,
			depth = depth,
			selected = selected,
			drawParamsStyle = drawParams.nestPreviewStyleKey,
			pointsServiceIdentity = System.identityHashCode(pointsService),
			renderingNestPreview = drawParams.renderingNestPreview,
			dataVersion = pointsService.recomposeTrigger.value,
			pointCacheVersion = PointStableCache.cacheTrigger.value,
			shapeCacheVersion = NestIntersectionShapesPathCache.cacheTrigger.value,
			selectedPointIds = selectedPointIds.orEmpty().toList(),
			skippedPointIds =
				if (drawParams.skipSelected && depth == 1) {
					pointsService.selectedPointsIds.value.toSet()
				} else {
					emptySet()
				},
			selectedShapeIds = selectedShapeIds.orEmpty().toSet(),
			hasSelectedPoints = hasSelectedPoints ?: false,
			pointCount = points.size,
			shapeCount = shapes.size,
			width = width,
			height = height,
			density = density.density,
			layoutDirection = layoutDirection
		)
	NestPreviewCache[key]?.let { return it }
	// Point Settings has a screen-sized canvas; avoid a second full points scan there.
	val extentDiameter =
		if (drawParams.pointSettingsDisplay) {
			0
		} else {
			nestPreviewExtent(
				nest = nest,
				points = points,
				defaultPoint = pointsService.defaultPoint.value,
				defaultNest = pointsService.defaultNest.value,
				defaultShape = pointsService.defaultIntersectionShape.value,
				density = density,
				isDefaultEditing = drawParams.isDefaultEditing
			) * 2f
		}.toInt()
	val pictureWidth = maxOf(width, extentDiameter).coerceAtLeast(1)
	val pictureHeight = maxOf(height, extentDiameter).coerceAtLeast(1)
	val picture = Picture()
	val recordingCanvas = picture.beginRecording(pictureWidth, pictureHeight)
	try {
		CanvasDrawScope().draw(
			density = density,
			layoutDirection = layoutDirection,
			canvas = ComposeCanvas(recordingCanvas),
			size = Size(pictureWidth.toFloat(), pictureHeight.toFloat())
		) {
			NestOverlay(
				nest = nest,
				depth = depth,
				center = Offset(pictureWidth / 2f, pictureHeight / 2f),
				drawParams = drawParams.copy(renderingNestPreview = true),
				selectedAll = selected,
				selectedPointsOverride = selectedPointIds,
				selectedShapesOverride = selectedShapeIds,
				hasSelectionOverride = hasSelectedPoints
			)
		}
	} finally {
		picture.endRecording()
	}
	return NestPreview(key, picture).also { NestPreviewCache.put(key, it) }
}

private fun DrawScope.drawNestPreview(
	nest: Nest,
	depth: Int,
	center: Offset,
	scaleFactor: Float,
	selected: Boolean,
	drawParams: DrawParams
) {
	val width = size.width.roundToInt().coerceAtLeast(1)
	val height = size.height.roundToInt().coerceAtLeast(1)
	val pointsService = drawParams.pointsService
	val density = drawContext.density
	val preview = getNestPreviewPicture(nest, depth, selected, drawParams, width, height, density, layoutDirection)
	val picture = preview.picture
	val pictureWidth = picture.width.coerceAtLeast(width)
	val pictureHeight = picture.height.coerceAtLeast(height)
	val previewScale = minOf(scaleFactor, size.width / pictureWidth, size.height / pictureHeight)
	val selectedIds = if (drawParams.renderingNestPreview) emptyList() else pointsService.selectedPointsIds.value

	scale(scale = previewScale, pivot = center) {
		drawIntoCanvas { canvas ->
			val nativeCanvas = canvas.nativeCanvas
			nativeCanvas.withTranslation(center.x - pictureWidth / 2f, center.y - pictureHeight / 2f) {
				nativeCanvas.drawPicture(picture)
			}
		}

		if (!selected && !drawParams.renderingNestPreview) {
			for (id in selectedIds) {
				val point = drawParams.pointsService.findPointById(id) ?: continue
				if (point.nestId != nest.id) continue
				PointIcon(
					point = point,
					depth = depth,
					center = center + point.getPos(),
					selected = true,
					drawParams = drawParams
				)
			}
		}

		if (!drawParams.renderingNestPreview && drawParams.showPointPreviewCenterStartPosition) {
			selectedIds.singleOrNull()?.let { id ->
				val point = drawParams.pointsService.findPointById(id) ?: return@let
				if (point.nestId == nest.id) {
					PointIcon(
						point = point,
						depth = depth,
						center = center,
						selected = true,
						drawParams = drawParams.copy(showPointPreviewCenterStartPosition = false)
					)
				}
			}
		}
	}
}
