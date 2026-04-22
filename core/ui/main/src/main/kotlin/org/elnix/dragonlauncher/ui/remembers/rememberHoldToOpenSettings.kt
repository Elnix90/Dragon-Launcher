package org.elnix.dragonlauncher.ui.remembers

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * Remember hold to open settings
 * Handles the drawing of the [org.elnix.dragonlauncher.ui.helpers.HoldToActivateArc]
 *
 * @param onSettings callback that fires when fully loaded
 * @param holdDelay how long to wait before circle starts showing
 * @param loadDuration how long to hold to fully load
 * @param tolerance how much finger can move awai from the starting point before canceling the loading
 * @receiver
 * @return
 */
@Composable
fun rememberHoldToOpenSettings(
    onSettings: (Offset) -> Unit,
    holdDelay: Long,     // ms before arc appears
    loadDuration: Long, // ms to fill arc
    tolerance: Float     // max movement allowed
): HoldGestureState {

    val scope = rememberCoroutineScope()

    var anchor by remember { mutableStateOf<Offset?>(null) }
    val progress = remember {
        Animatable(0f)
    }


    fun reset() {
        anchor = null
        scope.launch {
            progress.snapTo(0f)
        }
    }



    return remember(holdDelay, loadDuration, tolerance, onSettings) {
        HoldGestureState(
            pointerModifier = Modifier.pointerInput(Unit) {

                awaitEachGesture {

                    val down = awaitFirstDown()
                    anchor = down.position

                    val holdJob = scope.launch {
                        progress.snapTo(0f)

                        delay(holdDelay)


                        progress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = loadDuration.toInt(),
                                easing = LinearEasing
                            )
                        )

                        onSettings(down.position)
                        reset()
                    }

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }

                        if (change == null || !change.pressed) {
                            holdJob.cancel()
                            reset()
                            break
                        }

                        // Check drag distance
                        val dist = anchor?.let {
                            (change.position - it).getDistance()
                        } ?: 999f

                        if (dist > tolerance) {
                            holdJob.cancel()
                            reset()
                            break
                        }

                        change.consume()
                    }
                }
            },
            progressProvider = { progress.value },
            centerProvider = { anchor }
        )
    }
}

/** Container for the produced gesture state. */
class HoldGestureState(
    val pointerModifier: Modifier,
    val progressProvider: () -> Float,
    val centerProvider: () -> Offset?
)
