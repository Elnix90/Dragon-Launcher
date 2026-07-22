package org.elnix.dragonlauncher.ui.dragon.colors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.alphaMultiplier
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel

@Composable
public fun SliderColorPicker(
    actualColor: Color,
    initialColor: Color,
    onColorSelected: (Color) -> Unit
) {
    var red by remember(actualColor) { mutableFloatStateOf(actualColor.red) }
    var green by remember(actualColor) { mutableFloatStateOf(actualColor.green) }
    var blue by remember(actualColor) { mutableFloatStateOf(actualColor.blue) }
    var alpha by remember(actualColor) { mutableFloatStateOf(actualColor.alpha) }

    val color = Color(red, green, blue, alpha)

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
            valueRange = 0f..1f,
            resetEnabled = red != initialColor.red,
            onReset = {
                red = initialColor.red
                onColorSelected(color.copy(red = initialColor.red))
            }
        ) {
            red = it
            onColorSelected(color)
        }
        SliderWithLabel(
            label = stringResource(R.string.green),
            value = green,
            color = Color.Green,
            backgroundColor = Color.Green.alphaMultiplier(0.5f),
            valueRange = 0f..1f,
            resetEnabled = green != initialColor.green,
            onReset = {
                green = initialColor.green
                onColorSelected(color.copy(green = initialColor.green))
            }
        ) {
            green = it
            onColorSelected(color)
        }
        SliderWithLabel(
            label = stringResource(R.string.blue),
            value = blue,
            color = Color.Blue,
            backgroundColor = Color.Blue.alphaMultiplier(0.5f),
            valueRange = 0f..1f,
            resetEnabled = blue != initialColor.blue,
            onReset = {
                blue = initialColor.blue
                onColorSelected(color.copy(blue = initialColor.blue))
            }
        ) {
            blue = it
            onColorSelected(color)
        }
    }
}
