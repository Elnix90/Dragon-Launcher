package org.elnix.dragonlauncher.ui.remembers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

data class SweepAngleState(
    /**
     * The sweep angle to draw, in the range `-360f..360f`.
     * - Positive -> clockwise fill
     * - Negative -> anticlockwise fill
     * When a full clockwise turn is completed (cumulative hits 360),
     * continuing clockwise starts reducing from -360 upward, creating
     * a smooth unloading-then-reloading illusion.
     */
    val sweepAngle: () -> Float,
    /**
     * Feed this with the raw gesture angle in degrees (0..360) on every
     * pointer move event.
     */
    val onAngleChanged: (newAngle: Float) -> Unit,
    /** Resets all state, typically called on gesture end or cancel. */
    val reset: () -> Unit
)

/**
 * Angle360 on the receiver [Float] that is an angle but not wrapped.
 * The receiver could be an angle of 720° and this function converts it to the 0..360 range
 */
fun Float.angle360(): Int {
    val wrapped = this % 360f
    return (if (wrapped < 0f) wrapped + 360f else wrapped).toInt()
}

/**
 * Angle360 on the receiver [Int] that is an angle but not wrapped.
 * The receiver could be an angle of 720° and this function converts it to the 0..360 range
 */
fun Int.angle360(): Int {
    val wrapped = this % 360
    return if (wrapped < 0f) wrapped + 360 else wrapped
}

@Composable
fun rememberSweepAngle(): SweepAngleState {
    var cumulativeAngle by remember { mutableFloatStateOf(0f) }
    var lastRawAngle by remember { mutableFloatStateOf(0f) }

    return SweepAngleState(
        sweepAngle = {
            // cumulativeAngle grows freely - map it into -360..360
            // by folding at every 360 boundary with alternating sign
            when (val wrapped = cumulativeAngle % 720f) { // fold into -720..720
                // 0..360 -> clockwise fill: 0 -> 360
                in 0f..360f -> wrapped
                // 360..720 -> continue clockwise: start unloading anticlockwise -360 -> 0
                in 360f..720f -> wrapped - 720f
                // mirror for anticlockwise
                in -360f..0f -> wrapped
                in -720f..-360f -> wrapped + 720f
                else -> wrapped
            }
        },
        onAngleChanged = { newRaw ->
            var delta = newRaw - lastRawAngle

            // Wrap delta to [-180, 180] to handle the 0/360 crossing
            if (delta > 180f) delta -= 360f
            if (delta < -180f) delta += 360f

            cumulativeAngle += delta
            lastRawAngle = newRaw
        },
        reset = {
            cumulativeAngle = 0f
            lastRawAngle = 0f
        }
    )
}

/** Non-composable factory for SweepAngleState - creates a stateful angle tracker. */
fun createSweepAngleState(): SweepAngleState {
    var cumulativeAngle = 0f
    var lastRawAngle = 0f

    return SweepAngleState(
        sweepAngle = {
            when (val wrapped = cumulativeAngle % 720f) {
                in 0f..360f -> wrapped
                in 360f..720f -> wrapped - 720f
                in -360f..0f -> wrapped
                in -720f..-360f -> wrapped + 720f
                else -> wrapped
            }
        },
        onAngleChanged = { newRaw ->
            var delta = newRaw - lastRawAngle
            if (delta > 180f) delta -= 360f
            if (delta < -180f) delta += 360f
            cumulativeAngle += delta
            lastRawAngle = newRaw
        },
        reset = {
            cumulativeAngle = 0f
            lastRawAngle = 0f
        }
    )
}
