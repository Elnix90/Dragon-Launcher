package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun rememberHoldToOpenSettings(
    onSettings: () -> Unit,
    holdDelay: Long = 500,     // ms before arc appears
    loadDuration: Long = 1000, // ms to fill arc
    tolerance: Float = 24f      // max movement allowed
): HoldGestureState {

    val scope = rememberCoroutineScope()

    var fingerDown by remember { mutableStateOf(false) }
    var anchor by remember { mutableStateOf<Offset?>(null) }
    var progress by remember { mutableFloatStateOf(0f) }

    fun reset() {
        fingerDown = false
        anchor = null
        progress = 0f
    }

    return remember {

        HoldGestureState(
            pointerModifier = Modifier.pointerInput(Unit) {

                awaitEachGesture {

                    val down = awaitFirstDown()
                    fingerDown = true
                    anchor = down.position
                    progress = 0f

                    val holdJob = scope.launch {
                        delay(holdDelay)

                        val startTime = System.currentTimeMillis()
                        while (fingerDown) {
                            val elapsed = System.currentTimeMillis() - startTime
                            progress = (elapsed.toFloat() / loadDuration).coerceIn(0f, 1f)

                            if (progress >= 1f) {
                                onSettings()
                                reset()
                                break
                            }
                            delay(16)
                        }
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
            progressProvider = { progress },
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
