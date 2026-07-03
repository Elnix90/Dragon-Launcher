package org.elnix.dragonlauncher.ui.remembers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.base.model.serializables.Point
import kotlin.time.Duration.Companion.milliseconds


/**
 * Snapshot of Hold & Run state returned per recomposition.
 *
 * @property firedThisGesture True once the action has been auto-fired during the current hold.
 *   Remains true until [clear] is called (on pointer-up or point exit).
 * @property clear Resets all state; call it from the overlay release guard.
 */
data class HoldAndRunState(
    val firedThisGesture: Boolean,
    val clear: () -> Unit
)


/**
 * Composable controller for Hold & Run behavior.
 *
 * Fires [onFire] once with the current [Point] after the configured
 * [Point.holdAndRunDelayMs] of continuous hold on the same point.
 * If [Point.holdAndRunAction] is set, the launched point uses that action;
 * otherwise the point’s main [Point.action] is used.
 *
 * - If the finger exits the point before the delay elapses, the coroutine is cancelled
 *   because [currentPoint] changes (or becomes null), restarting with a new key.
 * - When [currentPoint] is already null (Live Nest is active), Hold & Run does not run.
 * - [onFire] is called exactly once per gesture; [HoldAndRunState.firedThisGesture] stays true until [clear].
 *
 * @param currentPoint The currently hovered point on the main nest, or null if inactive.
 * @param isDragging    True while a finger is on screen.
 * @param onFire        Lambda invoked on the UI thread when the hold delay elapses
 */
@Composable
fun rememberHoldAndRunController(
    currentPoint: Point?,
    isDragging: Boolean,
    onFire: (point: Point) -> Unit
): HoldAndRunState {

    var firedThisGesture by remember { mutableStateOf(false) }

    LaunchedEffect(currentPoint?.id, isDragging) {
        // Always reset when the point changes or drag ends.
        firedThisGesture = false

        if (!isDragging || currentPoint == null) return@LaunchedEffect

        val delayMs = currentPoint.holdAndRunDelayMs?.toLong() ?: return@LaunchedEffect

        delay(delayMs.milliseconds)

        // Guard: still on the same point and not yet fired (safety for rapid transitions).
        if (!firedThisGesture) {
            firedThisGesture = true
            val override = currentPoint.holdAndRunAction
            val pointToLaunch =
                if (override != null) currentPoint.copy(action = override) else currentPoint
            onFire(pointToLaunch)
        }
    }

    val clear: () -> Unit = remember { { firedThisGesture = false } }

    return HoldAndRunState(
        firedThisGesture = firedThisGesture,
        clear = clear
    )
}
