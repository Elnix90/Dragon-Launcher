package org.elnix.dragonlauncher.ui.dragon.colors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.alphaMultiplier
import org.elnix.dragonlauncher.ktx.to255
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel

private const val multiplier = 0.2f

@Composable
fun SliderColorPicker(
    actualColor: Color,
    initialColor: Color,
    onColorSelected: (Color) -> Unit
) {
    var red by remember(actualColor) { mutableIntStateOf(actualColor.red.to255) }
    var green by remember(actualColor) { mutableIntStateOf(actualColor.green.to255) }
    var blue by remember(actualColor) { mutableIntStateOf(actualColor.blue.to255) }
    val alpha = remember(actualColor) { actualColor.alpha.to255 }

    val initialColorRed = remember { initialColor.red.to255 }
    val initialColorGreen = remember { initialColor.green.to255 }
    val initialColorBlue = remember { initialColor.blue.to255 }

    fun select() {
        onColorSelected(Color(red, green, blue, alpha))
    }

    DragonSettingsGroup {
        SliderWithLabel(
            label = stringResource(R.string.red),
            value = red,
            color = Color.Red,
            backgroundColor = Color.Red.alphaMultiplier(multiplier),
            valueRange = 0..255,
            resetEnabled = red != initialColorRed,
            onReset = {
                red = initialColorRed
                select()
            }
        ) {
            red = it
            select()
        }

        SliderWithLabel(
            label = stringResource(R.string.green),
            value = green,
            color = Color.Green,
            backgroundColor = Color.Green.alphaMultiplier(multiplier),
            valueRange = 0..255,
            resetEnabled = green != initialColorGreen,
            onReset = {
                green = initialColorGreen
                select()
            }
        ) {
            green = it
            select()
        }

        SliderWithLabel(
            label = stringResource(R.string.blue),
            value = blue,
            color = Color.Blue,
            backgroundColor = Color.Blue.alphaMultiplier(multiplier),
            valueRange = 0..255,
            resetEnabled = blue != initialColorBlue,
            onReset = {
                blue = initialColorBlue
                select()
            }
        ) {
            blue = it
            select()
        }
    }
}
