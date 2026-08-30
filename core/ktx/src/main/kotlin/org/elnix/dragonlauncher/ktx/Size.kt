@file:Suppress("NOTHING_TO_INLINE")

package org.elnix.dragonlauncher.ktx

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize

/**
 * Quick ktx to create a [Size] whose [width][Size.width] and [height][Size.height] values are equals
 *
 * @param side value given to [width][Size.width] and [height][Size.height]
 * @return the newly created offset
 */
public inline fun Size.Companion.rect(side: Float): Size =
    Size(side, side)

/**
 * Quick ktx to create a [IntSize] whose [width][IntSize.width] and [height][IntSize.height] values are equals
 *
 * @param side value given to [width][IntSize.width] and [height][IntSize.height]
 * @return the newly created offset
 */
public inline fun IntSize.Companion.rect(side: Int): IntSize =
    IntSize(side, side)
