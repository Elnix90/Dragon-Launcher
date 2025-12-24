package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.utils.colors.AppObjectsColors
import kotlin.math.roundToInt

/**
 * Basic float slider for simple value adjustment (0f..1f range).
 *
 * @param label Optional label displayed above the slider
 * @param showValue Whether to display current value next to label
 * @param value Current slider value (0f..1f)
 * @param color Primary color for slider and text
 * @param onChange Callback invoked when slider value changes
 */
@Composable
fun SliderWithLabel(
    label: String? =null,
    showValue: Boolean = true,
    value: Float,
    color: Color,
    onChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (label != null) {
                Text(
                    text = label,
                    color = color
                )
            }

            if (showValue) {
                Text(
                    text = (value * 255).toInt().toString(),
                    color = color
                )
            }
        }

        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = 0f..1f,
            steps = 254,
            colors = AppObjectsColors.sliderColors(color)
        )
    }
}

/**
 * Percentage-based float slider (0%..100%) with drag state tracking.
 *
 * @param label Label displayed above the slider
 * @param value Current slider value (0f..1f, displays as 0%..100%)
 * @param color Primary color for slider and text
 * @param showValue Whether to display percentage value next to label
 * @param onChange Callback invoked during drag (new value 0f..1f)
 * @param onDragStateChange Callback for drag start (true) / end (false)
 */
@Composable
fun SliderWithLabel(
    modifier: Modifier = Modifier,
    label: String,
    value: Float,
    color: Color,
    showValue: Boolean = true,
    onChange: (Float) -> Unit,
    onDragStateChange: (Boolean) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = color)
            if (showValue) Text(text = "${(value * 100).toInt()}%", color = color)
        }

        Slider(
            value = value,
            onValueChange = { newValue ->
                onChange(newValue)
                onDragStateChange(true)
            },
            onValueChangeFinished = { onDragStateChange(false) },
            valueRange = 0f..1f,
            steps = 99,
            colors = AppObjectsColors.sliderColors(color)
        )
    }
}


/**
 * Integer slider for discrete values without drag state tracking.
 *
 * @param label Optional label displayed above the slider
 * @param value Current slider value (integer)
 * @param valueRange Range of allowed values (converted to float for slider)
 * @param color Primary color for slider and text
 * @param showValue Whether to display current value next to label
 * @param onReset Optional reset button callback
 * @param onChange Callback invoked when slider value changes (integer)
 */
@Composable
fun SliderWithLabel(
    label: String? = null,
    value: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    color: Color,
    showValue: Boolean,
    onReset: (() -> Unit)? = null,
    onChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (label != null) {
                Text(
                    text = label,
                    color = color
                )
            }

            if (showValue) {
                Text(
                    text = value.toString(),
                    color = color
                )
            }

            if (onReset != null) {
                IconButton(
                    onClick = onReset
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Reset",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Slider(
            value = value.toFloat(),
            onValueChange = { floatValue ->
                onChange(floatValue.roundToInt())
            },
            valueRange = valueRange,
            steps = 100,
            colors = AppObjectsColors.sliderColors(color)
        )
    }
}

/**
 * Integer slider with drag state tracking and optional reset.
 *
 * @param label Optional label displayed above the slider
 * @param value Current slider value (integer)
 * @param valueRange Range of allowed values (converted to float for slider)
 * @param color Primary color for slider and text
 * @param showValue Whether to display current value next to label
 * @param onReset Optional reset button callback
 * @param onChange Callback invoked during drag (integer value)
 * @param onDragStateChange Optional callback for drag start/end states
 */
@Composable
fun SliderWithLabel(
    label: String? = null,
    value: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    color: Color,
    showValue: Boolean,
    onReset: (() -> Unit)? = null,
    onChange: (Int) -> Unit,
    onDragStateChange: ((Boolean) -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (label != null) {
                Text(
                    text = label,
                    color = color
                )
            }

            if (showValue) {
                Text(
                    text = value.toString(),
                    color = color
                )
            }

            if (onReset != null) {
                IconButton(
                    onClick = onReset
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Reset",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Slider(
            value = value.toFloat(),
            onValueChange = { floatValue ->
                onChange(floatValue.roundToInt())
                onDragStateChange?.invoke(true)
            },
            onValueChangeFinished = {
                onDragStateChange?.invoke(false)
            },
            valueRange = valueRange,
            steps = 100,
            colors = AppObjectsColors.sliderColors(color)
        )
    }
}
