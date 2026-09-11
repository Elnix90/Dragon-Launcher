package org.elnix.dragonlauncher.ui.base.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.ktx.semiTransparentIfDisabled
import org.elnix.dragonlauncher.ui.base.animation.FancyAnimation
import org.elnix.dragonlauncher.ui.base.animation.rememberFancyAnimations
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.ui.base.withHaptic
import org.elnix.dragonlauncher.ui.base.withHapticParam

/**
 * Animated FAB, the basic overload. Takes a [Composable] lambda to file grained control the drawing inside th FAB.
 *
 * @param icon the [Composable] lambda to draw
 * @param modifier optional Compose [Modifier]
 * @param minSize the min size
 * @param enabled whether if the FAB is enabled, grayed out if not
 * @param interactionSource the optional interaction source
 * @param containerColor the container color
 * @param onClick the on click labda called upon user click
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnimatedFab(
    icon: @Composable BoxScope.() -> Unit,
    fabAnimation: FancyAnimation,
    modifier: Modifier = Modifier,
    minSize: Dp = 56.dp,
    enabled: Boolean = false,
    interactionSource: MutableInteractionSource,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    onClick: () -> Unit
) {
    val containerColor = containerColor.semiTransparentIfDisabled(enabled)
    Box(
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = fabAnimation.scale
                    scaleY = fabAnimation.scale
                    rotationZ = fabAnimation.outerRotation
                }.defaultMinSize(minWidth = minSize, minHeight = minSize)
                .clip(fabAnimation.shape)
                .background(containerColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = withHaptic(block = onClick)
                ),
        contentAlignment = Alignment.Center,
        content = icon
    )
}

/**
 * Animated FAB, the default overload for the project, takes only an [icon] in parameter and handles all the icon drawing by itself
 *
 * to control more in detail what's drawn, please use the other overload: [AnimatedFab]
 *
 * @see AnimatedFab
 *
 * @param icon the icon to be drawn inside the FAB
 * @param modifier optional Compose [Modifier]
 * @param enabled whether if the FAB is enabled, grayed out if not
 * @param minSize the min size
 * @param interactionSource the optional interaction source
 * @param containerColor the container color
 * @param onClick the on click labda called upon user click
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnimatedFab(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    minSize: Dp = 56.dp,
    enabled: Boolean = false,
    interactionSource: MutableInteractionSource = rememberInteractionSource(),
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    onClick: () -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()

    val fabAnimation =
        rememberFancyAnimations(
            isPressed = isPressed,
            normalShape = MaterialShapes.Cookie9Sided,
            pressedShape = MaterialShapes.Cookie7Sided
        )

    AnimatedFab(
        icon = {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = contentColorFor(containerColor),
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .rotate(fabAnimation.rotation - fabAnimation.outerRotation)
            )
        },
        fabAnimation = fabAnimation,
        modifier = modifier,
        minSize = minSize,
        enabled = enabled,
        interactionSource = interactionSource,
        containerColor = containerColor,
        onClick = onClick
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ToggleAnimatedFab(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    minSize: Dp = 56.dp,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    icon: (isPressed: Boolean) -> Int
) {
    val interactionSource = rememberInteractionSource()
    val buttonPressed by interactionSource.collectIsPressedAsState()

    val isActivated =
        if (buttonPressed) {
            !checked
        } else {
            checked
        }

    val fabAnimation =
        rememberFancyAnimations(
            isPressed = buttonPressed,
            normalShape = MaterialShapes.Cookie9Sided,
            pressedShape = MaterialShapes.Cookie7Sided
        )

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = fabAnimation.scale
                    scaleY = fabAnimation.scale

                    if (buttonPressed) {
                        rotationZ = fabAnimation.outerRotation
                    }
                }.defaultMinSize(minWidth = minSize, minHeight = minSize)
                .clip(fabAnimation.shape)
                .background(containerColor)
                .toggleable(
                    value = checked,
                    onValueChange = withHapticParam { onCheckedChange(!checked) },
                    interactionSource = interactionSource,
                    indication = null
                )
    ) {
        Icon(
            painter = painterResource(icon(isActivated)),
            contentDescription = null,
            tint = contentColorFor(containerColor),
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        if (buttonPressed) {
                            rotationZ = fabAnimation.rotation - fabAnimation.outerRotation
                        }
                    }
        )
    }
}
