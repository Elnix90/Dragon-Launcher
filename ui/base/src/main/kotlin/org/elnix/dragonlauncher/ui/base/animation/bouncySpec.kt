package org.elnix.dragonlauncher.ui.base.animation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.unit.IntOffset


public fun <T> bouncySpec(): SpringSpec<T> = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow
)

public fun <T> defaultSpec(): SpringSpec<T> = spring(
    dampingRatio = Spring.DampingRatioHighBouncy,
    stiffness = Spring.StiffnessHigh
)

public fun <T> easingSpec(): TweenSpec<T> = tween(
    durationMillis = 300,
    easing = FastOutSlowInEasing
)

public val navigationBouncySpec: SpringSpec<IntOffset> = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)


public val barsContentTransform: ContentTransform = ContentTransform(
    targetContentEnter = slideInVertically(
        spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    ) { it } + fadeIn(),
    initialContentExit = slideOutVertically(
        spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    ) { it } + fadeOut(),
    sizeTransform = SizeTransform(clip = false) // prevents the content from getting clipped during bounce
)


public val slideInHorizontalBouncy: EnterTransition = slideInHorizontally(bouncySpec()) { it } + fadeIn()
public val slideOutHorizontalBouncy: ExitTransition = slideOutHorizontally(bouncySpec()) { it } + fadeOut()

public val slideInVerticalBouncy: EnterTransition = slideInVertically(bouncySpec()) { it } + fadeIn()
public val slideOutVerticalBouncy: ExitTransition = slideOutVertically(bouncySpec()) { it } + fadeOut()
