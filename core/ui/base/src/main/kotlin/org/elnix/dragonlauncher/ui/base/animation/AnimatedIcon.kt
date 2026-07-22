package org.elnix.dragonlauncher.ui.base.animation

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.base.animation.AnimatedIconStatus.Default
import org.elnix.dragonlauncher.ui.base.animation.AnimatedIconStatus.Error
import org.elnix.dragonlauncher.ui.base.animation.AnimatedIconStatus.Success
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.modifiers.semiTransparentIfDisabled
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
public class AnimatedIcon
internal constructor(
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
private fun AnimatedIconStatus.icon(
    @DrawableRes
    defaultIcon: Int,
    @DrawableRes
    successIcon: Int,
    @DrawableRes
    errorIcon: Int
): Painter {
    return painterResource(
        when (this) {
            Default -> defaultIcon
            Success -> successIcon
            Error -> errorIcon
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
public fun rememberAnimatedIcon(): AnimatedIcon {
    val scope = rememberCoroutineScope()
    return remember { AnimatedIcon(scope) }
}


/**
 * Composable animated icon with state transitions.
 *
 * Displays icon that animates between [AnimatedIconStatus.Default], [AnimatedIconStatus.Success], and [AnimatedIconStatus.Error] states.
 * Icon is clickable and resets to default after animation completes.
 *
 * @param defaultIcon Resource ID of the default icon to display
 * @param onClick Callback when icon is clicked
 */
@Composable
public fun AnimatedIcon.Icon(
    @DrawableRes
    defaultIcon: Int,
    defaultColor: Color = MaterialTheme.colorScheme.onBackground,

    successIcon: Int = R.drawable.check,
    successColor: Color = MaterialTheme.colorScheme.secondary,

    @DrawableRes
    errorIcon: Int = R.drawable.close,
    errorColor: Color = MaterialTheme.colorScheme.error,

    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val status by this.status.asState()

    AnimatedContent(
        targetState = status,
        transitionSpec = { barsContentTransform }
    ) { status ->
        val painter = status.icon(defaultIcon, successIcon, errorIcon)

        Icon(
            painter = painter,
            contentDescription = null,
            tint = when (status) {
                Default -> defaultColor
                Success -> successColor
                Error -> errorColor
            },
            modifier = Modifier
                .semiTransparentIfDisabled(enabled)
                .clip(RoundedCornerShape(5.dp))
                .clickable(enabled = enabled, onClick = onClick)
                .padding(5.dp)
        )
    }
}