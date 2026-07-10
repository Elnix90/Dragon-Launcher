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
import org.elnix.dragonlauncher.base.util.ColorUtils.alphaMultiplier
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel

@Composable
public fun SliderColorPicker(
    actualColor: Color,
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
            valueRange = 0f..1f
        ) {
            red = it
            onColorSelected(color)
        }
        SliderWithLabel(
            label = stringResource(R.string.green),
            value = green,
            color = Color.Green,
            backgroundColor = Color.Green.alphaMultiplier(0.5f),
            valueRange = 0f..1f
        ) {
            green = it
            onColorSelected(color)
        }
        SliderWithLabel(
            label = stringResource(R.string.blue),
            value = blue,
            color = Color.Blue,
            backgroundColor = Color.Blue.alphaMultiplier(0.5f),
            valueRange = 0f..1f
        ) {
            blue = it
            onColorSelected(color)
        }
    }
}
