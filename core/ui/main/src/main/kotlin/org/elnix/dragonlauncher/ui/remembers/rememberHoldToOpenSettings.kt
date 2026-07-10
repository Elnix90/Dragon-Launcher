package org.elnix.dragonlauncher.ui.remembers

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
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
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.settings.stores.map.HoldToActivateArcSettingsStore
import kotlin.time.Duration.Companion.milliseconds


/**
 * Remember hold to open settings
 *
 * @param onSettings callback that fires when fully loaded
 * @param holdDelay how long to wait before circle starts showing (ms)
 * @param loadDuration how long to hold to fully load (ms)
 * @return [HoldGestureState] used in UI to get the pointer pos and the progress
 */
@Composable
public fun rememberHoldToOpenSettings(
    onSettings: (Offset) -> Unit,
    holdDelay: Long,
    loadDuration: Long,
): HoldGestureState {
    val scope = rememberCoroutineScope()
    val tolerance by HoldToActivateArcSettingsStore.holdToActivateSettingsTolerance.asState()

    var anchor: Offset? by remember { mutableStateOf(null) }
    val progress: Animatable<Float, AnimationVector1D> = remember {
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

                        delay(holdDelay.milliseconds)

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
public class HoldGestureState(
    public val pointerModifier: Modifier,
    public val progressProvider: () -> Float,
    public val centerProvider: () -> Offset?
)
