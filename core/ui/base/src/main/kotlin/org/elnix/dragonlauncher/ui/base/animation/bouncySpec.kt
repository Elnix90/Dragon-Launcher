package org.elnix.dragonlauncher.ui.base.animation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.unit.IntOffset

// Copied from https://github.com/sosauce/Chocola/blob/469eef0e6bca3bc32a25f9fcd0cb5e157abe72f4/app/src/main/java/com/sosauce/chocola/utils/Extensions.kt#L507

fun <T> bouncySpec() = spring<T>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow
)

val navigationBouncySpec = spring<IntOffset>(Spring.DampingRatioLowBouncy, Spring.StiffnessLow)


val barsContentTransform = ContentTransform(
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


val slideInHorizontalBouncy = slideInHorizontally(bouncySpec()) { it } + fadeIn()
val slideOutHorizontalBouncy = slideOutHorizontally(bouncySpec()) { it } + fadeOut()

val slideInVerticalBouncy = slideInVertically(bouncySpec()) { it } + fadeIn()
val slideOutVerticalBouncy = slideOutVertically(bouncySpec()) { it } + fadeOut()
