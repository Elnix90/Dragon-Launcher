package org.elnix.dragonlauncher.ui.remembers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import io.github.elnix90.logging.SWIPE_TAG
import io.github.elnix90.logging.logD
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.base.model.models.HitResult
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.ktx.px
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import kotlin.time.Duration.Companion.milliseconds


/**
 * Snapshot of Live Nest state returned per recomposition.
 *
 * @property isActive True while the user is inside an active Live Nest overlay.
 * @property hostPoint The parent point whose hold triggered the Live Nest.
 * @property nestedNestId The [Nest] being rendered as a scaled overlay.
 * @property liveNestScale Scale applied to ring radii (0.3–1.0).
 * @property liveNestCenter Finger position at the moment Live Nest activated; used as the
 *   drawing center and hit-test origin so the overlay appears around the host point
 *   rather than at the gesture-start origin.
// * @property scaledIntersectionShapes Pre-computed scaled shapes list, ready for drawing.
 * @property nestedHit Real-time hit result while [isActive]; null otherwise.
 * @property suppressMainLaunch True after an abort - blocks main-nest action on release.
 */
data class LiveNestState(
    val isActive: Boolean,
    val hostPoint: Point?,
    val nestedNestId: Int?,
    val liveNestScale: Float,
    val liveNestCenter: Offset?,
//    val scaledIntersectionShapes: Set<IntersectionShape>,
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
    val resolveOnRelease: () -> Point?,
    /** Call after a successful nested launch to clean up all Live Nest state. */
    val clearAfterLaunch: () -> Unit
)


private data class NestLevelState(
    // All the mutable state from rememberLiveNestController
    var liveNestActive: Boolean = false,
    var hostPoint: Point? = null,
    var nestedNestId: Int? = null,
    var liveNestScale: Float = 0.5f,
    var liveNestCenter: Offset? = null,
    var suppressMainLaunch: Boolean = false,
    var timerResetBump: Int = 0,

    val sweepAngleState: SweepAngleState,

    // Refs
    var currentRef: Offset? = null,
    var releaseHitRef: HitResult? = null
)

@Composable
fun rememberLiveNestControllerStack(
    pointsViewModel: PointsViewModel = activityViewModel(),
    isDragging: Boolean,
    rootStartPos: Offset?,
    rootNestId: Int,
    current: Offset?,
): List<LiveNestState> {
    val pointsService = pointsViewModel.pointsService
    val defaultPoint by pointsService.defaultPoint.asState()
    val maxNestingDepth by UiSettingsStore.maxLiveNestsDepth.asState()

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
            level.currentRef = current
        }
    }

    val activeLevelIndex = nestStack.indexOfLast { it.liveNestActive }
    val isAnyLiveNestActive = activeLevelIndex > 0

    val rootHit = remember(
        resetTrigger,
        isAnyLiveNestActive,
        isDragging,
        current,
        rootNestId,
        rootStartPos,
        activeLevelIndex
    ) {

        if (!isDragging || current == null || rootStartPos == null || isAnyLiveNestActive) {
            null
        } else {
            pointsService.resolveLiveNestHit(
                normalizedPos = current - rootStartPos,
                nestId = rootNestId,
                liveNestScale = 1f,
                graceDistance = null
            ).also {
                sweepAngleStateStack[0].onAngleChanged(it.angle360)
            }
        }
    }


    // Hit-test for ALL levels
    val hitTests: List<HitResult?> = nestStack.mapIndexed { idx, level ->
        val isRoot = idx == 0

        // No need to remember since current is changing always
        when {
            isRoot -> rootHit

            // If a deeper level is active, FREEZE this level's hit
            activeLevelIndex > idx -> {
                // Return the last cached hit (don't update)
                level.releaseHitRef
            }
            
            !level.liveNestActive || level.liveNestCenter == null || current == null || level.nestedNestId == null -> null

            else -> {
                val graceDistance = level.hostPoint?.getLiveNestGraceDistance(defaultPoint)?.px

                val normalizedPos = current - level.liveNestCenter!!

                pointsService.resolveLiveNestHit(
                    normalizedPos = normalizedPos,
                    nestId = level.nestedNestId!!,
                    liveNestScale = level.liveNestScale,
                    graceDistance = graceDistance
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
                nestStack[idx].releaseHitRef = hit
            }
        }
    }

//    // Scaled circles for ALL levels
//    val scaledCircles: List<List<UiCircle>> = nestStack.mapIndexed { idx, level ->
//        val isRoot = idx == 0
//        remember(level.nestedNest, level.liveNestScale) {
//            if (isRoot) {
//                uiCirclesFromDragDistances(rootNest.dragDistances)
//            } else {
//                val nest = level.nestedNest ?: return@remember emptyList<UiCircle>()
//                uiCirclesFromScaledDragDistances(scaleDragDistances(nest.dragDistances, level.liveNestScale))
//            }
//        }
//    }

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
                level.nestedNestId = rootNestId
                level.liveNestScale = 1f
                level.liveNestCenter = rootStartPos
                level.liveNestActive = true
                return@LaunchedEffect
            }

            if (level.liveNestActive) return@LaunchedEffect
            if (!nestStack[idx - 1].liveNestActive) return@LaunchedEffect

            val targetNestId = currentPoint.liveNestTargetNestId ?: return@LaunchedEffect

            val delayMs = (
                    currentPoint.liveNestPreviewDelayMs
                        ?: defaultPoint.liveNestPreviewDelayMs
                        ?: Point.defaultLiveNestPreviewDelayMs
                    ).toLong()

            val scale = currentPoint.liveNestScale ?: defaultPoint.liveNestScale ?: Point.defaultLiveNestScale

//            val previousLiveNestCircles = scaledCircles[idx -1]
            val previousLiveNestCenter = nestStack[idx - 1].liveNestCenter ?: return@LaunchedEffect


            val currentPointOffset = currentPoint.getPos() + previousLiveNestCenter

            delay(delayMs.milliseconds)


            val snapToCenterPos = currentPoint.liveNestSnapsToFingerPosition ?: defaultPoint.liveNestSnapsToFingerPosition
            ?: Point.defaultLiveNestSnapsToFingerPosition
            val center = if (snapToCenterPos) {
                currentPointOffset
            } else {
                level.currentRef ?: return@LaunchedEffect
            }


            level.hostPoint = currentPoint
            level.nestedNestId = targetNestId
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
                nestStack[idx].nestedNestId = null
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
            nestedNestId = if (isRoot) rootNestId else level.nestedNestId,
            liveNestScale = level.liveNestScale,
            liveNestCenter = if (isRoot) rootStartPos else level.liveNestCenter,
//            scaledIntersectionShapes = scaledCircles[idx],
            nestedHit = level.releaseHitRef,
            suppressMainLaunch = level.suppressMainLaunch,
            sweepAngleState = sweepAngleStateStack[idx],
            resolveOnRelease = {
                val lastHit = level.releaseHitRef
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
                level.nestedNestId = null
                level.liveNestCenter = null
                level.suppressMainLaunch = false
                level.releaseHitRef = null

                resetTrigger++
            }
        )
    }
}