package org.elnix.dragonlauncher.ui.remembers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.acos
import kotlin.math.sqrt
import org.elnix.dragonlauncher.common.serializables.CircleNest
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable.Companion.defaultSwipePointsValues
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.SWIPE_TAG
import org.elnix.dragonlauncher.common.messyfolder.UiCircle
import org.elnix.dragonlauncher.common.messyfolder.circles.HitResult
import org.elnix.dragonlauncher.common.messyfolder.circles.computePosition
import org.elnix.dragonlauncher.common.messyfolder.circles.resolveLiveNestHit
import org.elnix.dragonlauncher.common.messyfolder.circles.scaleDragDistances
import org.elnix.dragonlauncher.common.messyfolder.circles.uiCirclesFromDragDistances
import org.elnix.dragonlauncher.common.messyfolder.circles.uiCirclesFromScaledDragDistances
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.settings.stores.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.composition.LocalDefaultPoint
import org.elnix.dragonlauncher.ui.composition.LocalNests
import org.elnix.dragonlauncher.ui.composition.LocalPoints

/*  ─────────────  Live Nest public state  ─────────────  */

/**
 * Snapshot of Live Nest state returned per recomposition.
 *
 * @property isActive True while the user is inside an active Live Nest overlay.
 * @property hostPoint The parent point whose hold triggered the Live Nest.
 * @property nestedNest The [CircleNest] being rendered as a scaled overlay.
 * @property liveNestScale Scale applied to ring radii (0.3–1.0).
 * @property liveNestCenter Finger position at the moment Live Nest activated; used as the
 *   drawing center and hit-test origin so the overlay appears around the host point
 *   rather than at the gesture-start origin.
 * @property scaledUiCircles Pre-computed scaled ring list, ready for drawing.
 * @property nestedHit Real-time hit result while [isActive]; null otherwise.
 * @property suppressMainLaunch True after an abort — blocks main-nest action on release.
 */
data class LiveNestState(
    val isActive: Boolean,
    val hostPoint: SwipePointSerializable?,
    val nestedNest: CircleNest?,
    val liveNestScale: Float,
    val liveNestCenter: Offset?,
    val scaledUiCircles: List<UiCircle>,
    val nestedHit: HitResult?,
    val suppressMainLaunch: Boolean,
    val sweepAngleState: SweepAngleState,
    /**
     * Resolve which action to fire on finger-up.
     * Returns:
     *  - the nested point (Case A)
     *  - the host point (Case B – cancel zone)
     *  - null (Cases C/F – aborted, or not active)
     */
    val resolveOnRelease: () -> SwipePointSerializable?,
    /** Call after a successful nested launch to clean up all Live Nest state. */
    val clearAfterLaunch: () -> Unit
)


private data class NestLevelState(
    // All the mutable state from rememberLiveNestController
    var liveNestActive: Boolean = false,
    var hostPoint: SwipePointSerializable? = null,
    var nestedNest: CircleNest? = null,
    var liveNestScale: Float = 0.5f,
    var liveNestCenter: Offset? = null,
    var suppressMainLaunch: Boolean = false,
    var timerResetBump: Int = 0,

    val sweepAngleState: SweepAngleState,

    // Refs
    val currentRef: MutableReference<Offset?> = MutableReference(null),
    val releaseHitRef: MutableReference<HitResult?> = MutableReference(null)
)

private class MutableReference<T>(var value: T)


@Composable
fun rememberLiveNestControllerStack(
    isDragging: Boolean,
    rootStartPos: Offset?,
    rootNest: CircleNest,
    current: Offset?,
): List<LiveNestState> {

    val nests = LocalNests.current
    val points = LocalPoints.current
    val defaultPoint = LocalDefaultPoint.current

    val maxNestingDepth by UiSettingsStore.maxLiveNestsDepth.asState()
    val pointsActionSnapsToOuterCircle by BehaviorSettingsStore.pointsActionSnapsToOuterCircle.asState()

    var resetTrigger by remember { mutableIntStateOf(0) }

    val sweepAngleStateStack: List<SweepAngleState> = remember(maxNestingDepth) {
        List(maxNestingDepth) { createSweepAngleState() }
    }

    val nestStack = remember(maxNestingDepth) {
        List(maxNestingDepth) { NestLevelState(sweepAngleState = sweepAngleStateStack[it]) }
    }

    // SideEffect: keep refs up-to-date
    SideEffect {
        nestStack.forEach { level ->
            level.currentRef.value = current
        }
    }

    val activeLevelIndex = nestStack.indexOfLast { it.liveNestActive }
    val isAnyLiveNestActive = activeLevelIndex > 0

    /* ─────────────  Fast activation: turn tracking  ───────────── */

    val recentPositions = remember { mutableListOf<Offset>() }
    var angleVersion by remember { mutableIntStateOf(0) }

    var currentSnap by remember { mutableStateOf(current) }
    SideEffect {
        currentSnap = current
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            val pos = currentSnap
            if (pos != null) {
                val last = recentPositions.lastOrNull()
                if (last == null || sqrt(
                        (pos.x - last.x) * (pos.x - last.x) +
                                (pos.y - last.y) * (pos.y - last.y)
                    ) >= 10f
                ) {
                    recentPositions.add(pos)
                    if (recentPositions.size > 20) {
                        repeat(recentPositions.size - 20) { recentPositions.removeFirst() }
                    }
                    angleVersion++
                }
            }
            delay(16)
        }
    }

    val hasSharpAngle = remember {
        derivedStateOf {
            angleVersion

            val pts = recentPositions
            val n = pts.size
            if (n < 4) return@derivedStateOf false

            val mid = n / 2
            val v1x = pts[mid].x - pts[0].x
            val v1y = pts[mid].y - pts[0].y
            val v2x = pts.last().x - pts[mid].x
            val v2y = pts.last().y - pts[mid].y

            val mag1 = sqrt(v1x * v1x + v1y * v1y)
            val mag2 = sqrt(v2x * v2x + v2y * v2y)
            if (mag1 <= 10f || mag2 <= 10f) return@derivedStateOf false

            val dot = v1x * v2x + v1y * v2y
            val cosAngle = (dot / (mag1 * mag2)).coerceIn(-1f, 1f)
            val turnAngle = acos(cosAngle) * 180f / kotlin.math.PI.toFloat()

            val strokeLen = sqrt(
                (pts.last().x - pts[0].x) * (pts.last().x - pts[0].x) +
                        (pts.last().y - pts[0].y) * (pts.last().y - pts[0].y)
            )

			// Should the treshold angle be configurable? Probably
			// Should the jitter threshold (minimum strokeLen) be configurable? Probably not?
            turnAngle > 30f && strokeLen >= 80f
        }
    }

    val rootHit = remember(
        resetTrigger,
        isAnyLiveNestActive,
        isDragging,
        current,
        rootNest,
        rootStartPos,
        activeLevelIndex
    ) {

        if (!isDragging || current == null || rootStartPos == null || isAnyLiveNestActive) {
            null
        } else {
            resolveLiveNestHit(
                center = rootStartPos,
                pointerPos = current,
                nestedNest = rootNest,
                liveNestScale = 1f,
                points = points,
                pointsActionSnapToOuterCircle = pointsActionSnapsToOuterCircle,
                graceDistancePx = -1f
            ).also {
                sweepAngleStateStack[0].onAngleChanged(it.angle360)
            }
        }
    }


    // Hit-test for ALL levels
    val hitTests: List<HitResult?> = nestStack.mapIndexed { idx, level ->
        val isRoot = idx == 0

        remember(
            resetTrigger,
            isRoot,
            level.liveNestActive,
            level.liveNestCenter,
            current,
            level.nestedNest,
            level.liveNestScale,
            pointsActionSnapsToOuterCircle,
            level.hostPoint,
            activeLevelIndex
        ) {
            when {
                isRoot -> rootHit

                // If a deeper level is active, FREEZE this level's hit
                activeLevelIndex > idx -> {
                    // Return the last cached hit (don't update)
                    level.releaseHitRef.value
                }


                !level.liveNestActive || level.liveNestCenter == null || current == null || level.nestedNest == null -> null

                else -> resolveLiveNestHit(
                    center = level.liveNestCenter!!,
                    pointerPos = current,
                    nestedNest = level.nestedNest!!,
                    liveNestScale = level.liveNestScale,
                    points = points,
                    pointsActionSnapToOuterCircle = pointsActionSnapsToOuterCircle,
                    graceDistancePx = (level.hostPoint?.liveNestGraceDistancePx ?: defaultPoint.liveNestGraceDistancePx ?: defaultSwipePointsValues.liveNestGraceDistancePx!!).toFloat()
                ).also {
                    sweepAngleStateStack[idx].onAngleChanged(it.angle360)
                }
            }
        }
    }

    // Keep releaseHitRef up-to-date BEFORE they freeze
    SideEffect {
        hitTests.forEachIndexed { idx, hit ->
            if (hit != null && activeLevelIndex <= idx) {
                // Only cache if this level is NOT frozen (not deeper than active)
                nestStack[idx].releaseHitRef.value = hit
            }
        }
    }

    // Scaled circles for ALL levels
    val scaledCircles: List<List<UiCircle>> = nestStack.mapIndexed { idx, level ->
        val isRoot = idx == 0
        remember(level.nestedNest, level.liveNestScale) {
            if (isRoot) {
                uiCirclesFromDragDistances(rootNest.dragDistances)
            } else {
                val nest = level.nestedNest ?: return@remember emptyList<UiCircle>()
                uiCirclesFromScaledDragDistances(scaleDragDistances(nest.dragDistances, level.liveNestScale))
            }
        }
    }

    // Reset on new gesture
    LaunchedEffect(isDragging) {
        if (isDragging) {
            nestStack.forEach { level ->
                level.suppressMainLaunch = false
                level.timerResetBump = 0
            }
        } else {
            nestStack.forEach { level ->
                level.liveNestCenter = null
            }
            recentPositions.clear()
            angleVersion++
        }
    }

    // Hold timers for each level
    nestStack.forEachIndexed { idx, level ->
        val isRoot = idx == 0

        LaunchedEffect(
            resetTrigger,
            rootHit?.selectedPoint,
            hitTests.getOrNull(idx - 1)?.selectedPoint?.id,
            isDragging,
            level.timerResetBump,
            activeLevelIndex
        ) {

            val currentPoint =
                if (isRoot) {
                    rootHit?.selectedPoint
                } else {
                    hitTests[idx - 1]?.selectedPoint
                } ?: return@LaunchedEffect

            if (!isDragging) return@LaunchedEffect

            if (isRoot) {
                level.hostPoint = currentPoint
                level.nestedNest = rootNest
                level.liveNestScale = 1f
                level.liveNestCenter = rootStartPos
                level.liveNestActive = true
                return@LaunchedEffect
            }

            if (level.liveNestActive) return@LaunchedEffect
            if (!nestStack[idx - 1].liveNestActive) return@LaunchedEffect

            val targetNestId = currentPoint.liveNestTargetNestId ?: return@LaunchedEffect

            val delayMs = (currentPoint.liveNestPreviewDelayMs ?: defaultPoint.liveNestPreviewDelayMs ?: defaultSwipePointsValues.liveNestPreviewDelayMs!!).toLong()
            val scale = currentPoint.liveNestScale ?: defaultPoint.liveNestScale ?: defaultSwipePointsValues.liveNestScale!!

            val previousLiveNestCircles = scaledCircles[idx -1]
            val previousLiveNestCenter = nestStack[idx -1].liveNestCenter ?: return@LaunchedEffect

            val currentPointOffset = currentPoint.computePosition(previousLiveNestCircles, previousLiveNestCenter)

            val pointFastActivation = currentPoint.fastActivation ?: defaultPoint.fastActivation ?: defaultSwipePointsValues.fastActivation!!

            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < delayMs) {
                if (pointFastActivation && hasSharpAngle.value) {
                    break
                }
                delay(32)
            }


            val snapToCenterPos = currentPoint.liveNestSnapsToFingerPosition ?: defaultPoint.liveNestSnapsToFingerPosition ?: defaultSwipePointsValues.liveNestSnapsToFingerPosition!!
            val center = if (snapToCenterPos) {
                currentPointOffset
            } else {
                level.currentRef.value ?: return@LaunchedEffect
            }

            val nest = nests.firstOrNull { it.id == targetNestId } ?: CircleNest(targetNestId)

            level.hostPoint = currentPoint
            level.nestedNest = nest
            level.liveNestScale = scale
            level.liveNestCenter = center
            level.liveNestActive = true
        }
    }

    // Bounds abort for each level
    hitTests.forEachIndexed { idx, hit ->
        LaunchedEffect(hit?.isOutsideBounds) {
            if (hit?.isOutsideBounds == true && nestStack[idx].liveNestActive) {
                nestStack[idx].liveNestActive = false
                nestStack[idx].hostPoint = null
                nestStack[idx].nestedNest = null
                nestStack[idx].liveNestCenter = null
                nestStack[idx].suppressMainLaunch = true
                nestStack[idx].timerResetBump++
            }
        }
    }

    return nestStack.mapIndexed { idx, level ->
        val isRoot = idx == 0

        LiveNestState(
            isActive = level.liveNestActive || isRoot,
            hostPoint = level.hostPoint,
            nestedNest = if (isRoot) rootNest else level.nestedNest,
            liveNestScale = level.liveNestScale,
            liveNestCenter = if (isRoot) rootStartPos else level.liveNestCenter,
            scaledUiCircles = scaledCircles[idx],
            nestedHit = level.releaseHitRef.value,
            suppressMainLaunch = level.suppressMainLaunch,
            sweepAngleState = sweepAngleStateStack[idx],
            resolveOnRelease = {
                val lastHit = level.releaseHitRef.value
                logD(SWIPE_TAG) { "Last hit: $lastHit" }
                when {
                    !level.liveNestActive -> null
                    lastHit == null -> null
                    lastHit.isOutsideBounds -> null
                    lastHit.isInCancelZone -> if (isRoot) null else level.hostPoint
                    lastHit.selectedPoint != null -> lastHit.selectedPoint
                    else -> level.hostPoint
                }
            },
            clearAfterLaunch = {
                level.liveNestActive = false
                level.hostPoint = null
                level.nestedNest = null
                level.liveNestCenter = null
                level.suppressMainLaunch = false
                level.releaseHitRef.value = null

                resetTrigger++
            }
        )
    }
}
