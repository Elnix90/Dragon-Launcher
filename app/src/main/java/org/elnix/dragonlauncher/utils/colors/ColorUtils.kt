package org.elnix.dragonlauncher.utils.colors

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun Color.blendWith(other: Color, ratio: Float): Color {
    return Color(
        red = red * (1 - ratio) + other.red * ratio,
        green = green * (1 - ratio) + other.green * ratio,
        blue = blue * (1 - ratio) + other.blue * ratio,
        alpha = alpha
    )
}

fun Color.adjustBrightness(
    factor: Float,
    affectAlpha: Boolean = false
): Color {
    return Color(
        red = (red * factor).coerceIn(0f, 1f),
        green = (green * factor).coerceIn(0f, 1f),
        blue = (blue * factor).coerceIn(0f, 1f),
        alpha = if (affectAlpha) (alpha * factor).coerceIn(0f, 1f) else alpha
    )
}


object AppObjectsColors {

    @Composable
    fun switchColors(): SwitchColors {
        val colors = MaterialTheme.colorScheme
        return SwitchDefaults.colors(
            checkedThumbColor = colors.onSurface,
            checkedTrackColor = colors.primary,
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = colors.onSurface,
            uncheckedTrackColor = colors.background,
            uncheckedBorderColor = Color.Transparent
        )
    }

    @Composable
    fun buttonColors(containerColor: Color? = null): ButtonColors {
        val colors = MaterialTheme.colorScheme
        return ButtonDefaults.buttonColors(
            containerColor = containerColor ?: colors.primary,
            contentColor = colors.onPrimary
        )
    }

    @Composable
    fun cancelButtonColors(): ButtonColors {
        return ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.error
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun datePickerColors(): DatePickerColors {
        val colors = MaterialTheme.colorScheme

        return DatePickerDefaults.colors(
            containerColor = colors.surface,

            titleContentColor = colors.primary.copy(alpha = 0.8f),
            headlineContentColor = colors.onSurface,

            weekdayContentColor = colors.onSurface,
            subheadContentColor = colors.onSurface,

            navigationContentColor = colors.primary.copy(alpha = 0.8f),

            yearContentColor = colors.onSurface,
            disabledYearContentColor = colors.onSurface.copy(alpha = 0.38f),
            currentYearContentColor = colors.secondary,
            selectedYearContentColor = colors.onSecondary,
            disabledSelectedYearContentColor = colors.onSurface.copy(alpha = 0.12f),

            selectedYearContainerColor = colors.secondary.copy(alpha = 0.5f),
            disabledSelectedYearContainerColor = colors.surface.copy(alpha = 0.7f),

            dayContentColor = colors.onSurface,
            disabledDayContentColor = colors.onSurface.copy(alpha = 0.38f),

            selectedDayContentColor = colors.onSecondary,
            disabledSelectedDayContentColor = colors.onSurface.copy(alpha = 0.12f),

            selectedDayContainerColor = colors.secondary.copy(alpha = 0.5f),
            disabledSelectedDayContainerColor = colors.surface.copy(alpha = 0.7f),

            todayContentColor = colors.primary.copy(alpha = 0.8f),
            todayDateBorderColor = colors.primary.copy(alpha = 0.8f),

            dayInSelectionRangeContentColor = colors.onSecondary,
            dayInSelectionRangeContainerColor = colors.secondary.copy(alpha = 0.3f),

            dividerColor = colors.outline,

            dateTextFieldColors = null
        )
    }



    @Composable
    fun sliderColors(
        activeTrackColor: Color? = null,
        backgroundColor: Color? = null
    ): SliderColors {
        val colors = MaterialTheme.colorScheme
        return SliderDefaults.colors(
            thumbColor = activeTrackColor?: colors.primary,
            activeTrackColor = activeTrackColor?: colors.secondary,
            activeTickColor = activeTrackColor?: colors.primary,
            inactiveTrackColor = backgroundColor ?: colors.surface,
            inactiveTickColor = activeTrackColor?: colors.primary,
            disabledThumbColor = colors.primary,
            disabledActiveTrackColor = backgroundColor ?: colors.onSurface,
            disabledActiveTickColor = colors.primary,
        )
    }


// --Commented out by Inspection START (11/8/25, 7:59 PM):
//    @Composable
//    fun dropDownMenuColors(): TextFieldColors {
//        val colors = MaterialTheme.colorScheme
//        return TextFieldDefaults.colors(
//            focusedContainerColor = colors.secondary,
//            unfocusedContainerColor = colors.secondary,
//            disabledContainerColor = colors.surface,
//            focusedIndicatorColor = colors.primary,
//            unfocusedIndicatorColor = colors.outline,
//            focusedTextColor = colors.onSurface,
//            unfocusedTextColor = colors.onSurface
//        )
//    }
// --Commented out by Inspection STOP (11/8/25, 7:59 PM)

    @Composable
    fun checkboxColors(): CheckboxColors {
        val colors = MaterialTheme.colorScheme
        return CheckboxDefaults.colors(
            checkedColor = colors.primary,
            uncheckedColor = colors.outline,
            checkmarkColor = colors.onPrimary,
            disabledCheckedColor = colors.primary.copy(alpha = 0.5f),
            disabledUncheckedColor = colors.outline.copy(alpha = 0.5f),
            disabledIndeterminateColor = colors.onSurface.copy(alpha = 0.5f),
        )
    }

    @Composable
    fun outlinedTextFieldColors(
        backgroundColor: Color? = null,
        onBackgroundColor: Color? = null,
        removeBorder: Boolean = false
    ): TextFieldColors {
        val colors = MaterialTheme.colorScheme
        return OutlinedTextFieldDefaults.colors(
            focusedTextColor = onBackgroundColor ?: colors.onBackground,
            unfocusedTextColor = onBackgroundColor ?: colors.onBackground,
            disabledTextColor = onBackgroundColor ?: colors.onBackground.adjustBrightness( 0.5f),
            errorTextColor = colors.error,

            focusedContainerColor = backgroundColor ?: colors.background,
            unfocusedContainerColor = backgroundColor ?: colors.background,
            disabledContainerColor = backgroundColor ?: colors.background,
            errorContainerColor = backgroundColor ?: colors.background,

            cursorColor = colors.primary,
            errorCursorColor = colors.error,

            focusedBorderColor = if (!removeBorder) colors.primary else Color.Transparent,
            unfocusedBorderColor = if (!removeBorder) colors.outline else Color.Transparent,
            disabledBorderColor = if (!removeBorder) colors.outline.copy(0.5f) else Color.Transparent,
            errorBorderColor = if (!removeBorder) colors.error else Color.Transparent,

            focusedLeadingIconColor = colors.primary,
            unfocusedLeadingIconColor = colors.onSurfaceVariant,
            disabledLeadingIconColor = colors.surfaceVariant,
            errorLeadingIconColor = colors.error,

            focusedTrailingIconColor = colors.primary,
            unfocusedTrailingIconColor = colors.onSurfaceVariant,
            disabledTrailingIconColor = colors.surfaceVariant,
            errorTrailingIconColor = colors.error,

            focusedLabelColor = colors.primary,
            unfocusedLabelColor = colors.outline,
            disabledLabelColor = colors.outline.copy(0.5f),
            errorLabelColor = colors.error,

            focusedPlaceholderColor = colors.outline.copy(0.8f),
            unfocusedPlaceholderColor = colors.outline.copy(0.5f),
            disabledPlaceholderColor = colors.outline.copy(0.3f),
            errorPlaceholderColor = colors.error,

            focusedSupportingTextColor = colors.onSurfaceVariant,
            unfocusedSupportingTextColor = colors.onSurfaceVariant,
            disabledSupportingTextColor = colors.surfaceVariant,
            errorSupportingTextColor = colors.error,

            focusedPrefixColor = colors.onSurfaceVariant,
            unfocusedPrefixColor = colors.onSurfaceVariant,
            disabledPrefixColor = colors.surfaceVariant,
            errorPrefixColor = colors.error,

            focusedSuffixColor = colors.onSurfaceVariant,
            unfocusedSuffixColor = colors.onSurfaceVariant,
            disabledSuffixColor = colors.surfaceVariant,
            errorSuffixColor = colors.error
        )
    }

    @Composable
    fun radioButtonColors(): RadioButtonColors {
        val colors = MaterialTheme.colorScheme
        return RadioButtonDefaults.colors(
            selectedColor = colors.primary,
            unselectedColor = colors.onSurface,
            disabledSelectedColor = colors.primary.copy(0.5f),
            disabledUnselectedColor = colors.onSurface.copy(0.5f)
        )
    }

    @Composable
    fun iconButtonColors(
        backgroundColor: Color? = null,
        contentColor: Color? = null
    ): IconButtonColors {
        val colors = MaterialTheme.colorScheme
        return IconButtonDefaults.iconButtonColors(
            containerColor = backgroundColor ?: colors.primary,
            contentColor = contentColor ?: colors.outline,
            disabledContainerColor = backgroundColor ?: colors.primary.copy(0.5f),
            disabledContentColor = contentColor ?: colors.outline.copy(0.5f)
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun timePickerColors(): TimePickerColors {
        val colors = MaterialTheme.colorScheme
        return TimePickerDefaults.colors(
            clockDialColor = colors.primary.copy(0.5f),
            clockDialSelectedContentColor = colors.onPrimary,
            clockDialUnselectedContentColor = colors.onSurface,
            selectorColor = colors.secondary,
            containerColor = colors.surface,
            periodSelectorBorderColor = Color.Transparent,
            periodSelectorSelectedContainerColor = colors.surface,
            periodSelectorUnselectedContainerColor = colors.surface,
            periodSelectorSelectedContentColor = colors.primary.copy(0.8f),
            periodSelectorUnselectedContentColor = colors.onSurface,
            timeSelectorSelectedContainerColor = colors.primary.copy(0.5f),
            timeSelectorUnselectedContainerColor = colors.surface.copy(0.7f),
            timeSelectorSelectedContentColor = colors.onPrimary,
            timeSelectorUnselectedContentColor = colors.onSurface,
        )
    }

    @Composable
    fun cardColors(): CardColors {
        val colors = MaterialTheme.colorScheme
        return CardDefaults.cardColors(
            colors.surface,
            colors.onSurface,
            colors.surface.copy(0.5f),
            colors.onSurface.copy(0.5f),
        )
    }
}
