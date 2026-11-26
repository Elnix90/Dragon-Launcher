package org.elnix.dragonlauncher.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import io.github.elnix90.runtime.asState
import io.github.elnix90.shared.ui.animation.bouncySpec
import io.github.elnix90.shared.ui.base.asState
import io.github.elnix90.shared.ui.base.compositionlocals.LocalDisableHapticFeedbackGlobally
import org.elnix.dragonlauncher.base.cache.NestIntersectionShapesPathCache
import org.elnix.dragonlauncher.base.cache.PointStableCache
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.CustomHapticFeedback
import org.elnix.dragonlauncher.base.model.serializables.GlobalDraggingMode
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.ktx.angleDeg
import org.elnix.dragonlauncher.ktx.cleanString
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.models.SwipeViewModel
import org.elnix.dragonlauncher.settings.stores.map.AngleLineSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.components.PointPreviewTitle
import org.elnix.dragonlauncher.ui.composition.LocalNestDebugOverlay
import org.elnix.dragonlauncher.ui.helpers.DebugZone
import org.elnix.dragonlauncher.ui.helpers.PointerLocation
import org.elnix.dragonlauncher.ui.helpers.customobjects.actionLine
import org.elnix.dragonlauncher.ui.helpers.customobjects.resolveRotation
import org.elnix.dragonlauncher.ui.helpers.swipe.DrawParams
import org.elnix.dragonlauncher.ui.helpers.swipe.NestOverlay
import org.elnix.dragonlauncher.ui.helpers.swipe.drawNestLayer
import org.elnix.dragonlauncher.ui.helpers.swipe.getNestPreviewPicture
import org.elnix.dragonlauncher.ui.helpers.swipe.nestPreviewExtent
import org.elnix.dragonlauncher.ui.helpers.swipe.rememberDrawParams
import org.elnix.dragonlauncher.ui.remembers.LiveNestState
import org.elnix.dragonlauncher.ui.remembers.angle360
import org.elnix.dragonlauncher.ui.remembers.rememberCustomText
import org.elnix.dragonlauncher.ui.remembers.rememberCycleActionsController
import org.elnix.dragonlauncher.ui.remembers.rememberHoldAndRunController
import org.elnix.dragonlauncher.ui.remembers.rememberLiveNestControllerStack

@Composable
fun MainScreenOverlay(
	pointsViewModel: PointsViewModel = activityViewModel(),
	swipeViewModel: SwipeViewModel = activityViewModel(),
	lineBeforeNests: Boolean,
	start: Offset?,
	current: Offset?,
	globalDraggingMode: GlobalDraggingMode,
	currentNestId: Int
) {
	val ctx = LocalContext.current
	val extraColors = LocalExtraColors.current
	val haptic = LocalHapticFeedback.current
	val disableHapticFeedbackGlobally = LocalDisableHapticFeedbackGlobally.current

	val pointsService = pointsViewModel.pointsService
	val swipeService = swipeViewModel.swipeService

	val defaultPoint by pointsService.defaultPoint.asState()
	val defaultNest by pointsService.defaultNest.asState()
	val defaultShape by pointsService.defaultIntersectionShape.asState()

	val lineObject by swipeService.lineObject.asState()
	val angleObject by swipeService.angleObject.asState()
	val startObject by swipeService.startObject.asState()
	val endObject by swipeService.endObject.asState()
	val order by swipeService.lineObjectOrder.asState()

	val rgbLine by AngleLineSettingsStore.rgbLine.asState()

	val linePreviewSnapToAction by UiSettingsStore.linePreviewSnapToAction.asState()
	val animationWhenSnapping by UiSettingsStore.animationWhenSnapping.asState()
	val useSnappedAngleOrRealAngle by AngleLineSettingsStore.useSnappedAngleOrRealAngle.asState()

	val isDragging = start != null && current != null

	val liveNestControllersStack: List<LiveNestState> =
		rememberLiveNestControllerStack(
			isDragging = isDragging,
			current = current,
			rootStartPos = start,
			rootNestId = currentNestId
		)

	/** Find which level is currently active (highest active one) */
	val activeLevelIndex = liveNestControllersStack.indexOfLast { it.isActive }
	val highestController = liveNestControllersStack[activeLevelIndex]

	val isAnyLiveNestActive = activeLevelIndex > 0

	val hoveredPoint =
		liveNestControllersStack
			.findLast { liveNestState ->
				liveNestState.nestedHitResult?.selectedPoint != null
			}?.nestedHitResult
			?.selectedPoint

	/**
	 * The offset that is used to provide an animated offset to current selected points, when user uses animation when snapping.
	 * It has to have checked both in order to use this.
	 *
	 * The offset is animated when the [hoveredPoint] changes in order to respond to their offset
	 *
	 * [Offset.Zero] is considered the center of the nest, or in other words, [start]
	 */
	val animatedCurrent =
		remember {
			Animatable(Offset.Zero, Offset.VectorConverter)
		}

	/*
	 * 1. selects the hovered point in the point service
	 * 2. animates the offset whenever the hovered point changes to this new point offset using [Point.getPos]
	 */
	LaunchedEffect(hoveredPoint) {
		val hoveredPointId = hoveredPoint?.id
		pointsService.selectOnyOne(hoveredPointId)

		if (!animationWhenSnapping ||
			hoveredPoint == null ||
			(isAnyLiveNestActive && hoveredPoint == highestController.hostPoint)
		) {
			return@LaunchedEffect
		}

		animatedCurrent.animateTo(
			targetValue = hoveredPoint.getPos(),
			animationSpec = bouncySpec()
		)
	}

	/*
	 * This prevents the animated offset to always come from the center, but start animating from the current pos
	 * Which should be always non-null id a point is selected.
	 *
	 * it snaps either when the root nest is on no point, or when the highest live lest is at its center (the hovered point is the host)
	 */
	if (animationWhenSnapping) {
		LaunchedEffect(current) {
			if (!isDragging) return@LaunchedEffect

			if (hoveredPoint == null || (isAnyLiveNestActive && hoveredPoint == highestController.hostPoint)) {
				// Use the active ring center, or the gesture start for the root ring.
				animatedCurrent.snapTo(current - (highestController.liveNestCenter ?: start))
			}
		}
	}

	val cycleActionsController =
		rememberCycleActionsController(
			currentAction = hoveredPoint,
			isDragging = isDragging
		)

	/**
	 * When a non-base cycle stage is active, substitute the stage's action in the preview point
	 * so actionsInCircle and AppPreviewTitle reflect the action that will fire on release.
	 * Loop Over reuses the last stage's action with a temporary label; customIcon is cleared
	 * whenever either the base or staged action is OpenCircleNest (mini-nest rings need null icon).
	 */
	val displayPoint: Point? =
		hoveredPoint?.let { hp ->
			val ca = hp.cycleActions
			if (ca.isNullOrEmpty()) return@let hp

			val idx = cycleActionsController.currentStageIndex
			if (idx > 0) {
				val staged = ca.getOrNull(idx - 1)?.action ?: return@let hp
				if (staged is Action.OpenNest || hp.action is Action.OpenNest) {
					hp.copy(action = staged, customIcon = null)
				} else {
					hp.copy(action = staged)
				}
			} else {
				hp
			}
		}
	val holdAndRun =
		rememberHoldAndRunController(
			currentPoint = hoveredPoint,
			isDragging = isDragging
		) { firedPoint ->
			// Hold & Run auto-fires mid-hold, so the cycle stage under the finger is what must fire.
			swipeService.launchAction(pointsService.cycleStageAction.value ?: firedPoint.action)
		}

	LaunchedEffect(hoveredPoint?.id, liveNestControllersStack.count { it.isActive }) {
		hoveredPoint?.let { point ->
			if (!disableHapticFeedbackGlobally) {
				val hitNestId = highestController.nestedNestId ?: return@let
				val hitNest = pointsService.findNestById(hitNestId)

				val targetShape =
					hitNest.getInterSectionShapes(defaultNest, false).find { highestController.nestedHitResult?.selectedPoint?.shapeId == it.id }

				val hapticToPerform = (point.haptic ?: targetShape?.haptic ?: defaultHapticFeedback())
				hapticToPerform.perform(ctx)
			}
		}
	}

	LaunchedEffect(highestController.nestedHitResult?.isInCancelZone) {
		if (isAnyLiveNestActive &&
			highestController.isActive &&
			highestController.nestedHitResult?.isInCancelZone == true &&
			!disableHapticFeedbackGlobally
		) {
			haptic.performHapticFeedback(HapticFeedbackType.LongPress)
		}
	}

	LaunchedEffect(isDragging) {
		liveNestControllersStack.forEach { it.clearAfterLaunch() }
		holdAndRun.clear()
		cycleActionsController.clear()
	}

	val showLineObjectPreview by AngleLineSettingsStore.showLineObjectPreview.asState()
	val showAngleLineObjectPreview by AngleLineSettingsStore.showAngleLineObjectPreview.asState()
	val showStartObjectPreview by AngleLineSettingsStore.showStartObjectPreview.asState()
	val showEndObjectPreview by AngleLineSettingsStore.showEndObjectPreview.asState()

	val multiplyOrSubtractOpacityInLiveNests by UiSettingsStore.multiplyOrSubtractOpacityInLiveNests.asState()

	/**
	 * Alpha value for each layer: main nest, then each active Live Nest overlay (from highest to shallowest).
	 * The more the user go deeper, the more transparent first layers get
	 * */
	val liveNestLayersAlphas: List<Float> =
		buildList {
			var alpha = 1f

			liveNestControllersStack.filter { it.isActive }.forEach { controller ->
				add(alpha)

				val percent =
					(controller.hostPoint?.liveNestSubNestOpacityPercent ?: defaultPoint.liveNestSubNestOpacityPercent)
						.takeIf { it != -1 } ?: Point.defaultLiveNestMainNestOpacityPercent

				if (multiplyOrSubtractOpacityInLiveNests) {
					alpha -= percent.coerceIn(0, 100) / 100f
				} else {
					alpha *= percent.coerceIn(0, 100) / 100f
				}
			}
		}.reversed()

	val debugInfo by DebugSettingsStore.mainScreenDebugInfos.asState()
	val nestDebugOverlay = LocalNestDebugOverlay.current
	val drawParams =
		rememberDrawParams(
			eraseColor = Color.Transparent,
			isDefaultEditing = false,
			allowShowPointCenter = true,
			pointSettingsDisplay = false,
			isTopPoint = false,
			showCancelZone = nestDebugOverlay,
			hideShapes = false,
			skipSelected = false
		)

	/** Invalidates cached artwork when point images or selections change. */
	val iconsTrigger = PointStableCache.cacheTrigger.asState()
	val shapePathsTrigger = NestIntersectionShapesPathCache.cacheTrigger.asState()
	val selectedPointsIds = pointsService.selectedPointsIds.asState()
	val pointsVersion by pointsService.recomposeTrigger.asState()

	Box(Modifier.fillMaxSize()) {
		DebugZone(debugInfo) {
			Text("start = ${start?.let { "%.1f, %.1f".format(it.x, it.y) } ?: "-"}")
			Text("current = ${current?.let { "%.1f, %.1f".format(it.x, it.y) } ?: "-"}")
			Text("sweep raw = %.1f°".format(highestController.sweepAngleState.sweepAngle()))
			Text("drag = $isDragging")
			Text("activeLevel = $activeLevelIndex")
			Text("isAliveNestActive = $isAnyLiveNestActive")
			Text("current nest = $currentNestId")
			Text("current point = $hoveredPoint")
		}

		if (debugInfo) {
			val drawScopeText = rememberCustomText(animatedCurrent.value.cleanString(), 0f)

			Canvas(Modifier.fillMaxSize()) {
				PointerLocation(
					offset = animatedCurrent.value + (highestController.liveNestCenter ?: start ?: Offset.Zero),
					centerText = drawScopeText
				)
			}
		}

		if (isDragging || globalDraggingMode is GlobalDraggingMode.Fixed) {
			for ((idx, controller) in liveNestControllersStack.withIndex()) {
				if (controller.isActive) {
					val liveNestOpacity = liveNestLayersAlphas.getOrNull(idx) ?: continue

					/**
					 * Whether if this controller is the one above all, which mean the currently active one.
					 * All the controllers below are active, but frozen, only this one has an angle line that moves and points that selects.
					 * Until this one gets dismissed by the controller system
					 */
					val isHighestController = idx == activeLevelIndex

					val nest = pointsService.findNestById(controller.nestedNestId!!)

					val liveNestCenter = controller.liveNestCenter!!
					val liveNestCenterState = rememberUpdatedState(liveNestCenter)
					val liveNestHitResult = controller.nestedHitResult
					val liveNestSelectedPoint = liveNestHitResult?.selectedPoint

					/**
					 * The final drawn offset of the pointer.
					 * The `end` pos of the [org.elnix.dragonlauncher.base.model.models.AngleLineObjects]
					 *
					 * The computation is quite a mess, but here's a breakdown. wow, I'm speaking like an AI
					 *
					 *
					 * - If the current controller (from the `for` loop above) is **NOT** the highest (which means its is currently active and moves), it uses the center of the live nest as a `end`
					 * - If both snap points and animated are checked, it uses the animated current + the center of the live nest
					 * - If only the snap to action is checked, it uses [Point.getPos] to... get its [pos][Point.pos] + the center of the live nest
					 * - Finally, if nothing worked, it defaults to the computed current pos
					 */
					val effectiveCurrentPos: Offset? =
						when {
							// Means that the live HAS to snap to action, because otherwise it would move around under the top activated live nest
							!isHighestController -> {
								liveNestControllersStack[idx + 1].liveNestCenter!!
							}

							linePreviewSnapToAction && animationWhenSnapping && liveNestSelectedPoint != null -> {
								animatedCurrent.value + liveNestCenter
							}

							linePreviewSnapToAction && liveNestSelectedPoint != null -> {
								liveNestSelectedPoint.getPos() + liveNestCenter
							}

							else -> {
								current
							}
						}

					val sweepAngle: Float = controller.sweepAngleState.sweepAngle()

					/**
					 * The final used angle in the colors
					 * Shorter breakdown here:
					 *
					 *  - If not the highest controller, use the angle of the host point
					 *  - If snaps to the points and a point is selected in that controller
					 *  - else use the sweepAngle but convert to int to lose the decimals
					 */
					val effectiveSweepAngle: Int =
						when {
							!isHighestController -> {
								liveNestControllersStack[idx + 1]
									.hostPoint!!
									.getPos()
									.angleDeg()
									.toInt()
							}

							useSnappedAngleOrRealAngle && liveNestSelectedPoint != null -> {
								liveNestSelectedPoint.getPos().angleDeg().toInt()
							}

							else -> {
								sweepAngle.toInt()
							}
						}

					val pickedRememberShapeAngle = remember(angleObject.shape) { angleObject.shape.resolveShape() }
					val pickedRememberRotationAngle = angleObject.resolveRotation(true, effectiveSweepAngle)

					val pickedRememberShapeStart = remember(startObject.shape) { startObject.shape.resolveShape() }
					val pickedRememberRotationStart = startObject.resolveRotation(true, effectiveSweepAngle)

					val pickedRememberShapeEnd = remember(endObject.shape) { endObject.shape.resolveShape() }
					val pickedRememberRotationEnd = endObject.resolveRotation(false, effectiveSweepAngle)

					fun DrawScope.lineDrawing() {
						if (effectiveCurrentPos == null) return

						/**
						 * The line color uses a [Int] angle, that it converts to a float, to prevent tiny difference in colors.
						 * This method can only produce at most 360 different colors.
						 *
						 * This is needed by the [org.elnix.dragonlauncher.ui.helpers.customobjects.customGlowPaint] to provide optimizations when dealing with the low-level Paint APIs.
						 * This prevents the [org.elnix.dragonlauncher.ui.helpers.customobjects.PaintCache] to be made useless by too much different [android.graphics.Paint] requests
						 */
						val lineColor: Color =
							if (rgbLine) {
								Color.hsv(effectiveSweepAngle.angle360().toFloat(), 1f, 1f)
							} else {
								extraColors.angleLine
							}

						actionLine(
							start = liveNestCenter,
							end = effectiveCurrentPos,
							sweepAngle = sweepAngle,
							lineColor = lineColor,
							order = order,
							eraseColor = null,
							showLineObjectPreview = showLineObjectPreview,
							showAngleLineObjectPreview = showAngleLineObjectPreview,
							showStartObjectPreview = showStartObjectPreview,
							showEndObjectPreview = showEndObjectPreview,
							pickedRememberShapeAngle = pickedRememberShapeAngle,
							pickedRememberRotationAngle = pickedRememberRotationAngle,
							pickedRememberRotationStart = pickedRememberRotationStart,
							pickedRememberShapeStart = pickedRememberShapeStart,
							pickedRememberRotationEnd = pickedRememberRotationEnd,
							pickedRememberShapeEnd = pickedRememberShapeEnd,
							lineCustomObject = lineObject,
							angleLineCustomObject = angleObject,
							startCustomObject = startObject,
							endCustomObject = endObject
						)
					}

					fun DrawScope.drawDebugLines() {
						if (!nestDebugOverlay) return
						val pos = controller.recentPositions
						pos.forEach {
							drawCircle(
								color = Color.Red,
								radius = 5f,
								center = it
							)
						}

						if (pos.size < 4) return

						val mid = pos.size / 2

						val start = pos.first()
						val middle = pos[mid]
						val end = pos.last()

						drawLine(
							start = start,
							end = middle,
							color = Color.Green,
							strokeWidth = 2f
						)

						drawLine(
							start = middle,
							end = end,
							color = Color.Green,
							strokeWidth = 2f
						)
					}

					/** Every value captured by lineDrawing is keyed, so frozen lines redraw only when their inputs change. */
					val lineModifier =
						remember(
							liveNestCenter,
							effectiveCurrentPos,
							sweepAngle,
							effectiveSweepAngle,
							rgbLine,
							extraColors.angleLine,
							order,
							lineObject,
							angleObject,
							startObject,
							endObject,
							showLineObjectPreview,
							showAngleLineObjectPreview,
							showStartObjectPreview,
							showEndObjectPreview,
							pickedRememberShapeAngle,
							pickedRememberRotationAngle,
							pickedRememberShapeStart,
							pickedRememberRotationStart,
							pickedRememberShapeEnd,
							pickedRememberRotationEnd
						) {
							Modifier.drawWithCache {
								onDrawBehind { lineDrawing() }
							}
						}

					val canvasModifier =
						Modifier
							.fillMaxSize()
							.graphicsLayer {
								alpha = liveNestOpacity
								compositingStrategy = CompositingStrategy.Offscreen
							}

					Box(canvasModifier) {
						if (lineBeforeNests) {
							Canvas(Modifier.fillMaxSize().then(lineModifier)) {}
						}

						LiveNestPointsCanvas(
							center = liveNestCenterState,
							nest = nest,
							defaultPoint = defaultPoint,
							defaultNest = defaultNest,
							defaultShape = defaultShape,
							drawParams = drawParams,
							lockedPoint = if (isHighestController) null else controller.hostPoint,
							iconsTrigger = iconsTrigger,
							shapePathsTrigger = shapePathsTrigger,
							pointsVersion = pointsVersion,
							selectedPointsIds = selectedPointsIds,
							drawSelectedPoint = isHighestController,
							centerIsStable = idx == 0 || controller.hostPoint?.getLiveNestSnapsToFingerPosition(defaultPoint, false) == true
						)

						if (!lineBeforeNests || nestDebugOverlay) {
							Canvas(Modifier.fillMaxSize().then(if (lineBeforeNests) Modifier else lineModifier)) {
								drawDebugLines()
							}
						}
					}
				} else {
					break
				}
			}
		}
	}

	// A staged Cycle Actions stage keeps the point's id but changes its action, so the id-keyed
	// icon cache cannot reflect it; fall back to the action-derived icon while a stage is active.
	val cycleStageActive = displayPoint != null && displayPoint.action != hoveredPoint.action

	PointPreviewTitle(displayPoint, useActionIcon = cycleStageActive)
}

/** Draws a cached nest picture on the screen-sized canvas. */
@Composable
private fun LiveNestPointsCanvas(
	center: State<Offset>,
	nest: Nest,
	defaultPoint: Point,
	defaultNest: Nest,
	defaultShape: IntersectionShape,
	drawParams: DrawParams,
	lockedPoint: Point?,
	iconsTrigger: State<Int>,
	shapePathsTrigger: State<Int>,
	pointsVersion: Int,
	selectedPointsIds: State<List<Int>>,
	drawSelectedPoint: Boolean,
	centerIsStable: Boolean
) {
	val density = LocalDensity.current
	val points = drawParams.pointsService.getPointsForNest(nest.id)
	val extent = remember(nest, points, defaultPoint, defaultNest, defaultShape, drawParams, density, pointsVersion) {
		nestPreviewExtent(nest, points, defaultPoint, defaultNest, defaultShape, density)
	}
	val diameter = (extent * 2f).toInt().coerceAtLeast(1)
	val hasSelection = remember(selectedPointsIds, lockedPoint) {
		derivedStateOf { selectedPointsIds.value.isNotEmpty() || lockedPoint != null }
	}
	val selectedPointsInNest = remember(selectedPointsIds, lockedPoint, nest.id, drawParams.pointsService) {
		derivedStateOf {
			buildList {
				selectedPointsIds.value.forEach { id ->
					if (drawParams.pointsService.findPointById(id)?.nestId == nest.id) add(id)
				}
				lockedPoint?.takeIf { it.nestId == nest.id }?.id?.let { if (it !in this) add(it) }
			}
		}
	}
	val selectedShapesInNest = remember(selectedPointsInNest, drawParams.pointsService) {
		derivedStateOf {
			buildSet {
				selectedPointsInNest.value.forEach { id ->
					drawParams.pointsService
						.findPointById(id)
						?.shapeId
						?.let(::add)
				}
			}
		}
	}
	val canvasModifier = remember(
		nest,
		defaultPoint,
		defaultNest,
		defaultShape,
		drawParams,
		lockedPoint,
		iconsTrigger,
		shapePathsTrigger,
		pointsVersion,
		center,
		hasSelection,
		selectedPointsInNest,
		selectedShapesInNest,
		drawSelectedPoint,
		diameter
	) {
		Modifier
			.fillMaxSize()
			.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
			.drawWithCache {
				iconsTrigger.value
				shapePathsTrigger.value
				val staticPoints = if (drawSelectedPoint) emptyList() else selectedPointsInNest.value
				val preview =
					getNestPreviewPicture(
						nest = nest,
						depth = 1,
						selected = false,
						drawParams = if (drawSelectedPoint) drawParams.copy(skipSelected = true) else drawParams,
						width = diameter,
						height = diameter,
						density = density,
						layoutDirection = layoutDirection,
						selectedPointIds = staticPoints,
						selectedShapeIds = selectedShapesInNest.value,
						hasSelectedPoints = hasSelection.value
					)
				onDrawBehind {
					val liveCenter = center.value
					drawNestLayer(preview, liveCenter, centerIsStable)
					if (drawSelectedPoint) {
						val ids = selectedPointsIds.value
						NestOverlay(
							center = liveCenter,
							nest = nest,
							depth = 1,
							drawParams = drawParams,
							selectedPointsOverride = ids,
							hasSelectionOverride = ids.isNotEmpty(),
							selectedOnly = true,
							drawShapes = false,
							drawCancelZone = false,
							drawCenter = false
						)
					}
				}
			}
	}
	Canvas(canvasModifier) {}
}

fun defaultHapticFeedback(): CustomHapticFeedback =
	CustomHapticFeedback.build {
		haptic(20)
	}
