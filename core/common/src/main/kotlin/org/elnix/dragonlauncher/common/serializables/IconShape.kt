package org.elnix.dragonlauncher.common.serializables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    @SerialName("Triangle")
    object Triangle : IconShape()

    @Serializable
    @SerialName("Squircle")
    object Squircle : IconShape()

    @Serializable
    @SerialName("Hexagon")
    object Hexagon : IconShape()

    @Serializable
    @SerialName("Pentagon")
    object Pentagon : IconShape()

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
    @SerialName("Random")
    object Random : IconShape()

    @Serializable
    @SerialName("Custom")
    data class Custom(
        val shape: CustomIconShape
    ) : IconShape()

    companion object {
        val allShapes = listOf(
            PlatformDefault,
            Circle,
            Square,
            RoundedSquare,
            Triangle,
            Squircle,
            Hexagon,
            Pentagon,
            Teardrop,
            Pebble,
            EasterEgg,
            Random,
        )


        val allShapesWithoutRandom = listOf(
            PlatformDefault,
            Circle,
            Square,
            RoundedSquare,
            Triangle,
            Squircle,
            Hexagon,
            Pentagon,
            Teardrop,
            Pebble,
            EasterEgg,
        )

        object IconShapeJson: DragonJson<IconShape>()
    }
}