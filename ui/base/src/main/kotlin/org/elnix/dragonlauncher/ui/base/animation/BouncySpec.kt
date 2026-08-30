package org.elnix.dragonlauncher.ui.base.animation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

fun <T> bouncySpec(): SpringSpec<T> =
    spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
