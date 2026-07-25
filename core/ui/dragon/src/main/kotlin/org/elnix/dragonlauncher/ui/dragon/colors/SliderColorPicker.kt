package org.elnix.dragonlauncher.ui.dragon.colors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.alphaMultiplier
import org.elnix.dragonlauncher.ktx.to255
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel

@Composable
public fun SliderColorPicker(
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

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
        SliderWithLabel(
            label = stringResource(R.string.red),
            value = red,
            color = Color.Red,
            backgroundColor = Color.Red.alphaMultiplier(0.5f),
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
            backgroundColor = Color.Green.alphaMultiplier(0.5f),
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
            backgroundColor = Color.Blue.alphaMultiplier(0.5f),
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
