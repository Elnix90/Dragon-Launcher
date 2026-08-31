package org.elnix.dragonlauncher.ui.base.animation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

fun <T> defaultSpec(): SpringSpec<T> =
    spring(
        dampingRatio = Spring.DampingRatioHighBouncy,
        stiffness = Spring.StiffnessHigh
    )
