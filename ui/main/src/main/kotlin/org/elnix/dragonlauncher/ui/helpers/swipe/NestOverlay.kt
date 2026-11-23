package org.elnix.dragonlauncher.ui.helpers.swipe

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import io.github.elnix90.shared.ui.base.asState
import org.elnix.dragonlauncher.base.cache.NestIntersectionShapesPathCache
import org.elnix.dragonlauncher.base.cache.PointStableCache
import org.elnix.dragonlauncher.base.model.serializables.GlobalDraggingMode
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.ui.components.IntersectionShape

/**
 * Composable wrapper that renders a nest and its points inside a [Canvas].
 *
 * Use this from composable context when you need to draw a complete nest
 * with backgrounds, borders, action icons, and badges. Prefer the
 * [DrawScope.NestOverlay] version when you are already inside a Canvas.
 */
@Composable
fun NestOverlay(
	nest: Nest,
	center: Offset,
	modifier: Modifier = Modifier,
	depth: Int = 1,
	eraseColor: Color,
	allowShowPointCenter: Boolean = false,
	showCancelZone: Boolean = false,
	hideShapes: Boolean = false,
	skipSelected: Boolean = false
) {
	val drawParams =
		rememberDrawParams(
			eraseColor = eraseColor,
			isDefaultEditing = false,
			isTopPoint = false,
			pointSettingsDisplay = true,
			allowShowPointCenter = allowShowPointCenter,
			showCancelZone = showCancelZone,
			hideShapes = hideShapes,
			skipSelected = skipSelected
		)
	val iconTrigger by PointStableCache.cacheTrigger.asState()

	// The key is actually useful, I tried to remove it, but it messed up the drawing in the PointSettingScreen
	key(iconTrigger) {
		Canvas(
			modifier =
				Modifier
					.fillMaxSize()
					.then(modifier)
		) {
			this.NestOverlay(
				nest = nest,
				depth = depth,
				center = center,
				drawParams = drawParams,
				selectedAll = false
			)
		}
	}
}

@Suppress("FunctionName")
fun DrawScope.NestOverlay(
	nest: Nest,
	depth: Int,
	center: Offset,
	drawParams: DrawParams,
	selectedAll: Boolean = false,
	lockedPoint: Point? = null,
	selectedPointsOverride: List<Int>? = null,
	selectedShapesOverride: Set<Int>? = null,
	hasSelectionOverride: Boolean? = null,
	selectedOnly: Boolean = false,
	drawShapes: Boolean = true,
	drawCancelZone: Boolean = true,
	drawCenter: Boolean = true
) {
	require(depth > 0)

	val isSettingDisplay = drawParams.pointSettingsDisplay

	val defaultNest = drawParams.pointsService.defaultNest.value
	val interSectionShapes = nest.getInterSectionShapes(defaultNest, drawParams.isDefaultEditing)
	val defaultShape = drawParams.pointsService.defaultIntersectionShape.value

	val selectedPointsIds =
		selectedPointsOverride
			?: if (drawParams.renderingNestPreview) emptyList() else drawParams.pointsService.getSelectedPoints(lockedPoint)
	val selectedPointIds = selectedPointsIds.toHashSet()
	val hasSelectedPoints = hasSelectionOverride ?: selectedPointsIds.isNotEmpty()
	val selectedShapes =
		if (depth == 1 && !isSettingDisplay && drawParams.globalDraggingMode !is GlobalDraggingMode.Fixed && hasSelectedPoints) {
			selectedShapesOverride ?: drawParams.pointsService.getSelectedShapeIds(nest.id, lockedPoint)
		} else {
			emptySet()
		}

	if (!drawParams.hideShapes && drawShapes) {
		repeat(2) { pass ->
			interSectionShapes.forEach { shape ->
				if (pass == 0 && depth == 1) return@forEach

				val showShape =
					depth > 1 ||
						drawParams.globalDraggingMode is GlobalDraggingMode.Fixed ||
						isSettingDisplay ||
						(
							nest.getShowAllShapes(
								defaultNest,
								drawParams.showAllShapesInNest,
								drawParams.isDefaultEditing
							) &&
								hasSelectedPoints
						) ||
						(
							nest.getShowCurrentShape(defaultNest, drawParams.showShape, drawParams.isDefaultEditing) &&
								(shape.id in selectedShapes)
						)

				if (showShape) {
					val path = NestIntersectionShapesPathCache[shape] ?: return@forEach

					this.IntersectionShape(
						path = path,
						shape = shape,
						defaultShape = defaultShape,
						center = center,
						extraColors = drawParams.extraColors,
						erase = pass == 0,
						isDefaultEditing = drawParams.isDefaultEditing,
						eraseColor = drawParams.eraseColor
					)
				}
			}
		}
	}

	if (drawCancelZone && drawParams.showCancelZone) {
		drawCircle(
			brush =
				Brush.sweepGradient(
					colors =
						listOf(
							Color.Red,
							Color.Yellow,
							Color.Green,
							Color.Cyan,
							Color.Blue,
							Color.Magenta
						)
				),
			radius = nest.getCancelZone(defaultNest, drawParams.isDefaultEditing).toPx(),
			center = center,
			style = Stroke(Stroke.HairlineWidth)
		)
	}

	if (drawCenter && isSettingDisplay) {
		centerOfNest(center)
	}

	val points = drawParams.pointsService.getPointsForNest(nest.id)
	val skipSelectedIds =
		if (drawParams.skipSelected && depth == 1) {
			drawParams.pointsService.selectedPointsIds.value
				.toHashSet()
		} else {
			emptySet()
		}
	val showAllNestPoints = nest.getShowAllPointsInCurrentNest(defaultNest, drawParams.showAllPointsInCurrentNest, drawParams.isDefaultEditing)
	val showAllShapePoints = nest.getShowAllPointsInCurrentNest(defaultNest, drawParams.showAllPointsInCurrentShape, drawParams.isDefaultEditing)
	val drawAllPoints =
		!selectedOnly && (
			depth > 1 || isSettingDisplay || drawParams.globalDraggingMode is GlobalDraggingMode.Fixed ||
				(hasSelectedPoints && showAllNestPoints)
		)
	val drawShapePoints = !selectedOnly && hasSelectedPoints && showAllShapePoints && selectedShapes.isNotEmpty()
	val previewPointId = selectedPointsIds.singleOrNull().takeIf { drawParams.showPointPreviewCenterStartPosition }
	var previewPoint: Point? = null

	/** Draws a point after the ring's visibility check. */
	fun drawPoint(point: Point) {
		val id = point.id
		if (id == previewPointId) previewPoint = point
		val pointOffset = center + point.getPos()

		if (drawParams.nestDebugOverlay) {
			val endOffset: Offset =
				if (point.shapeId == null) {
					center
				} else {
					interSectionShapes.firstOrNull { it.id == point.shapeId }?.let { shape ->
						center + shape.getOffset(defaultShape, drawParams.isDefaultEditing)
					} ?: center
				}

			drawLine(
				color = Color.White,
				start = endOffset,
				end = pointOffset
			)
		}

		PointIcon(
			depth = depth,
			point = point,
			center = pointOffset,
			selected = selectedAll || (id in selectedPointsIds),
			drawParams = drawParams
		)
	}

	if (drawAllPoints || drawShapePoints) {
		for (point in points) {
			val id = point.id
			if (id in skipSelectedIds) continue
			if (!drawAllPoints &&
				!((drawParams.showCurrentPoint && id in selectedPointIds) || (drawShapePoints && point.shapeId in selectedShapes))
			) {
				continue
			}
			drawPoint(point)
		}
	} else if (drawParams.showCurrentPoint) {
		val seenIds = mutableSetOf<Int>()
		for (id in selectedPointsIds) {
			if (!seenIds.add(id) || id in skipSelectedIds) continue
			val point = drawParams.pointsService.findPointById(id) ?: continue
			if (point.nestId == nest.id) drawPoint(point)
		}
	}

	if (previewPointId != null) {
		val point = previewPoint ?: return
		PointIcon(
			point = point,
			depth = depth,
			center = center,
			selected = true,
			drawParams = drawParams.copy(showPointPreviewCenterStartPosition = false)
		)
	}
}
