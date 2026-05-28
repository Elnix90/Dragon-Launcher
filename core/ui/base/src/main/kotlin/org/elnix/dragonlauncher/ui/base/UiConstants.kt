package org.elnix.dragonlauncher.ui.base

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButtonShapes
import androidx.compose.material3.MaterialShapes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.common.serializables.CustomGlow
import org.elnix.dragonlauncher.common.serializables.CustomObject
import org.elnix.dragonlauncher.common.serializables.IconShape

object UiConstants {

    val DRAGON_SHAPE_CORNER_DP = 12.dp
    val PRESSED_DRAGON_SHAPE_CORNER_DP = 5.dp

    val DragonShape = RoundedCornerShape(DRAGON_SHAPE_CORNER_DP)
    val PressedDragonShape = androidx.compose.foundation.shape.RoundedCornerShape(PRESSED_DRAGON_SHAPE_CORNER_DP)

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    fun dragonShapes(): ButtonShapes =
        ButtonShapes(
            shape = DragonShape,
            pressedShape = PressedDragonShape
        )

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    fun dragonIconButtonShapes(): IconButtonShapes =
        IconButtonShapes(
            shape = DragonShape,
            pressedShape = PressedDragonShape
        )

    val defaultLineCustomObject = CustomObject(
        stroke = 2f,
        color = null, // RGB Color according to the angle
        glow = CustomGlow(
            radius = 10f
        ),

        /** Not used for the line as it goes from `start` to `end` */
        shape = null,
        size = null,
        rotation = null, // No rotation for line, (it's nullable, but I use nul here to indicate that the rotation isn't available)

        eraseBackground = false
    )


    val defaultAngleCustomObject = CustomObject(
        stroke = 2f,
        color = null, // RGB Color according to the angle
        glow = CustomGlow(
            radius = 20f
        ),
        shape = IconShape.Circle,
        size = 50f,
        rotation = 90,
        eraseBackground = false
    )

    val defaultStartCustomObject = CustomObject(
        stroke = 4f,
        color = null, // RGB Color according to the angle
        glow = CustomGlow(
            radius = 32f
        ),
        shape = IconShape.Circle,
        size = 30f,
        rotation = 0,
        eraseBackground = true
    )

    val defaultEndCustomObject = CustomObject(
        stroke = 0f,
        color = null, // RGB Color according to the angle
        glow = CustomGlow(
            radius = 12f
        ),
        shape = IconShape.Circle,
        size = 8f,
        rotation = 0,
        eraseBackground = false
    )


    val defaultHoldCustomObject = CustomObject(
        stroke = 10f,
        color = Color.Red,
        glow = CustomGlow(
            radius = 12f
        ),
        shape = IconShape.Circle,
        size = 70f,
        rotation = 0,
        eraseBackground = false
    )


    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    val allMaterialShapes = listOf(
        MaterialShapes.Circle,
        MaterialShapes.Square,
        MaterialShapes.Slanted,
        MaterialShapes.Arch,
        MaterialShapes.Fan,
        MaterialShapes.Arrow,
        MaterialShapes.SemiCircle,
        MaterialShapes.Oval,
        MaterialShapes.Pill,
        MaterialShapes.Triangle,
        MaterialShapes.Diamond,
        MaterialShapes.ClamShell,
        MaterialShapes.Pentagon,
        MaterialShapes.Gem,
        MaterialShapes.VerySunny,
        MaterialShapes.Sunny,
        MaterialShapes.Cookie4Sided,
        MaterialShapes.Cookie6Sided,
        MaterialShapes.Cookie7Sided,
        MaterialShapes.Cookie9Sided,
        MaterialShapes.Cookie12Sided,
        MaterialShapes.Ghostish,
        MaterialShapes.Clover4Leaf,
        MaterialShapes.Clover8Leaf,
        MaterialShapes.Burst,
        MaterialShapes.SoftBurst,
        MaterialShapes.Boom,
        MaterialShapes.SoftBoom,
        MaterialShapes.Flower,
        MaterialShapes.Puffy,
        MaterialShapes.PuffyDiamond,
        MaterialShapes.PixelCircle,
        MaterialShapes.PixelTriangle,
        MaterialShapes.Bun,
        MaterialShapes.Heart
    )
}