package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.model.serializables.serializers.CornerRoundingSerializer

@Serializable
@SerialName("IconShape")
public sealed class IconShape {

    @Serializable
    @SerialName("PlatformDefault")
    public object PlatformDefault : IconShape()

    @Serializable
    @SerialName("Circle")
    public object Circle : IconShape()

    @Serializable
    @SerialName("Square")
    public object Square : IconShape()

    @Serializable
    @SerialName("RoundedSquare")
    public object RoundedSquare : IconShape()


    @Serializable
    @SerialName("Squircle")
    public object Squircle : IconShape()

    @Serializable
    @SerialName("Hexagon")
    public object Hexagon : IconShape()


    @Serializable
    @SerialName("Teardrop")
    public object Teardrop : IconShape()

    @Serializable
    @SerialName("Pebble")
    public object Pebble : IconShape()

    @Serializable
    @SerialName("EasterEgg")
    public object EasterEgg : IconShape()

    @Serializable
    @SerialName("Slanted")
    public object Slanted : IconShape()

    @Serializable
    @SerialName("Arch")
    public object Arch : IconShape()

    @Serializable
    @SerialName("Fan")
    public object Fan : IconShape()

    @Serializable
    @SerialName("Arrow")
    public object Arrow : IconShape()

    @Serializable
    @SerialName("SemiCircle")
    public object SemiCircle : IconShape()

    @Serializable
    @SerialName("Oval")
    public object Oval : IconShape()

    @Serializable
    @SerialName("Pill")
    public object Pill : IconShape()

    @Serializable
    @SerialName("Triangle")
    public object Triangle : IconShape()

    @Serializable
    @SerialName("Diamond")
    public object Diamond : IconShape()

    @Serializable
    @SerialName("ClamShell")
    public object ClamShell : IconShape()

    @Serializable
    @SerialName("Pentagon")
    public object Pentagon : IconShape()

    @Serializable
    @SerialName("Gem")
    public object Gem : IconShape()

    @Serializable
    @SerialName("VerySunny")
    public object VerySunny : IconShape()

    @Serializable
    @SerialName("Sunny")
    public object Sunny : IconShape()

    @Serializable
    @SerialName("Cookie4Sided")
    public object Cookie4Sided : IconShape()

    @Serializable
    @SerialName("Cookie6Sided")
    public object Cookie6Sided : IconShape()

    @Serializable
    @SerialName("Cookie7Sided")
    public object Cookie7Sided : IconShape()

    @Serializable
    @SerialName("Cookie9Sided")
    public object Cookie9Sided : IconShape()

    @Serializable
    @SerialName("Cookie12Sided")
    public object Cookie12Sided : IconShape()

    @Serializable
    @SerialName("Ghostish")
    public object Ghostish : IconShape()

    @Serializable
    @SerialName("Clover4Leaf")
    public object Clover4Leaf : IconShape()

    @Serializable
    @SerialName("Clover8Leaf")
    public object Clover8Leaf : IconShape()

    @Serializable
    @SerialName("Burst")
    public object Burst : IconShape()

    @Serializable
    @SerialName("SoftBurst")
    public object SoftBurst : IconShape()

    @Serializable
    @SerialName("Boom")
    public object Boom : IconShape()

    @Serializable
    @SerialName("SoftBoom")
    public object SoftBoom : IconShape()

    @Serializable
    @SerialName("Flower")
    public object Flower : IconShape()

    @Serializable
    @SerialName("Puffy")
    public object Puffy : IconShape()

    @Serializable
    @SerialName("PuffyDiamond")
    public object PuffyDiamond : IconShape()

    @Serializable
    @SerialName("PixelCircle")
    public object PixelCircle : IconShape()

    @Serializable
    @SerialName("PixelTriangle")
    public object PixelTriangle : IconShape()

    @Serializable
    @SerialName("Bun")
    public object Bun : IconShape()

    @Serializable
    @SerialName("Heart")
    public object Heart : IconShape()
    @Serializable
    @SerialName("Random")
    public object Random : IconShape()

    @Serializable
    @SerialName("Custom")
    public data class Custom(
        val numVertices: Int,
        val radius: Float = 1f,
        val centerX: Float = 0f,
        val centerY: Float = 0f,
        @Serializable(with = CornerRoundingSerializer::class)
        val rounding: CornerRounding = CornerRounding.Unrounded,

        @Serializable(with = CornerRoundingSerializer::class)
        val perVertexRounding: List<CornerRounding>? = null
        ) : IconShape()

    public companion object {

        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        public val allMaterialShapes: List<RoundedPolygon> = listOf(
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

        public val allShapes: List<IconShape> = listOf(
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

        public val allShapesWithoutRandom: List<IconShape> = allShapes.filterNot { it == Random }

        public object IconShapeJson: DragonJson<IconShape>()
    }
}