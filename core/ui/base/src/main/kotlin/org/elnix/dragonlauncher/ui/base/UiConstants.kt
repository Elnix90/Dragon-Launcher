package org.elnix.dragonlauncher.ui.base

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButtonShapes
import androidx.compose.material3.MaterialShapes
import androidx.compose.ui.unit.dp

object UiConstants {


    @Deprecated("Use new per-composables MaterialTheme.shapes")

    const val DRAGON_SHAPE_CORNER_PERCENT = 35


    @Deprecated("Use new per-composables MaterialTheme.shapes")

    const val PRESSED_DRAGON_SHAPE_CORNER_PERCENT = 20


    @Deprecated("Use new per-composables MaterialTheme.shapes")
    val DragonShape = RoundedCornerShape(16.dp)

    @Deprecated("Use new per-composables MaterialTheme.shapes")
    val PressedDragonShape = RoundedCornerShape(10.dp)

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    fun dragonIconButtonShapes(): IconButtonShapes =
        IconButtonShapes(
            shape = DragonShape,
            pressedShape = PressedDragonShape
        )


    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    val pinMaterialShapes = listOf(
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
}