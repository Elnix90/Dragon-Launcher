package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.material3.MaterialShapes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.DragonJson

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
    @SerialName("Pebble")
    public object Pebble : IconShape()

    @Serializable
    @SerialName("Square")
    public object Square : IconShape()

    @Serializable
    @SerialName("RightSquare")
    public object RightSquare : IconShape()

    @Serializable
    @SerialName("Pentagon")
    public object Pentagon : IconShape()

    @Serializable
    @SerialName("Hexagon")
    public object Hexagon : IconShape()

    @Serializable
    @SerialName("Heptagon")
    public object Heptagon : IconShape()

    @Serializable
    @SerialName("Octagon")
    public object Octagon : IconShape()

    @Serializable
    @SerialName("Decagon")
    public object Decagon : IconShape()

    @Serializable
    @SerialName("RoundedSquare")
    public object RoundedSquare : IconShape()


    @Serializable
    @SerialName("Squircle")
    public object Squircle : IconShape()

    @Serializable
    @SerialName("Teardrop")
    public object Teardrop : IconShape()

    @Serializable
    @SerialName("Heart")
    public object Heart : IconShape()

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
    @SerialName("RoundedTriangle")
    public object RoundedTriangle : IconShape()
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
    @SerialName("Bun")
    public object Bun : IconShape()

    @Serializable
    @SerialName("Random")
    public object Random : IconShape()

//    @Serializable
//    @SerialName("Custom")
//    public data class Custom(
//        val numVertices: Int,
//        val radius: Float = 1f,
//        val centerX: Float = 0f,
//        val centerY: Float = 0f,
//        @Serializable(with = CornerRoundingSerializer::class)
//        val rounding: CornerRounding = CornerRounding.Unrounded,
//
//        @Serializable(with = CornerRoundingSerializer::class)
//        val perVertexRounding: List<CornerRounding>? = null
//    ) : IconShape()

    override fun toString(): String = this.javaClass.simpleName

    public companion object {

        /**
         * A selection of shapes for the pin that makes them pretty. not all of the [MaterialShapes] fit
         */
        public val pinMaterialShapes: Set<IconShape> = setOf(
            Circle,
            Slanted,
            Arch,
            Arrow,
            Oval,
            Pill,
            Triangle,
            Diamond,
            Pentagon,
            Gem,
            Cookie4Sided,
            Cookie7Sided,
            Cookie9Sided,
            Cookie12Sided
        )


        /**
         * The shapes that are allowed to be picked into the nest shape picker.
         * This limitation is due to the heavy math required to compute the shape boundary.
         * The Ideal shape is the circle as we can very easily compute the intersection using simple math, but as soon as this becomes a more complicated shape, the result starts to be approximated.
         */
        public val allowedNestShapes: Set<IconShape> by lazy {
            setOf(
                Circle,
                Triangle,
                Square,
                Pentagon,
                Hexagon,
                Cookie6Sided,
                Heptagon,
                Cookie7Sided,
                Hexagon,
                Octagon,
                Cookie9Sided,
                Decagon,
                Cookie12Sided
            )
        }


        public val allShapes: Set<IconShape> by lazy {
            setOf(
                PlatformDefault,
                Circle,
                Pebble,
                Square,
                RightSquare,
                Pentagon,
                Hexagon,
                Heptagon,
                Octagon,
                Decagon,
                RoundedSquare,
                Squircle,
                Teardrop,
                Slanted,
                Arch,
                Fan,
                Arrow,
                SemiCircle,
                Oval,
                Pill,
                RoundedTriangle,
                Triangle,
                Diamond,
                ClamShell,
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
                Bun,
                Heart,
                Random
            )
        }

        public val allShapesWithoutRandom: Set<IconShape> by lazy {
            allShapes.filterNotTo(mutableSetOf()) { it == Random }
        }

        public object IconShapeJson : DragonJson<IconShape>()
    }
}