package org.elnix.dragonlauncher.ui.base.modifiers

import androidx.annotation.IntRange
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
    onClick: () -> Unit
): Modifier {
    val shape =
        provideClickableShape(
            interactionSource = interactionSource,
            isSelected = isSelected
        )

    val onclickWithOptionalHaptic =
        if (hapticFeedback) {
            withHaptic(HapticFeedbackType.LongPress) {
                onClick()
            }
        } else {
            onClick
        }
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
    isSelected: Boolean = false,
    @IntRange(0, 100)
    defaultRoundingPercent: Int = 35,
    @IntRange(0, 100)
    pressedRoundingPercent: Int = 20
): Shape {
    val isPressed by interactionSource.collectIsPressedAsState()

    val shapeRound by animateIntAsState(
        targetValue = if (isPressed || isSelected) pressedRoundingPercent else defaultRoundingPercent,
        label = "shape_anim",
        animationSpec = defaultSpec()
    )

    return RoundedCornerShape(shapeRound)
}

// private val defaultDefaultShape = RoundedPolygon.rectangle(
//    rounding = CornerRounding(35f),
// ).normalized()
//
//
// private val defaultPressedShape = RoundedPolygon.rectangle(
//    rounding = CornerRounding(20f),
// ).normalized()
//
//
// @Composable
// fun provideClickableShape(
//    interactionSource: MutableInteractionSource,
//    isSelected: Boolean = false,
//    defaultShape: RoundedPolygon = defaultDefaultShape,
//    pressedShape: RoundedPolygon = defaultPressedShape,
//    roundedPolygon: Boolean
// ): Shape {
//    val isPressed by interactionSource.collectIsPressedAsState()
//
//    val pressedProgress by animateFloatAsState(
//        targetValue = if (isPressed || isSelected) 1f else 0f,
//        animationSpec = bouncySpec()
//    )
//
//    val morph = remember { Morph(defaultShape, pressedShape) }
//
//    val shape = remember(morph, pressedProgress) {
//        MorphPolygonShape(morph, pressedProgress)
//    }
//
//    return shape
// }
