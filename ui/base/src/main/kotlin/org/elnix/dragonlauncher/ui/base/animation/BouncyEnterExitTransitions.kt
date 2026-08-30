package org.elnix.dragonlauncher.ui.base.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

val slideInHorizontalBouncy: EnterTransition = slideInHorizontally(bouncySpec()) { it } + fadeIn()
val slideOutHorizontalBouncy: ExitTransition = slideOutHorizontally(bouncySpec()) { it } + fadeOut()

val slideInVerticalBouncy: EnterTransition = slideInVertically(bouncySpec()) { it } + fadeIn()
val slideOutVerticalBouncy: ExitTransition = slideOutVertically(bouncySpec()) { it } + fadeOut()

val slideInVerticalBouncyUp: EnterTransition = slideInVertically(bouncySpec()) { -it } + fadeIn()
val slideOutVerticalBouncyUp: ExitTransition = slideOutVertically(bouncySpec()) { -it } + fadeOut()
