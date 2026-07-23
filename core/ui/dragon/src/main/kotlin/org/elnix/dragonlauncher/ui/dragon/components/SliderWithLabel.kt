package org.elnix.dragonlauncher.ui.dragon.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.ktx.semiTransparentIfDisabled
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription
import java.text.NumberFormat
import java.util.Locale.getDefault
import kotlin.math.roundToInt

/**
 * Internal slider implementation shared by all SliderWithLabel overloads.
 *
 * This function operates purely on Float values, as required by Material Slider.
 * Public overloads are responsible for:
 * - Type conversion (Int ↔ Float)
 * - Step calculation
 * - Value formatting
 *
 * @param modifier Modifier applied to the root column
 * @param label Optional label displayed above the slider
 * @param value Current slider value as Float
 * @param valueRange Allowed slider range
 * @param steps Number of discrete steps (0 for continuous)
 * @param color Primary color for slider and text
 * @param valueText Pre-formatted value string to display
 * @param backgroundColor Color of the background of the slider
 * @param enabled Whether if the slider is interactable, slightly faded when disabled
 * @param resetEnabled Whether if the reset button is interactable
 * @param onReset Optional reset button callback
 * @param onDragStateChange Optional callback invoked with true on drag start and false on drag end
 * @param onChange Callback invoked when slider value changes
 */
@Composable
private fun SliderWithLabelInternal(
    modifier: Modifier,
    label: String,
    description: String? = null,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    color: Color,
    valueText: String,
    backgroundColor: Color,
    enabled: Boolean,
    resetEnabled: Boolean,
    allowTextEditValue: Boolean,
    onDragStateChange: ((Boolean) -> Unit)?,
    onReset: () -> Unit,
    onChange: (Float) -> Unit
) {
    val ctx = LocalContext.current
    val focusManager = LocalFocusManager.current
    val displayColor = color.semiTransparentIfDisabled(enabled)

    var editingText by remember { mutableStateOf(valueText) }
    var isError by remember { mutableStateOf(false) }


    // Edit the visual value whenever the real value changes to keep consistency
    LaunchedEffect(valueText) { editingText = valueText }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            TextWithDescription(
                text = label,
                description = description,
                modifier = Modifier.weight(1f)
            )

            val formatter = remember {
                NumberFormat.getInstance(getDefault())
            }

            EditValueTextField(
                value = editingText,
                modifier = Modifier,
                onValueChange = {
                    editingText = it
                    isError = false
                },
                enabled = allowTextEditValue,
                resetEnabled = resetEnabled,
                backgroundColor = backgroundColor,
                onReset = onReset,
                onDone = {
                    try {
                        val parsedNumber = formatter.parse(editingText.trim())?.toFloat() ?: throw NumberFormatException("Empty input")

                        val newValue = parsedNumber.coerceIn(valueRange)

                        onDragStateChange?.invoke(true)
                        onChange(newValue)
                        onDragStateChange?.invoke(false)
                    } catch (_: Exception) {
                        isError = true
                        ctx.showToast("Failed to parse number")
                        // Ignore malformed input - slider keeps its current value
                    }
                    focusManager.clearFocus()
                }
            )
        }

        Slider(
            value = value,
            enabled = enabled,
            onValueChange = {
                onChange(it)
                onDragStateChange?.invoke(true)
            },
            onValueChangeFinished = {
                onDragStateChange?.invoke(false)
            },
            valueRange = valueRange,
            steps = steps,
            colors = AppObjectsColors.sliderColors(displayColor, backgroundColor),
            modifier = Modifier.height(25.dp)
        )
    }
}

/**
 * SliderWithLabel overload for integer values.
 *
 * This slider allows selecting **every integer value in the given range**
 * without rounding issues. Internally, the slider uses Float values, but
 * step count and conversion ensure perfect integer snapping.
 *
 * @param modifier Modifier applied to the slider container
 * @param label Optional label displayed above the slider
 * @param value Current integer value
 * @param valueRange Allowed integer range (inclusive)
 * @param color Primary color for slider and text
 * @param backgroundColor Color of the background of the slider
 * @param enabled Whether if the slider is interactable
 * @param resetEnabled Whether if the reset button is interactable
 * @param onReset Optional reset button callback
 * @param onDragStateChange Optional callback for drag start/end
 * @param onChange Callback invoked when the value changes
 */
@Composable
public fun SliderWithLabel(
    modifier: Modifier = Modifier,
    label: String,
    description: String? = null,
    value: Int,
    valueRange: IntRange,
    enabled: Boolean = true,
    resetEnabled: Boolean,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    allowTextEditValue: Boolean = true,
    onDragStateChange: ((Boolean) -> Unit)? = null,
    onReset: () -> Unit,
    onChange: (Int) -> Unit
) {
    val floatRange = remember(valueRange) {
        valueRange.first.toFloat()..valueRange.last.toFloat()
    }

    val steps = remember(valueRange) {
        // Number of discrete selectable values minus endpoints
        (valueRange.last - valueRange.first - 1).coerceAtLeast(0)
    }

    SliderWithLabelInternal(
        modifier = modifier,
        label = label,
        description = description,
        value = value.toFloat(),
        valueRange = floatRange,
        steps = steps,
        color = color,
        valueText = value.toString(),
        backgroundColor = backgroundColor,
        enabled = enabled,
        resetEnabled = resetEnabled,
        allowTextEditValue = allowTextEditValue,
        onReset = onReset,
        onDragStateChange = onDragStateChange
    ) { floatValue ->
        onChange(floatValue.roundToInt())
    }
}

/**
 * SliderWithLabel overload for floating-point values.
 *
 * This slider operates in continuous mode unless a custom range implies
 * discrete behavior. The displayed value is formatted to the requested
 * number of decimal places.
 *
 * @param modifier Modifier applied to the slider container
 * @param label Optional label displayed above the slider
 * @param value Current float value
 * @param valueRange Allowed float range
 * @param color Primary color for slider and text
 * @param backgroundColor Color of the background of the slider
 * @param enabled Whether if the slider is interactable
 * @param resetEnabled Whether if the reset button is interactable
 * @param decimals Number of decimal places shown in the value text
 * @param onReset Optional reset button callback
 * @param onDragStateChange Optional callback for drag start/end
 * @param onChange Callback invoked when the value changes
 */
@Composable
public fun SliderWithLabel(
    modifier: Modifier = Modifier,
    label: String,
    description: String? = null,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    enabled: Boolean = true,
    resetEnabled: Boolean,
    decimals: Int = 2,
    allowTextEditValue: Boolean = true,
    onDragStateChange: ((Boolean) -> Unit)? = null,
    onReset: () -> Unit,
    onChange: (Float) -> Unit
) {
    val valueText = remember(value, decimals) {
        "%.${decimals}f".format(value)
    }

    SliderWithLabelInternal(
        modifier = modifier,
        label = label,
        description = description,
        value = value,
        valueRange = valueRange,
        steps = 0,
        color = color,
        valueText = valueText,
        backgroundColor = backgroundColor,
        enabled = enabled,
        resetEnabled = resetEnabled,
        allowTextEditValue = allowTextEditValue,
        onReset = onReset,
        onDragStateChange = onDragStateChange,
        onChange = onChange
    )
}

/**
 * SliderWithLabel overload for Dp values.
 *
 * @param modifier Modifier applied to the slider container
 * @param label Optional label displayed above the slider
 * @param value Current Dp value
 * @param valueRange Allowed Dp range
 * @param color Primary color for slider and text
 * @param backgroundColor Color of the background of the slider
 * @param enabled Whether if the slider is interactable
 * @param resetEnabled Whether if the reset button is interactable
 * @param decimals Number of decimal places shown in the value text
 * @param onReset Optional reset button callback
 * @param onDragStateChange Optional callback for drag start/end
 * @param onChange Callback invoked when the value changes
 */
@Composable
public fun SliderWithLabel(
    modifier: Modifier = Modifier,
    label: String,
    description: String? = null,
    value: Dp,
    valueRange: ClosedRange<Dp>,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    enabled: Boolean = true,
    resetEnabled: Boolean,
    decimals: Int = 2,
    allowTextEditValue: Boolean = true,
    onDragStateChange: ((Boolean) -> Unit)? = null,
    onReset: () -> Unit,
    onChange: (Dp) -> Unit
) {
    val valueText = remember(value, decimals) {
        "%.${decimals}f".format(value.value)
    }

    val floatValueRange = valueRange.start.value..valueRange.endInclusive.value

    SliderWithLabelInternal(
        modifier = modifier,
        label = label,
        description = description,
        value = value.value,
        valueRange = floatValueRange,
        steps = 0,
        color = color,
        valueText = valueText,
        backgroundColor = backgroundColor,
        enabled = enabled,
        allowTextEditValue = allowTextEditValue,
        onReset = onReset,
        resetEnabled = resetEnabled,
        onDragStateChange = onDragStateChange
    ) {
        onChange(it.dp)
    }
}