package org.elnix.dragonlauncher.ui.base

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon

public object UiConstants {


    @Deprecated("Use new per-composables MaterialTheme.shapes")
    public const val DRAGON_SHAPE_CORNER_PERCENT: Int = 35


    @Deprecated("Use new per-composables MaterialTheme.shapes")
    public const val PRESSED_DRAGON_SHAPE_CORNER_PERCENT: Int = 20


    public val dragonSettingGroupPaddingValues: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp)


}