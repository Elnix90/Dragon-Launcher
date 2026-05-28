package org.elnix.dragonlauncher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.SWIPE_TAG
import org.elnix.dragonlauncher.common.messyfolder.circles.computePosition
import org.elnix.dragonlauncher.common.messyfolder.circles.scaleDragDistances
import org.elnix.dragonlauncher.common.messyfolder.resolveShape
import org.elnix.dragonlauncher.common.serializables.Nest
import org.elnix.dragonlauncher.common.serializables.CustomHapticFeedback
import org.elnix.dragonlauncher.common.serializables.SwipeAction
import org.elnix.dragonlauncher.common.serializables.Point
import org.elnix.dragonlauncher.common.serializables.Point.Companion.defaultSwipePointsValues
import org.elnix.dragonlauncher.common.utils.HapticUtils.performCustomHaptic
import org.elnix.dragonlauncher.logging.logI
import org.elnix.dragonlauncher.models.AppsViewModel
import org.elnix.dragonlauncher.settings.stores.AngleLineSettingsStore
import org.elnix.dragonlauncher.settings.stores.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.UiConstants
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.compositionslocals.LocalDisableHapticFeedbackGlobally
import org.elnix.dragonlauncher.ui.components.AppPreviewTitle
import org.elnix.dragonlauncher.ui.composition.LocalAngleLineObject
import org.elnix.dragonlauncher.ui.composition.LocalDefaultPoint
import org.elnix.dragonlauncher.ui.composition.LocalEndLineObject
import org.elnix.dragonlauncher.ui.composition.LocalLineObject
import org.elnix.dragonlauncher.ui.composition.LocalStartLineObject
import org.elnix.dragonlauncher.ui.dialogs.rememberLineObjectsOrder
import org.elnix.dragonlauncher.ui.dragon.components.DragonColumnGroup
import org.elnix.dragonlauncher.ui.helpers.customobjects.actionLine
import org.elnix.dragonlauncher.ui.helpers.nests.circlesSettingsOverlay
import org.elnix.dragonlauncher.ui.remembers.LiveNestState
import org.elnix.dragonlauncher.ui.remembers.rememberCycleActionsController
import org.elnix.dragonlauncher.ui.remembers.rememberHoldAndRunController
import org.elnix.dragonlauncher.ui.remembers.rememberLiveNestControllerStack
import org.elnix.dragonlauncher.ui.remembers.rememberSwipeDefaultParams

@Composable
fun MainScreenOverlay(
    appsViewModel: AppsViewModel = activityViewModel(),
    start: Offset?,
    current: Offset?,
    currentNest: Nest,
    onLaunch: ((Point) -> Unit)?
) {
    val ctx = LocalContext.current
    val extraColors = LocalExtraColors.current
    val defaultPoint = LocalDefaultPoint.current
    val disableHapticFeedbackGlobally = LocalDisableHapticFeedbackGlobally.current

    val lineObject = LocalLineObject.current
    val angleLineObject = LocalAngleLineObject.current
    val startObject = LocalStartLineObject.current
    val endObject = LocalEndLineObject.current

    val rgbLine by UiSettingsStore.rgbLine.asState()
    val debugInfos by DebugSettingsStore.debugInfos.asState()

    val showLaunchingAppLabel by UiSettingsStore.showLaunchingAppLabel.asState()
    val showLaunchingAppIcon by UiSettingsStore.showLaunchingAppIcon.asState()

    val appLabelIconOverlayTopPadding by UiSettingsStore.appLabelIconOverlayTopPadding.asState()
    val appLabelOverlaySize by UiSettingsStore.appLabelOverlaySize.asState()
    val appIconOverlaySize by UiSettingsStore.appIconOverlaySize.asState()

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
    val deepestController = liveNestControllersStack[activeLevelIndex]

    val targetCircle = deepestController.hostPoint?.circleNumber ?: -1

    val isAnyLiveNestActive = activeLevelIndex > 0

    val selectedPointsPerLevel: List<Point?> =
        buildList {
            for (i in 0..activeLevelIndex) {
                add(liveNestControllersStack[i].nestedHit?.selectedPoint)
            }
        }

    val hoveredPoint = selectedPointsPerLevel.findLast { it != null }

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
            if (staged is SwipeAction.OpenCircleNest || hp.action is SwipeAction.OpenCircleNest)
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
        appsViewModel.reloadPointIcon(dp)
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
                val hapticMap = deepestController.nestedNest?.haptic ?: emptyMap()
                val targetCircle = deepestController.nestedHit?.targetCircle ?: return@LaunchedEffect

                performCustomHaptic(ctx, (point.hapticFeedback ?: hapticMap[targetCircle] ?: defaultHapticFeedback(targetCircle)))
            }
        }
    }

    // Haptic when entering the "cancel zone" of the Live Nest (Case B).
    LaunchedEffect(deepestController.nestedHit?.isInCancelZone) {
        if (isAnyLiveNestActive && deepestController.isActive && deepestController.nestedHit?.isInCancelZone == true && !disableHapticFeedbackGlobally) {
            performCustomHaptic(ctx, defaultHapticFeedback(-1))
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
        (angleLineObject.shape ?: UiConstants.defaultAngleCustomObject.shape).resolveShape()
    }
    val pickedRememberRotationAngle = remember(isDragging) {
        angleLineObject.rotation
            ?.takeIf { it != -1 }
            ?: (0..360).random()
    }

    val pickedRememberShapeStart = remember(isDragging) {
        (startObject.shape ?: UiConstants.defaultStartCustomObject.shape).resolveShape()
    }
    val pickedRememberRotationStart = remember(isDragging) {
        startObject.rotation
            ?.takeIf { it != -1 }
            ?: (0..360).random()
    }

    val pickedRememberShapeEnd = remember(isDragging) {
        (endObject.shape ?: UiConstants.defaultEndCustomObject.shape).resolveShape()
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

            val percent = (controller.hostPoint?.liveNestMainNestOpacityPercent ?: defaultPoint.liveNestMainNestOpacityPercent).takeIf { it != -1 }
                ?: defaultSwipePointsValues.liveNestMainNestOpacityPercent!!
            if (multiplyOrSubtractOpacityInLiveNests) {
                alpha -= percent.coerceIn(0, 100) / 100f
            } else {
                alpha *= percent.coerceIn(0, 100) / 100f
            }
        }
    }.reversed()

    Box(Modifier.fillMaxSize()) {

        AnimatedVisibility(debugInfos) {
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
                isDragging = isDragging,
                targetCircle = targetCircle
            )
        }

        val drawParams = rememberSwipeDefaultParams(allowShowIconInCenter = true)

        /**
         *  Main nest (lines + rings + icons) and Live Nest overlay are split so the host can
         *  dim the main layer via [Point.liveNestMainNestOpacityPercent].
         */
        if (isDragging) {
            Box(Modifier.fillMaxSize()) {
                liveNestControllersStack.forEachIndexed { idx, controller ->
//                    val isRoot = idx == 0

                    val liveNestOpacity = liveNestLayersAlphas.getOrNull(idx) ?: return@forEachIndexed

                    if (controller.isActive) {
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
                                    val dragRadii = scaleDragDistances(nestedNestForDraw.dragDistances, controller.liveNestScale)
                                    val targetCircle = hitResult.targetCircle
                                    val radius = (dragRadii[targetCircle] ?: dragRadii[targetCircle])!!

                                    outerSelectedPoint.computePosition(
                                        radius = radius,
                                        center = liveNestCenterForDraw
                                    )
                                }

                                // Means that the live HAS to snap to action, because otherwise it would move around under the top activated live nest
                                !isDeepestController -> {
                                    liveNestControllersStack[idx + 1].liveNestCenter!!
                                }

                                else -> current
                            }
                        }

                        // Main canva, uses drawWithCache to improve drawing performances
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = liveNestOpacity
                                    compositingStrategy = CompositingStrategy.Offscreen
                                }
                                .drawWithCache {
                                    onDrawBehind {

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

                                        drawIntoCanvas { canvas ->
                                            val bounds = Rect(0f, 0f, size.width, size.height)
                                            canvas.saveLayer(bounds, Paint())

                                            val effectiveTargetCircle: Int = controller.nestedHit?.targetCircle ?: -1
                                            circlesSettingsOverlay(
                                                drawParams = drawParams,
                                                center = liveNestCenterForDraw,
                                                depth = 1,
                                                currentCircle = effectiveTargetCircle,
                                                circles = controller.scaledUiCircles,
                                                selectedPoint = outerSelectedPoint,
                                                nestId = nestedNestForDraw.id,
                                            )

                                            canvas.restore()
                                        }
                                    }
                                }
                        )
                    }
                }
            }
        }
    }


    // Label on top of the screen.
    // Priority: inner Live Nest selection → outer Live Nest selection (with cycle stage) → main nest.
    if (showLaunchingAppLabel || showLaunchingAppIcon) {
        AppPreviewTitle(
            point = displayPoint,
            topPadding = appLabelIconOverlayTopPadding.dp,
            labelSize = appLabelOverlaySize,
            iconSize = appIconOverlaySize,
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
    targetCircle: Int
) {

    DragonColumnGroup {
        CompositionLocalProvider(
            LocalContentColor provides Color.White,
            LocalTextStyle provides TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 12.sp
            )
        ) {
            Text("start = ${start?.let { "%.1f, %.1f".format(it.x, it.y) } ?: "—"}")
            Text("current = ${current?.let { "%.1f, %.1f".format(it.x, it.y) } ?: "—"}")
            Text("sweep raw = %.1f°".format(sweepAngle))
            Text("angle 0–360 = %.1f°".format(angle360))
            Text("drag = $isDragging")
            Text("activeLevel = $activeLevel")
            Text("isAliveNestActive = $isAliveNestActive")
            Text("target circle = $targetCircle")
            Text("selectedPointPerLevel = ${selectedPointPerLevel.map { it?.id?.substring(0, 5) }}")
            Text("current nest = $currentNest")
            Text("current point = $hoveredPoint")
        }
    }
}


fun defaultHapticFeedback(id: Int): CustomHapticFeedback = CustomHapticFeedback(
    listOf(
        true to
                when (id) {
                    -1 -> 5 // Cancel Zone, small feedback
                    0 -> 20  // First circle 20ms
                    else -> 20 + 20 * id // others: add 20ms each
                }
    )
)
