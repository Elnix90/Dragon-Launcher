package org.elnix.dragonlauncher.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.navigation3.ui.NavDisplay
import org.elnix.dragonlauncher.ui.base.animation.navigationBouncySpec

fun slideFadeInFromRight(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { (it * 0.15f).toInt() },
        animationSpec = tween(250, easing = FastOutSlowInEasing)
    ) + fadeIn(
        initialAlpha = 0.5f,
        animationSpec = tween(200, delayMillis = 66, easing = LinearEasing)
    )
}

fun slideFadeOutToRight(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { (it * 0.10f).toInt() },
        animationSpec = tween(250, easing = FastOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(50, easing = LinearEasing)
    )
}

fun slideFadeInFromLeft(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { -(it * 0.15f).toInt() },
        animationSpec = tween(350, easing = FastOutSlowInEasing)
    ) + fadeIn(
        initialAlpha = 0.5f,
        animationSpec = tween(50, delayMillis = 66, easing = LinearEasing)
    )
}

fun slideFadeOutToLeft(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { -(it * 0.10f).toInt() },
        animationSpec = tween(250, easing = FastOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(50, easing = LinearEasing)
    )
}

fun raiseUpAnimation() =
    fadeIn(
        animationSpec = tween(50),
        initialAlpha = 0.5f
    ) + slideInVertically(
        initialOffsetY = { it / 2 },
        animationSpec = tween(100, easing = FastOutSlowInEasing)
    )

fun collapseDownAnimation() =
    fadeOut(
        animationSpec = tween(50),
        targetAlpha = 0f
    ) + slideOutVertically(
        targetOffsetY = { it / 2 },
        animationSpec = tween(50, easing = LinearOutSlowInEasing)
    )


val verticalMetadata = NavDisplay.transitionSpec {
    slideInVertically(navigationBouncySpec) { it } + fadeIn() togetherWith fadeOut()
}

val horizontalMetadata = NavDisplay.transitionSpec {
    slideInHorizontally(navigationBouncySpec) { it } + fadeIn() togetherWith fadeOut()
}