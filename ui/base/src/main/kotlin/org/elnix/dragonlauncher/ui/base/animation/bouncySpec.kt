package org.elnix.dragonlauncher.ui.base.animation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.unit.IntOffset


fun <T> bouncySpec(): SpringSpec<T> = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow
)

fun <T> defaultSpec(): SpringSpec<T> = spring(
    dampingRatio = Spring.DampingRatioHighBouncy,
    stiffness = Spring.StiffnessHigh
)

val navigationBouncySpec: SpringSpec<IntOffset> = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)


val barsContentTransform: ContentTransform = ContentTransform(
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


val slideInHorizontalBouncy: EnterTransition = slideInHorizontally(bouncySpec()) { it } + fadeIn()
val slideOutHorizontalBouncy: ExitTransition = slideOutHorizontally(bouncySpec()) { it } + fadeOut()

val slideInVerticalBouncy: EnterTransition = slideInVertically(bouncySpec()) { it } + fadeIn()
val slideOutVerticalBouncy: ExitTransition = slideOutVertically(bouncySpec()) { it } + fadeOut()

val slideInVerticalBouncyUp: EnterTransition = slideInVertically(bouncySpec()) { -it } + fadeIn()
val slideOutVerticalBouncyUp: ExitTransition = slideOutVertically(bouncySpec()) { -it } + fadeOut()
