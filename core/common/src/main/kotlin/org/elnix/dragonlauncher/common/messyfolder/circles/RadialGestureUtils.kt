package org.elnix.dragonlauncher.common.messyfolder.circles

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.SWIPE_TAG
import org.elnix.dragonlauncher.common.messyfolder.UiCircle
import org.elnix.dragonlauncher.common.serializables.CircleNest
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable
import org.elnix.dragonlauncher.logging.logD
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/*  ─────────────  Basic geometry helpers  ─────────────  */

/** Smallest angular difference between two angles in degrees, result in [0, 180]. */
fun angularDistanceDeg(a: Double, b: Float): Double {
    val d = abs(a - b)
    return minOf(d, 360.0 - d)
}

/** Angle 0–360 from [offset] relative to [center] (north = 0, clockwise). */
fun angle360FromOffset(center: Offset, offset: Offset): Float {
    val dx = offset.x - center.x
    val dy = offset.y - center.y
    val angleRad = atan2(dx.toDouble(), -dy.toDouble())
    var deg = Math.toDegrees(angleRad).toFloat()
    if (deg < 0f) deg += 360f
    return deg
}

/** Euclidean distance from [center] to [offset] in pixels. */
fun distFromCenter(center: Offset, offset: Offset): Float =
    hypot(offset.x - center.x, offset.y - center.y)

/*  ─────────────  Ring resolution (integer drag distances)  ─────────────  */

/**
 * Main-overlay style ring selection from integer [dragDistances].
 *
 * When [snapToOuterCircle] is true the innermost ring whose threshold is
 * still >= [dist] wins; otherwise the outermost ring whose threshold has
 * been crossed wins (classic Dragon Launcher behaviour).
 */
fun computeTargetCircleFromDist(
    dist: Float,
    dragDistances: Map<Int, Int>,
    snapToOuterCircle: Boolean
): Int {
    if (dragDistances.isEmpty()) return -1
    return if (snapToOuterCircle) {
        var best: Map.Entry<Int, Int>? = null
        for (entry in dragDistances) {
            if (dist <= entry.value) {
                if (best == null || entry.value < best.value) best = entry
            }
        }
        best?.key ?: dragDistances.maxByOrNull { it.value }!!.key
    } else {
        var best: Map.Entry<Int, Int>? = null
        for (entry in dragDistances) {
            if (dist >= entry.value) {
                if (best == null || entry.value > best.value) best = entry
            }
        }
        best?.key ?: dragDistances.minByOrNull { it.value }!!.key
    }
}

/*  ─────────────  Ring resolution (float / scaled drag distances)  ─────────────  */

/**
 * Same logic as [computeTargetCircleFromDist] but for pre-scaled float thresholds,
 * used by the Live Nest overlay after [scaleDragDistances] is applied.
 */
fun computeTargetCircleFromDistFloat(
    dist: Float,
    dragDistances: Map<Int, Float>,
    pointActionSnapToOuterCircle: Boolean
): Int {
    if (dragDistances.isEmpty()) return -1
    return if (pointActionSnapToOuterCircle) {
        var best: Map.Entry<Int, Float>? = null
        for (entry in dragDistances) {
            if (dist <= entry.value) {
                if (best == null || entry.value < best.value) best = entry
            }
        }
        best?.key ?: dragDistances.maxByOrNull { it.value }!!.key
    } else {
        var best: Map.Entry<Int, Float>? = null
        for (entry in dragDistances) {
            if (dist >= entry.value) {
                if (best == null || entry.value > best.value) best = entry
            }
        }
        best?.key ?: dragDistances.minByOrNull { it.value }!!.key
    }
}

/*  ─────────────  Point-on-ring selection  ─────────────  */

/**
 * Selects the closest point on [targetCircle] from [candidates] by angle,
 * subject to the per-circle minimum-angle gate in [minAngles].
 *
 * Returns null when no candidate falls within the angle tolerance.
 */
fun selectPointOnRing(
    candidates: List<SwipePointSerializable>,
    angle360: Float,
    targetCircle: Int,
    minAngles: Map<Int, Int>
): SwipePointSerializable? {
    val onRing = candidates.filter { it.circleNumber == targetCircle }
    val closest = onRing.minByOrNull { angularDistanceDeg(it.angleDeg, angle360) } ?: return null
    val minAngle = minAngles[targetCircle] ?: 0
    if (minAngle == 0) return closest
    val shortest = angularDistanceDeg(closest.angleDeg, angle360)
    return if (shortest <= minAngle) closest else null
}

/*  ─────────────  Scale helpers  ─────────────  */

/** Returns a copy of [dragDistances] with every value multiplied by [scale]. */
fun scaleDragDistances(dragDistances: Map<Int, Int>, scale: Float): Map<Int, Float> =
    dragDistances.mapValues { (_, v) -> v * scale }

/**
 * Outer radius of the nest (px) from a pre-scaled distances map.
 * The cancel-zone key (-1) is excluded because it is not a real ring boundary.
 */
fun outerRadiusPx(scaled: Map<Int, Float>): Float =
    scaled.filter { it.key != -1 }.values.maxOrNull() ?: 0f

/*  ─────────────  Live Nest hit-test  ─────────────  */

/**
 * Resolved result of one pointer position against a Live Nest nest.
 *
 * @property targetCircle The resolved ring index (-1 = cancel zone).
 * @property selectedPoint The best matching point, or null when outside angle tolerance or empty.
 * @property isOutsideBounds True when the pointer has moved beyond the outermost ring.
 * @property isInCancelZone True when [targetCircle] == -1.
 */
data class HitResult(
    val targetCircle: Int,
    val selectedPoint: SwipePointSerializable?,
    val isOutsideBounds: Boolean,
    val angle360: Float
) {
    val isInCancelZone = targetCircle == -1
}

/**
 * Resolves a pointer position against a scaled Live Nest.
 *
 * All geometry reuses the same rules as `MainScreenOverlay` — [scaleDragDistances],
 * [computeTargetCircleFromDistFloat], [selectPointOnRing] — with the only addition
 * being a bounds check via [outerRadiusPx].
 *
 * @param center Gesture origin (same `start` used throughout the drag).
 * @param pointerPos Current finger position.
 * @param nestedNest The [CircleNest] that will be rendered as a Live Nest.
 * @param liveNestScale Scale factor applied to all radii (0.3–1.0).
 * @param points All swipe points; filtered internally to [nestedNest].id.
 * @param pointsActionSnapToOuterCircle Same flag as `BehaviorSettingsStore.pointsActionSnapsToOuterCircle`.
 * @param graceDistancePx Extra tolerance (px) added beyond the outer ring before exit fires.
 *   `0f` means strict bounds (default behavior). `-1f` means no bounds
 */
fun resolveLiveNestHit(
    center: Offset,
    pointerPos: Offset,
    nestedNest: CircleNest,
    liveNestScale: Float,
    points: List<SwipePointSerializable>,
    pointsActionSnapToOuterCircle: Boolean,
    graceDistancePx: Float = 0f
): HitResult {
    val scaledDistances = scaleDragDistances(nestedNest.dragDistances, liveNestScale)
    val outerRadius = outerRadiusPx(scaledDistances)
    val dist = distFromCenter(center, pointerPos)
    val angle360 = angle360FromOffset(center, pointerPos)

    /*  ─── Bounds check (Case C / F) ───  */
    graceDistancePx.takeIf{ it > -1 }?.let {
        if (outerRadius > 0f && dist > outerRadius + graceDistancePx) {
            return HitResult(
                targetCircle = -1,
                selectedPoint = null,
                isOutsideBounds = true,
                angle360 = angle360
            )
        }
    }

    val targetCircle = computeTargetCircleFromDistFloat(dist, scaledDistances, pointsActionSnapToOuterCircle)
    val isInCancelZone = targetCircle == -1

    // When inside the cancel zone there is no point to select.
    val selectedPoint = if (isInCancelZone) {
        null
    } else {
        val nestPoints = points.filter { (it.nestId ?: 0) == nestedNest.id }
        selectPointOnRing(nestPoints, angle360, targetCircle, nestedNest.minAngleActivation)
    }

    return HitResult(
        targetCircle = targetCircle,
        selectedPoint = selectedPoint,
        isOutsideBounds = false,
        angle360 = angle360
    )
}

/*  ─────────────  Shared UiCircle builder  ─────────────  */

/**
 * Builds a [UiCircle] list from integer [dragDistances], skipping the cancel-zone key (-1).
 * Used by both `MainScreenOverlay` and the Live Nest drawing layer so the construction
 * logic stays consistent when one side changes.
 */
fun uiCirclesFromDragDistances(dragDistances: Map<Int, Int>): List<UiCircle> =
    dragDistances
        .filter { it.key != -1 }
        .map { (id, radius) -> UiCircle(id = id, radius = radius.toFloat()) }

/**
 * Float-valued variant used for the scaled Live Nest ring list.
 */
fun uiCirclesFromScaledDragDistances(scaledDistances: Map<Int, Float>): List<UiCircle> =
    scaledDistances
        .filter { it.key != -1 }
        .map { (id, radius) -> UiCircle(id = id, radius = radius) }


fun createCirclesFromDragDistances(
    dragDistances: Map<Int, Int>,
    circles: SnapshotStateList<UiCircle>,
) {
    circles.clear()

    uiCirclesFromDragDistances(dragDistances).forEach {
        circles.add(it)
    }
}

fun createCirclesFromDragDistancesWithCustomIncrement(
    dragDistances: Map<Int, Int>,
    circles: SnapshotStateList<UiCircle>,
    baseRadius: Float,
    circlesWidthIncrement: Float
) {
    circles.clear()

    uiCirclesFromDragDistances(dragDistances).forEach { (circleNumber, _) ->
        val radius = circlesWidthIncrement * (circleNumber + 1) * baseRadius

        circles.add(UiCircle(circleNumber, radius))
    }
}


inline fun Offset.undoRotation(
    angle: () -> Float
): Offset {
    val angleRad = Math.toRadians(angle().toDouble())
    val cos = cos(angleRad).toFloat()
    val sin = sin(angleRad).toFloat()

    logD(SWIPE_TAG) { "TRANSFORM:\nangleRad: $angleRad\ncosA: $cos | sinA: $sin"}

//    val unrotatedX = this.x * cos + this.y * sin
//    val unrotatedY = this.x * sin - this.y * cos

    val unrotatedX = this.x * cos - this.y * sin
    val unrotatedY = this.x * sin + this.y * cos

    return Offset(
        unrotatedX,
        unrotatedY
    )
}

inline fun Offset.undoScale(
    zoom: () -> Float
): Offset {
    val zoom = zoom()
    return Offset(
        this.x / zoom,
        this.y / zoom
    )
}

inline fun Offset.undoTranslation(
    translation: () -> Offset
): Offset {
    val translation = translation()
    return Offset(
        this.x + translation.x,
        this.y + translation.y
    )
}