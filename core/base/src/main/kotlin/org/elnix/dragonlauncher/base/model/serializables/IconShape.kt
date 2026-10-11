package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.DragonJson

@Immutable
@Serializable
@SerialName("IconShape")
public sealed class IconShape {
	@Serializable
	@SerialName("PlatformDefault")
	object PlatformDefault : IconShape()

	@Serializable
	@SerialName("Circle")
	object Circle : IconShape()

	@Serializable
	@SerialName("Pebble")
	object Pebble : IconShape()

	@Serializable
	@SerialName("Square")
	object Square : IconShape()

	@Serializable
	@SerialName("RightSquare")
	object RightSquare : IconShape()

	@Serializable
	@SerialName("Pentagon")
	object Pentagon : IconShape()

	@Serializable
	@SerialName("Hexagon")
	object Hexagon : IconShape()

	@Serializable
	@SerialName("Heptagon")
	object Heptagon : IconShape()

	@Serializable
	@SerialName("Octagon")
	object Octagon : IconShape()

	@Serializable
	@SerialName("Decagon")
	object Decagon : IconShape()

	@Serializable
	@SerialName("RoundedSquare")
	object RoundedSquare : IconShape()

	@Serializable
	@SerialName("Squircle")
	object Squircle : IconShape()

	@Serializable
	@SerialName("Teardrop")
	object Teardrop : IconShape()

	@Serializable
	@SerialName("Heart")
	object Heart : IconShape()

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
	@SerialName("RoundedTriangle")
	object RoundedTriangle : IconShape()

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
	@SerialName("Bun")
	object Bun : IconShape()

	@Serializable
	@SerialName("Random")
	object Random : IconShape()

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

	companion object {
		/**
		 * The shapes that are allowed to be picked into the nest shape picker.
		 * This limitation is due to the heavy math required to compute the shape boundary.
		 * The Ideal shape is the circle as we can very easily compute the intersection using simple math, but as soon as this becomes a more complicated shape, the result starts to be approximated.
		 */
		val allowedNestShapes: Set<IconShape> by lazy {
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

		val allShapes: Set<IconShape> by lazy {
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

		val allShapesWithoutRandom: Set<IconShape> by lazy {
			allShapes.filterNotTo(mutableSetOf()) { it == Random }
		}

		object IconShapeJson : DragonJson<IconShape>()
	}
}
