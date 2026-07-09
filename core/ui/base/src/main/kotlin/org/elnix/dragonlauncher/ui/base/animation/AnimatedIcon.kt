package org.elnix.dragonlauncher.ui.base.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.base.animation.AnimatedIconStatus.Default
import org.elnix.dragonlauncher.ui.base.animation.AnimatedIconStatus.Error
import org.elnix.dragonlauncher.ui.base.animation.AnimatedIconStatus.Success
import kotlin.time.Duration.Companion.milliseconds


/**
 * Animated icon status
 * there are 3 possible states for this component:
 *  [Default], [Success] and [Error]
 */
public enum class AnimatedIconStatus {
    Default, Success, Error
}


/**
 * Controller for animated icon status transitions.
 *
 * Manages state changes with automatic reset after delays.
 * Cancels previous jobs if new state is triggered before reset completes.
 *
 * @param scope CoroutineScope for launching state change animations
 */
public class AnimatedIcon(
    private val scope: CoroutineScope
) {
    public val status: SettingFlow<AnimatedIconStatus> = SettingFlow(Default)

    private var job: Job? = null


    /**
     * Sets icon to [AnimatedIconStatus.Error] state.
     *
     * Shows [AnimatedIconStatus.Error] icon for 500ms then returns to [AnimatedIconStatus.Default].
     * Cancels any previous pending state change.
     */
    public fun setError() {
        job?.cancel()
        job = scope.launch {
            status.value = Error
            delay(500.milliseconds)
            status.value = Default
        }
    }

    /**
     * Sets icon to [AnimatedIconStatus.Success] state.
     *
     * Shows [AnimatedIconStatus.Success] icon for 500ms then returns to [AnimatedIconStatus.Default].
     * Cancels any previous pending state change.
     */
    public fun setSuccess() {
        job = scope.launch {
            status.value = Success
            delay(500.milliseconds)
            status.value = Default
        }
    }
}


/**
 * Returns the appropriate painter for this status.
 *
 * @param defaultIcon Resource ID of the default icon
 * @return Painter for the current status ([AnimatedIconStatus.Default], [AnimatedIconStatus.Success], or [AnimatedIconStatus.Error] icon)
 */
@Composable
public fun AnimatedIconStatus.icon(defaultIcon: Int): Painter {
    return painterResource(
        when (this) {
            Default -> defaultIcon
            Success -> R.drawable.check
            Error -> R.drawable.close
        }
    )
}

/**
 * Creates and remembers an [AnimatedIcon] controller.
 *
 * Automatically manages the lifecycle with the composition.
 *
 * @return [AnimatedIcon] instance tied to current composition scope
 */
@Composable
public fun rememberClipboardIconController(): AnimatedIcon {
    val scope = rememberCoroutineScope()
    return remember { AnimatedIcon(scope) }
}
