package org.elnix.dragonlauncher.common.serializables

import kotlinx.serialization.Serializable

@Serializable
sealed class IconShape {

    @Serializable
    object PlatformDefault : IconShape()

    @Serializable
    object Circle : IconShape()

    @Serializable
    object Square : IconShape()

    @Serializable
    object RoundedSquare : IconShape()

    @Serializable
    object Triangle : IconShape()

    @Serializable
    object Squircle : IconShape()

    @Serializable
    object Hexagon : IconShape()

    @Serializable
    object Pentagon : IconShape()

    @Serializable
    object Teardrop : IconShape()

    @Serializable
    object Pebble : IconShape()

    @Serializable
    object EasterEgg : IconShape()

    @Serializable
    object Random : IconShape()

    @Serializable
    object Dragon : IconShape()

    @Serializable
    object SharpTriangle : IconShape()

    @Serializable
    object RoundedTriangle : IconShape()

    @Serializable
    object Rhombus : IconShape()
    @Serializable
    data class Custom(
        val shape: CustomIconShapeSerializable
    ) : IconShape()
}


val allShapes = listOf(
    IconShape.PlatformDefault,
    IconShape.Circle,
    IconShape.Square,
    IconShape.RoundedSquare,
    IconShape.Triangle,
    IconShape.SharpTriangle,
    IconShape.RoundedTriangle,
    IconShape.Rhombus,
    IconShape.Squircle,
    IconShape.Hexagon,
    IconShape.Pentagon,
    IconShape.Teardrop,
    IconShape.Pebble,
    IconShape.EasterEgg,
    IconShape.Random,
    IconShape.Dragon
)
