package org.elnix.dragonlauncher.ui.helpers.customobjects

import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Converts the given [value] to pixels only if it is higher than 0 or returns `null`
 * @param value the [Float] you want to convert to pixels
 * @return the [value] in pixels of `null`
 */
@OptIn(ExperimentalContracts::class)
public fun DrawScope.toPxOrNull(value: Float?): Float? {
    contract {
        returnsNotNull() implies (value != null)
    }

    return value?.takeIf { it > 0f }?.let { it * this.density }
}