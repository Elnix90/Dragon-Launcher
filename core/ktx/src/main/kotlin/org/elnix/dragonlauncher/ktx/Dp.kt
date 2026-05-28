package org.elnix.dragonlauncher.ktx

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * Returns this dp value converted to raw pixels for the current screen density.
 *
 * This is a composable-only property that multiplies the underlying dp value by
 * the current density from [LocalDensity], making it convenient to use dp-based
 * dimensions with APIs that expect pixel values.
 */
inline val Dp.px: Float
    @Composable
    get() = value * LocalDensity.current.density


/** Create a [Dp] using an [Float], using local density for consistent results across different density devices */
@Stable
inline val Float.toDp: Dp
    @Composable
    get() = with(LocalDensity.current) { this@toDp.toDp() }
