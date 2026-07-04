package org.elnix.dragonlauncher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import io.github.elnix90.logging.SWIPE_TAG
import io.github.elnix90.logging.logI
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.CustomHapticFeedback
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultAngleCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultEndCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultStartCustomObject
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.models.IconsViewModel
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.settings.stores.map.AngleLineSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.compositionslocals.LocalDisableHapticFeedbackGlobally
import org.elnix.dragonlauncher.ui.components.PointPreviewTitle
import org.elnix.dragonlauncher.ui.composition.LocalAngleLineObject
import org.elnix.dragonlauncher.ui.composition.LocalEndLineObject
import org.elnix.dragonlauncher.ui.composition.LocalLineObject
import org.elnix.dragonlauncher.ui.composition.LocalStartLineObject
import org.elnix.dragonlauncher.ui.dialogs.rememberLineObjectsOrder
import org.elnix.dragonlauncher.ui.helpers.DebugZone
import org.elnix.dragonlauncher.ui.helpers.customobjects.actionLine
import org.elnix.dragonlauncher.ui.helpers.swipe.NestOverlay
import org.elnix.dragonlauncher.ui.helpers.swipe.rememberDrawParams
import org.elnix.dragonlauncher.ui.remembers.LiveNestState
import org.elnix.dragonlauncher.ui.remembers.rememberCycleActionsController
import org.elnix.dragonlauncher.ui.remembers.rememberHoldAndRunController
import org.elnix.dragonlauncher.ui.remembers.rememberLiveNestControllerStack

@Composable
fun MainScreenOverlay(
    iconsViewModel: IconsViewModel = activityViewModel(),
    pointsViewModel: PointsViewModel = activityViewModel(),
    start: Offset?,
    current: Offset?,
    currentNest: Nest,
    onLaunch: ((Point) -> Unit)?
) {
    val ctx = LocalContext.current
    val extraColors = LocalExtraColors.current
    val pointsService = pointsViewModel.pointsService

    val defaultPoint by pointsService.defaultPoint.asState()
    val disableHapticFeedbackGlobally = LocalDisableHapticFeedbackGlobally.current

    val lineObject = LocalLineObject.current
    val angleLineObject = LocalAngleLineObject.current
    val startObject = LocalStartLineObject.current
    val endObject = LocalEndLineObject.current

    val rgbLine by AngleLineSettingsStore.rgbLine.asState()

    val showLaunchingAppLabel by UiSettingsStore.showLaunchingAppLabel.asState()
    val showLaunchingAppIcon by UiSettingsStore.showPreviewPoint.asState()

    val appLabelIconOverlayTopPadding by UiSettingsStore.appLabelIconOverlayTopPadding.asState()

    val linePreviewSnapToAction by UiSettingsStore.linePreviewSnapToAction.asState()

    val isDragging = start != null && current != null

    val order by rememberLineObjectsOrder()

    val liveNestControllersStack: List<LiveNestState> = rememberLiveNestControllerStack(
        isDragging = isDragging,
        current = current,
        rootStartPos = start,
        rootNest = currentNest
    )

    // Find which level is currently active (deepest active one)
    val activeLevelIndex = liveNestControllersStack.indexOfLast { it.isActive }
    assert(activeLevelIndex > -1)

    val deepestController = liveNestControllersStack[activeLevelIndex]

    val isAnyLiveNestActive = activeLevelIndex > 0

    val selectedPointsPerLevel: List<Point?> =
        buildList {
            for (i in 0..activeLevelIndex) {
                add(liveNestControllersStack[i].nestedHit?.selectedPoint)
            }
        }

    val hoveredPoint = selectedPointsPerLevel.findLast { it != null }
    LaunchedEffect(hoveredPoint) {
        pointsService.selectOnyOne(hoveredPoint?.id)
    }

    val cycleActionsController = rememberCycleActionsController(
        currentAction = hoveredPoint,
        isDragging = isDragging
    )

    // When a non-base cycle stage is active, substitute the stage's action in the preview point
    // so actionsInCircle and AppPreviewTitle reflect the action that will fire on release.
    // Loop Over reuses the last stage's action with a temporary label; customIcon is cleared
    // whenever either the base or staged action is OpenCircleNest (mini-nest rings need null icon).
    val displayPoint: Point? = hoveredPoint?.let { hp ->
        val ca = hp.cycleActions
        if (ca.isNullOrEmpty()) return@let hp

        val idx = cycleActionsController.currentStageIndex
        if (idx > 0) {
            val staged = ca.getOrNull(idx - 1)?.action ?: return@let hp
            if (staged is Action.OpenCircleNest || hp.action is Action.OpenCircleNest)
                hp.copy(action = staged, customIcon = null)
            else {
                hp.copy(action = staged)
            }
        } else {
            hp
        }
    }

    // Reload the point icon depending on the action in the cycleController
    LaunchedEffect(hoveredPoint?.id, cycleActionsController.currentStageIndex) {
        if (!isDragging) return@LaunchedEffect
        val hp = hoveredPoint ?: return@LaunchedEffect
        if (hp.cycleActions.isNullOrEmpty()) return@LaunchedEffect
        val dp = displayPoint ?: return@LaunchedEffect
        iconsViewModel.reloadIcon(dp)
    }


    val holdAndRun = rememberHoldAndRunController(
        currentPoint = hoveredPoint,
        isDragging = isDragging
    ) { firedPoint ->
        onLaunch?.invoke(firedPoint)
    }


    LaunchedEffect(hoveredPoint?.id, liveNestControllersStack.count { it.isActive }) {
        hoveredPoint?.let { point ->
            if (!disableHapticFeedbackGlobally) {
                // Determine which circle/haptic map to use
                val hitNest = deepestController.nestedNest ?: return@let

                val nestHaptic = hitNest.haptic
                val targetShape =
                    hitNest.intersectionShapes.find { deepestController.nestedHit?.selectedPoint?.collidingShapeId == it.id }

                val hapticToPerform = (point.haptic ?: targetShape?.haptic ?: nestHaptic ?: defaultHapticFeedback())
                hapticToPerform.perform(ctx)
            }
        }
    }

    val haptic = LocalHapticFeedback.current
    // Haptic when entering the "cancel zone" of the Live Nest (Case B).
    LaunchedEffect(deepestController.nestedHit?.isInCancelZone) {
        if (isAnyLiveNestActive && deepestController.isActive && deepestController.nestedHit?.isInCancelZone == true && !disableHapticFeedbackGlobally) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }


    LaunchedEffect(isDragging) {
        if (!isDragging) {
            logI(SWIPE_TAG) { "Resolving app launch: isAliveNestActive: $isAnyLiveNestActive ($liveNestControllersStack)" }
            when {
                liveNestControllersStack[0].suppressMainLaunch -> {
                    logI(SWIPE_TAG) { "Aborted because suppressMainLaunch was true" }
                }

                holdAndRun.firedThisGesture -> {
                    logI(SWIPE_TAG) { "Aborted because hold and run already fired this gesture" }
                }

                else -> {
                    val nestedPoint = deepestController.resolveOnRelease()
                    if (nestedPoint != null) {
                        val stageAction = cycleActionsController.resolveOnRelease()
                        if (stageAction != null) {
                            onLaunch?.invoke(nestedPoint.copy(action = stageAction))
                        } else {
                            onLaunch?.invoke(nestedPoint)
                        }
                    }
                }
            }
        }
        liveNestControllersStack.forEach { it.clearAfterLaunch() }
        holdAndRun.clear()
        cycleActionsController.clear()
    }


    val showLineObjectPreview by AngleLineSettingsStore.showLineObjectPreview.asState()
    val showAngleLineObjectPreview by AngleLineSettingsStore.showAngleLineObjectPreview.asState()
    val showStartObjectPreview by AngleLineSettingsStore.showStartObjectPreview.asState()
    val showEndObjectPreview by AngleLineSettingsStore.showEndObjectPreview.asState()

    val pickedRememberShapeAngle = remember(isDragging) {
        (angleLineObject.shape ?: defaultAngleCustomObject.shape).resolveShape()
    }
    val pickedRememberRotationAngle = remember(isDragging) {
        angleLineObject.rotation
            ?.takeIf { it != -1 }
            ?: (0..360).random()
    }

    val pickedRememberShapeStart = remember(isDragging) {
        (startObject.shape ?: defaultStartCustomObject.shape).resolveShape()
    }
    val pickedRememberRotationStart = remember(isDragging) {
        startObject.rotation
            ?.takeIf { it != -1 }
            ?: (0..360).random()
    }

    val pickedRememberShapeEnd = remember(isDragging) {
        (endObject.shape ?: defaultEndCustomObject.shape).resolveShape()
    }
    val pickedRememberRotationEnd = remember(isDragging) {
        endObject.rotation
            ?.takeIf { it != -1 }
            ?: (0..360).random()
    }

    val multiplyOrSubtractOpacityInLiveNests by UiSettingsStore.multiplyOrSubtractOpacityInLiveNests.asState()

    /**
     * Alpha value for each layer: main nest, then each active Live Nest overlay (from deepest to shallowest).
     * The more the user go deeper, the more transparent first layers get
     * */
    val liveNestLayersAlphas: List<Float> = buildList {
        var alpha = 1f

        liveNestControllersStack.filter { it.isActive }.forEach { controller ->
            add(alpha)

            val percent =
                (controller.hostPoint?.liveNestMainNestOpacityPercent ?: defaultPoint.liveNestMainNestOpacityPercent)
                    .takeIf { it != -1 } ?: Point.defaultLiveNestMainNestOpacityPercent

            if (multiplyOrSubtractOpacityInLiveNests) {
                alpha -= percent.coerceIn(0, 100) / 100f
            } else {
                alpha *= percent.coerceIn(0, 100) / 100f
            }
        }
    }.reversed()

    val drawParams = rememberDrawParams(
        preventBgErasing = false,
        showConfiguratorDecorations = false,
        forceShowAllActionsInCurrentNest = false,
        allowShowPointCenter = false,
        hideSelectedPoint = false
    )

    Box(Modifier.fillMaxSize()) {

        MainScreenOverlayDebugInfos(
            hoveredPoint = hoveredPoint,
            selectedPointPerLevel = selectedPointsPerLevel,
            currentNest = currentNest,
            activeLevel = activeLevelIndex,
            isAliveNestActive = isAnyLiveNestActive,
            start = start,
            current = current,
            sweepAngle = deepestController.sweepAngleState.sweepAngle(),
            angle360 = deepestController.sweepAngleState.angle360(),
            isDragging = isDragging
        )


        if (isDragging) {
            for ((idx, controller) in liveNestControllersStack.withIndex()) {
                if (controller.isActive) {

                    val liveNestOpacity = liveNestLayersAlphas.getOrNull(idx) ?: continue

                    val isDeepestController = idx == activeLevelIndex

                    val nestedNestForDraw = controller.nestedNest!!
                    val liveNestCenterForDraw = controller.liveNestCenter!!
                    val hitResult = controller.nestedHit
                    val outerSelectedPoint = hitResult?.selectedPoint

                    val sweepAngle = controller.sweepAngleState.sweepAngle()
                    val angle360 = controller.sweepAngleState.angle360()

                    val effectiveCurrentPos: Offset = remember(current, hoveredPoint, isAnyLiveNestActive, activeLevelIndex) {
                        when {
                            linePreviewSnapToAction && outerSelectedPoint != null -> {
                                outerSelectedPoint.offset + liveNestCenterForDraw
                            }

                            // Means that the live HAS to snap to action, because otherwise it would move around under the top activated live nest
                            !isDeepestController -> {
                                liveNestControllersStack[idx + 1].liveNestCenter!!
                            }

                            else -> current
                        }
                    }

                    // Main canvas, uses drawWithCache to improve drawing performances
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = liveNestOpacity
                                compositingStrategy = CompositingStrategy.Offscreen
                            }
                            .drawBehind {
                                NestOverlay(
                                    center = liveNestCenterForDraw,
                                    nest = nestedNestForDraw,
                                    depth = 1,
                                    drawParams = drawParams,
                                    selectedAll = false,
                                )
                            }
                    ) {

                        drawIntoCanvas { canvas ->
                            val bounds = Rect(0f, 0f, size.width, size.height)
                            canvas.saveLayer(bounds, Paint())

                            val lineColor: Color =
                                if (rgbLine) Color.hsv(angle360, 1f, 1f)
                                else extraColors.angleLine

                            actionLine(
                                start = liveNestCenterForDraw,
                                end = effectiveCurrentPos,
                                sweepAngle = sweepAngle,
                                lineColor = lineColor,
                                order = order,
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
                                angleLineCustomObject = angleLineObject,
                                startCustomObject = startObject,
                                endCustomObject = endObject
                            )
                        }
                    }
                } else break
            }
        }
    }


    // Label on top of the screen.
    // Priority: inner Live Nest selection → outer Live Nest selection (with cycle stage) → main nest.
    if (showLaunchingAppLabel || showLaunchingAppIcon) {
        PointPreviewTitle(
            point = displayPoint,
            topPadding = appLabelIconOverlayTopPadding.dp,
            showLabel = showLaunchingAppLabel,
            showIcon = showLaunchingAppIcon
        )
    }
}


@Composable
private fun MainScreenOverlayDebugInfos(
    hoveredPoint: Point?,
    selectedPointPerLevel: List<Point?>,
    currentNest: Nest,
    activeLevel: Int,
    isAliveNestActive: Boolean,
    start: Offset?,
    current: Offset?,
    sweepAngle: Float,
    angle360: Float,
    isDragging: Boolean,
) {

    DebugZone(DebugSettingsStore.mainScreenDebugInfos) {
        Text("start = ${start?.let { "%.1f, %.1f".format(it.x, it.y) } ?: "-"}")
        Text("current = ${current?.let { "%.1f, %.1f".format(it.x, it.y) } ?: "-"}")
        Text("sweep raw = %.1f°".format(sweepAngle))
        Text("angle 0–360 = %.1f°".format(angle360))
        Text("drag = $isDragging")
        Text("activeLevel = $activeLevel")
        Text("isAliveNestActive = $isAliveNestActive")
        Text("selectedPointPerLevel = ${selectedPointPerLevel.map { it?.id }}")
        Text("current nest = $currentNest")
        Text("current point = $hoveredPoint")
    }
}


fun defaultHapticFeedback(): CustomHapticFeedback = CustomHapticFeedback.build {
    haptic(20)
}
