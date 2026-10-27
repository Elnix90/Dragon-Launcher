package org.elnix.dragonlauncher.animation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

// TODO

@Deprecated("remove and only use the shared ui")
fun <T> bouncySpec(): SpringSpec<T> =
	spring(
		dampingRatio = Spring.DampingRatioMediumBouncy,
		stiffness = Spring.StiffnessLow
	)
