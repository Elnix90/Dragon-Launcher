package org.elnix.dragonlauncher.ui.base

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

object UiConstants {

    @Deprecated("Use new per-composables MaterialTheme.shapes")
    const val DRAGON_SHAPE_CORNER_PERCENT: Int = 35

    @Deprecated("Use new per-composables MaterialTheme.shapes")
    const val PRESSED_DRAGON_SHAPE_CORNER_PERCENT: Int = 20

    val dragonSettingGroupPaddingValues: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
}