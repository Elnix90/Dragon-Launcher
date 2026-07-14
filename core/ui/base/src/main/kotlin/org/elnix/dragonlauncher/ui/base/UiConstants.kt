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

    /**
     * A selection of shapes for the pin that makes them pretty. not all of the [MaterialShapes] fit
     */
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    public val pinMaterialShapes: Set<RoundedPolygon> = setOf(
        MaterialShapes.Circle,
        MaterialShapes.Slanted,
        MaterialShapes.Arch,
        MaterialShapes.Arrow,
        MaterialShapes.Oval,
        MaterialShapes.Pill,
        MaterialShapes.Triangle,
        MaterialShapes.Diamond,
        MaterialShapes.Pentagon,
        MaterialShapes.Gem,
        MaterialShapes.Cookie4Sided,
        MaterialShapes.Cookie7Sided,
        MaterialShapes.Cookie9Sided,
        MaterialShapes.Cookie12Sided
    )


    /**
     * The shapes that are allowed to be picked into the nest shape picker.
     * This limitation is due to the heavy math required to compute the shape boundary.
     * The Ideal shape is the circle as we can very easily compute the intersection using simple math, but as soon as this becomes a more complicated shape, the result starts to be approximated.
     */
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    public val allowedNestShapes: Set<RoundedPolygon> = setOf(
        MaterialShapes.Circle,
//        TODO()
    )
}