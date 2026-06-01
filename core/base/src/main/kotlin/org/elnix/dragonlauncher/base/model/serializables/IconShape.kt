package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.graphics.shapes.CornerRounding
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.model.serializables.serializers.CornerRoundingSerializer

@Serializable
@SerialName("IconShape")
sealed class IconShape {

    @Serializable
    @SerialName("PlatformDefault")
    object PlatformDefault : IconShape()

    @Serializable
    @SerialName("Circle")
    object Circle : IconShape()

    @Serializable
    @SerialName("Square")
    object Square : IconShape()

    @Serializable
    @SerialName("RoundedSquare")
    object RoundedSquare : IconShape()


    @Serializable
    @SerialName("Squircle")
    object Squircle : IconShape()

    @Serializable
    @SerialName("Hexagon")
    object Hexagon : IconShape()


    @Serializable
    @SerialName("Teardrop")
    object Teardrop : IconShape()

    @Serializable
    @SerialName("Pebble")
    object Pebble : IconShape()

    @Serializable
    @SerialName("EasterEgg")
    object EasterEgg : IconShape()

    @Serializable
    @SerialName("Slanted")
    object Slanted : IconShape()

    @Serializable
    @SerialName("Arch")
    object Arch : IconShape()

    @Serializable
    @SerialName("Fan")
    object Fan : IconShape()

    @Serializable
    @SerialName("Arrow")
    object Arrow : IconShape()

    @Serializable
    @SerialName("SemiCircle")
    object SemiCircle : IconShape()

    @Serializable
    @SerialName("Oval")
    object Oval : IconShape()

    @Serializable
    @SerialName("Pill")
    object Pill : IconShape()

    @Serializable
    @SerialName("Triangle")
    object Triangle : IconShape()

    @Serializable
    @SerialName("Diamond")
    object Diamond : IconShape()

    @Serializable
    @SerialName("ClamShell")
    object ClamShell : IconShape()

    @Serializable
    @SerialName("Pentagon")
    object Pentagon : IconShape()

    @Serializable
    @SerialName("Gem")
    object Gem : IconShape()

    @Serializable
    @SerialName("VerySunny")
    object VerySunny : IconShape()

    @Serializable
    @SerialName("Sunny")
    object Sunny : IconShape()

    @Serializable
    @SerialName("Cookie4Sided")
    object Cookie4Sided : IconShape()

    @Serializable
    @SerialName("Cookie6Sided")
    object Cookie6Sided : IconShape()

    @Serializable
    @SerialName("Cookie7Sided")
    object Cookie7Sided : IconShape()

    @Serializable
    @SerialName("Cookie9Sided")
    object Cookie9Sided : IconShape()

    @Serializable
    @SerialName("Cookie12Sided")
    object Cookie12Sided : IconShape()

    @Serializable
    @SerialName("Ghostish")
    object Ghostish : IconShape()

    @Serializable
    @SerialName("Clover4Leaf")
    object Clover4Leaf : IconShape()

    @Serializable
    @SerialName("Clover8Leaf")
    object Clover8Leaf : IconShape()

    @Serializable
    @SerialName("Burst")
    object Burst : IconShape()

    @Serializable
    @SerialName("SoftBurst")
    object SoftBurst : IconShape()

    @Serializable
    @SerialName("Boom")
    object Boom : IconShape()

    @Serializable
    @SerialName("SoftBoom")
    object SoftBoom : IconShape()

    @Serializable
    @SerialName("Flower")
    object Flower : IconShape()

    @Serializable
    @SerialName("Puffy")
    object Puffy : IconShape()

    @Serializable
    @SerialName("PuffyDiamond")
    object PuffyDiamond : IconShape()

    @Serializable
    @SerialName("PixelCircle")
    object PixelCircle : IconShape()

    @Serializable
    @SerialName("PixelTriangle")
    object PixelTriangle : IconShape()

    @Serializable
    @SerialName("Bun")
    object Bun : IconShape()

    @Serializable
    @SerialName("Heart")
    object Heart : IconShape()
    @Serializable
    @SerialName("Random")
    object Random : IconShape()

    @Serializable
    @SerialName("Custom")
    data class Custom(
        val numVertices: Int,
        val radius: Float = 1f,
        val centerX: Float = 0f,
        val centerY: Float = 0f,
        @Serializable(with = CornerRoundingSerializer::class)
        val rounding: CornerRounding = CornerRounding.Unrounded,

        @Serializable(with = CornerRoundingSerializer::class)
        val perVertexRounding: List<CornerRounding>? = null
        ) : IconShape()

    companion object {

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

        val allShapes = listOf(
            PlatformDefault,
            Circle,
            Square,
            Slanted,
            Arch,
            Fan,
            Arrow,
            SemiCircle,
            Oval,
            Pill,
            Triangle,
            Diamond,
            ClamShell,
            Pentagon,
            Gem,
            VerySunny,
            Sunny,
            Cookie4Sided,
            Cookie6Sided,
            Cookie7Sided,
            Cookie9Sided,
            Cookie12Sided,
            Ghostish,
            Clover4Leaf,
            Clover8Leaf,
            Burst,
            SoftBurst,
            Boom,
            SoftBoom,
            Flower,
            Puffy,
            PuffyDiamond,
            PixelCircle,
            PixelTriangle,
            Bun,
            Heart,
            Random
        )

        val allShapesWithoutRandom = allShapes.filterNot { it == Random }

        object IconShapeJson: DragonJson<IconShape>()
    }
}