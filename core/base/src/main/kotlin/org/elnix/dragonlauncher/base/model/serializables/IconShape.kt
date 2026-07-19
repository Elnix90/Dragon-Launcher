package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.material3.MaterialShapes
import androidx.graphics.shapes.CornerRounding
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.model.serializables.serializers.CornerRoundingSerializer
import kotlin.reflect.KClass

@Serializable
@SerialName("IconShape")
public sealed class IconShape {

    @Serializable
    public object PlatformDefault : IconShape()

    @Serializable
    public object Circle : IconShape()

    @Serializable
    @SerialName("Pebble")
    public object Pebble : IconShape()

    @Serializable
    public object Square : IconShape()

    @Serializable
    public object Pentagon : IconShape()

    @Serializable
    public object Hexagon : IconShape()

    @Serializable
    public object Heptagon : IconShape()

    @Serializable
    public object Octagon : IconShape()

    @Serializable
    public object Decagon : IconShape()

    @Serializable
    public object RoundedSquare : IconShape()


    @Serializable
    public object Squircle : IconShape()

    @Serializable
    public object Teardrop : IconShape()

    @Serializable
    public object Heart : IconShape()

    @Serializable
    public object Slanted : IconShape()

    @Serializable
    public object Arch : IconShape()

    @Serializable
    public object Fan : IconShape()

    @Serializable
    public object Arrow : IconShape()

    @Serializable
    public object SemiCircle : IconShape()

    @Serializable
    public object Oval : IconShape()

    @Serializable
    public object Pill : IconShape()

    @Serializable
    public object RoundedTriangle : IconShape()
    @Serializable
    public object Triangle : IconShape()

    @Serializable
    public object Diamond : IconShape()

    @Serializable
    public object ClamShell : IconShape()

    @Serializable
    public object Gem : IconShape()

    @Serializable
    public object VerySunny : IconShape()

    @Serializable
    public object Sunny : IconShape()

    @Serializable
    public object Cookie4Sided : IconShape()

    @Serializable
    public object Cookie6Sided : IconShape()

    @Serializable
    public object Cookie7Sided : IconShape()

    @Serializable
    public object Cookie9Sided : IconShape()

    @Serializable
    public object Cookie12Sided : IconShape()

    @Serializable
    public object Ghostish : IconShape()

    @Serializable
    public object Clover4Leaf : IconShape()

    @Serializable
    public object Clover8Leaf : IconShape()

    @Serializable
    public object Burst : IconShape()

    @Serializable
    public object SoftBurst : IconShape()

    @Serializable
    public object Boom : IconShape()

    @Serializable
    public object SoftBoom : IconShape()

    @Serializable
    public object Flower : IconShape()

    @Serializable
    public object Puffy : IconShape()

    @Serializable
    public object PuffyDiamond : IconShape()

    @Serializable
    public object PixelCircle : IconShape()

    @Serializable
    public object Bun : IconShape()

    @Serializable
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
        public val allowedNestShapes: Set<KClass< out IconShape>> = setOf(
            Circle::class,
            Triangle::class,
            RoundedTriangle::class,
            Square::class,
            RoundedSquare::class,
            Pentagon::class,
            Hexagon::class,
            Cookie6Sided::class,
            Heptagon::class,
            Cookie7Sided::class,
            Hexagon::class,
            Octagon::class,
            Cookie9Sided::class,
            Decagon::class,
            Cookie12Sided::class
        )


        public val allShapes: Set<IconShape> by lazy {
            setOf(
                PlatformDefault,
                Circle,
                Pebble,
                Square,
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