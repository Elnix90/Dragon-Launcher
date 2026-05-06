package org.elnix.dragonlauncher.ui.base.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.ui.base.animation.bouncySpec
import org.elnix.dragonlauncher.ui.base.animation.rememberFancyAnimations
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.ui.base.withHaptic
import org.elnix.dragonlauncher.ui.base.withHapticParam


@Composable
fun AnimatedFab(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    minSize: Dp = 56.dp,
    interactionSource: MutableInteractionSource = rememberInteractionSource(),
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    onClick: () -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val fabAnimation = rememberFancyAnimations(isPressed)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = fabAnimation.scale
                scaleY = fabAnimation.scale
                rotationZ = fabAnimation.outerRotation
            }
            .defaultMinSize(minWidth = minSize, minHeight = minSize)
            .clip(fabAnimation.shape)
            .background(containerColor)
            .clickable(interactionSource = interactionSource, indication = null, onClick = withHaptic(block = onClick))
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = contentColorFor(containerColor),
            modifier = Modifier
                .align(Alignment.Center)
                .rotate(fabAnimation.rotation - fabAnimation.outerRotation)
        )
    }
}

@Composable
fun ToggleAnimatedFab(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    minSize: Dp = 56.dp,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    icon: (checkedProgress: Float) -> Int
) {

    val checkedProgress by
        animateFloatAsState(
            targetValue = if (checked) 1f else 0f,
            animationSpec = bouncySpec(),
        )
    val fabAnimation = rememberFancyAnimations(checkedProgress > .5f)

    Box(
        modifier = modifier
            .scale(fabAnimation.scale)
            .defaultMinSize(minWidth = minSize, minHeight = minSize)
            .clip(fabAnimation.shape)
            .background(containerColor)
            .toggleable(
                value = checked,
                onValueChange = withHapticParam { onCheckedChange(!checked) },
                interactionSource = null,
                indication = null
            )
    ) {
        Icon(
            painter = painterResource(icon(checkedProgress)),
            contentDescription = null,
            tint = contentColorFor(containerColor),
            modifier = Modifier
                .align(Alignment.Center)
                .rotate(fabAnimation.rotation)
        )
    }
}

