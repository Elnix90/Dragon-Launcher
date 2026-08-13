package org.elnix.dragonlauncher.ui.base.modifiers

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import org.elnix.dragonlauncher.ui.base.animation.defaultSpec
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.ui.base.withHaptic


@Composable
fun Modifier.shapedClickable(
    enabled: Boolean = true,
    isSelected: Boolean = false,
    hapticFeedback: Boolean = false,
    interactionSource: MutableInteractionSource = rememberInteractionSource(),
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier {

    val shape = provideClickableShape(
        interactionSource = interactionSource,
        isSelected = isSelected
    )

    val onclickWithOptionalHaptic = if (hapticFeedback) {
        withHaptic(HapticFeedbackType.LongPress) {
            onClick()
        }
    } else onClick
    return this
        .clip(shape)
        .combinedClickable(
            interactionSource = interactionSource,
            enabled = enabled,
            onClick = onclickWithOptionalHaptic,
            onLongClick = onLongClick
        )
}

@Composable
fun provideClickableShape(
    interactionSource: MutableInteractionSource,
    isSelected: Boolean = false
): Shape {
    val isPressed by interactionSource.collectIsPressedAsState()

    val shapeRound by animateIntAsState(
        targetValue = if (isPressed || isSelected) 20 else 35,
        label = "shape_anim",
        animationSpec = defaultSpec()
    )

    return RoundedCornerShape(shapeRound)
}