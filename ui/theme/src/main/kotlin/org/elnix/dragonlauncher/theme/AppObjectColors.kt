package org.elnix.dragonlauncher.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.ToggleButtonColors
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.elnix.dragonlauncher.ktx.alphaMultiplier
import org.elnix.dragonlauncher.ui.composition.LocalUseCustomColorChannels

object AppObjectsColors {

    @Composable
    fun switchColors(): SwitchColors {
        return if (LocalUseCustomColorChannels.current) {
            with(MaterialTheme.colorScheme) {
                SwitchDefaults.colors(
                    checkedThumbColor = outline,
                    checkedTrackColor = primary,
                    checkedBorderColor = Color.Transparent,
                    uncheckedThumbColor = outline.alphaMultiplier(0.7f),
                    uncheckedTrackColor = background,
                    uncheckedBorderColor = Color.Transparent,
                    disabledCheckedThumbColor = outline.alphaMultiplier(0.5f),
                    disabledCheckedTrackColor = primary.alphaMultiplier(0.5f),
                    disabledCheckedBorderColor = Color.Transparent,
                    disabledUncheckedThumbColor = onSurface.alphaMultiplier(0.5f),
                    disabledUncheckedTrackColor = background,
                    disabledUncheckedBorderColor = Color.Transparent,
                )
            }
        } else SwitchDefaults.colors()
    }

    @Composable
    fun buttonColors(): ButtonColors {
        return if (LocalUseCustomColorChannels.current) {
            with(MaterialTheme.colorScheme) {
                ButtonDefaults.buttonColors(
                    containerColor = primary,
                    contentColor = onPrimary,
                    disabledContainerColor = primary.alphaMultiplier(0.5f),
                    disabledContentColor = onPrimary.alphaMultiplier(0.5f)
                )
            }
        } else ButtonDefaults.buttonColors()
    }

    @Composable
    fun cancelButtonColors(): ButtonColors {
        return if (LocalUseCustomColorChannels.current) {
            with(MaterialTheme.colorScheme) {
                ButtonDefaults.buttonColors(
                    containerColor = errorContainer,
                    contentColor = onErrorContainer,
                    disabledContainerColor = errorContainer.alphaMultiplier(0.5f),
                    disabledContentColor = onErrorContainer.alphaMultiplier(0.5f)
                )
            }
        } else ButtonDefaults.outlinedButtonColors()
    }


    @Composable
    fun sliderColors(
        activeTrackColor: Color,
    ): SliderColors {
        return if (LocalUseCustomColorChannels.current) {
            with(MaterialTheme.colorScheme) {
                SliderDefaults.colors(
                    thumbColor = activeTrackColor,
                    activeTrackColor = activeTrackColor,
                    inactiveTrackColor = surfaceVariant,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = activeTrackColor,

                    disabledThumbColor = activeTrackColor.alphaMultiplier(0.8f),
                    disabledActiveTrackColor = activeTrackColor.alphaMultiplier(0.5f),
                    disabledInactiveTrackColor = surfaceVariant.alphaMultiplier(0.8f),
                    disabledActiveTickColor = Color.Transparent,
                    disabledInactiveTickColor = activeTrackColor.alphaMultiplier(0.8f),
                )
            }
        } else SliderDefaults.colors()
    }

    @Composable
    fun checkboxColors(): CheckboxColors {
        return if (LocalUseCustomColorChannels.current) {
            with(MaterialTheme.colorScheme) {
                CheckboxDefaults.colors(
                    checkedColor = primary,
                    uncheckedColor = outline,
                    checkmarkColor = onPrimary,
                    disabledCheckedColor = primary.alphaMultiplier(0.5f),
                    disabledUncheckedColor = outline.alphaMultiplier(0.5f),
                    disabledIndeterminateColor = onSurface.alphaMultiplier(0.5f),
                )
            }
        } else CheckboxDefaults.colors()
    }

    @Composable
    fun outlinedTextFieldColors(
        backgroundColor: Color? = null,
        onBackgroundColor: Color? = null,
        removeBorder: Boolean = true
    ): TextFieldColors {
        return if (LocalUseCustomColorChannels.current) {
            with(MaterialTheme.colorScheme) {
                OutlinedTextFieldDefaults.colors(
                    focusedTextColor = onBackgroundColor ?: onBackground,
                    unfocusedTextColor = onBackgroundColor ?: onBackground,
                    disabledTextColor = (onBackgroundColor ?: onBackground).alphaMultiplier(0.5f),
                    errorTextColor = error,

                    focusedContainerColor = backgroundColor ?: background,
                    unfocusedContainerColor = backgroundColor ?: background,
                    disabledContainerColor = (backgroundColor ?: background).alphaMultiplier(0.5f),
                    errorContainerColor = backgroundColor ?: background,

                    cursorColor = primary,
                    errorCursorColor = error,

                    focusedBorderColor = if (!removeBorder) primary else Color.Transparent,
                    unfocusedBorderColor = if (!removeBorder) outline else Color.Transparent,
                    disabledBorderColor = if (!removeBorder) outline.alphaMultiplier(0.5f) else Color.Transparent,
                    errorBorderColor = error,

                    focusedLeadingIconColor = primary,
                    unfocusedLeadingIconColor = onSurfaceVariant,
                    disabledLeadingIconColor = surfaceVariant,
                    errorLeadingIconColor = error,

                    focusedTrailingIconColor = primary,
                    unfocusedTrailingIconColor = onSurfaceVariant,
                    disabledTrailingIconColor = surfaceVariant,
                    errorTrailingIconColor = error,

                    focusedLabelColor = primary,
                    unfocusedLabelColor = outline,
                    disabledLabelColor = outline.alphaMultiplier(0.5f),
                    errorLabelColor = error,

                    focusedPlaceholderColor = outline.alphaMultiplier(0.8f),
                    unfocusedPlaceholderColor = outline.alphaMultiplier(0.5f),
                    disabledPlaceholderColor = outline.alphaMultiplier(0.3f),
                    errorPlaceholderColor = error,

                    focusedSupportingTextColor = onSurfaceVariant,
                    unfocusedSupportingTextColor = onSurfaceVariant,
                    disabledSupportingTextColor = surfaceVariant,
                    errorSupportingTextColor = error,

                    focusedPrefixColor = onSurfaceVariant,
                    unfocusedPrefixColor = onSurfaceVariant,
                    disabledPrefixColor = surfaceVariant,
                    errorPrefixColor = error,

                    focusedSuffixColor = onSurfaceVariant,
                    unfocusedSuffixColor = onSurfaceVariant,
                    disabledSuffixColor = surfaceVariant,
                    errorSuffixColor = error
                )
            }
        } else OutlinedTextFieldDefaults.colors()
    }

    @Composable
    fun radioButtonColors(): RadioButtonColors {
        return if (LocalUseCustomColorChannels.current) {
            with(MaterialTheme.colorScheme) {
                RadioButtonDefaults.colors(
                    selectedColor = primary,
                    unselectedColor = onSurface,
                    disabledSelectedColor = primary.alphaMultiplier(0.5f),
                    disabledUnselectedColor = onSurface.alphaMultiplier(0.5f)
                )
            }
        } else RadioButtonDefaults.colors()
    }

    @Composable
    fun iconToggleButtonColors(): IconToggleButtonColors {
        return if (LocalUseCustomColorChannels.current) {
            with(MaterialTheme.colorScheme) {
                IconButtonDefaults.iconToggleButtonColors(
                    containerColor = surface,
                    contentColor = onSurface,
                    disabledContainerColor = surfaceVariant,
                    disabledContentColor = onSurfaceVariant,
                    checkedContainerColor = primary,
                    checkedContentColor = onPrimary
                )
            }
        } else IconButtonDefaults.iconToggleButtonColors()
    }

    @Composable
    fun iconButtonColors(): IconButtonColors {
        return if (LocalUseCustomColorChannels.current) {
            with(MaterialTheme.colorScheme) {
                IconButtonDefaults.iconButtonColors(
                    containerColor = surface,
                    contentColor = primary,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = primary.alphaMultiplier(0.5f)
                )
            }
        } else IconButtonDefaults.iconButtonColors()
    }


    @Composable
    fun cancelIconButtonColors(): IconButtonColors {
        return if (LocalUseCustomColorChannels.current) {
            with(MaterialTheme.colorScheme) {
                IconButtonDefaults.iconButtonColors(
                    containerColor = surface,
                    contentColor = error,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = error.alphaMultiplier(0.5f)
                )
            }
        } else IconButtonDefaults.iconButtonColors()
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Deprecated("Do not use this its only there as a placeholder")
    @Composable
    fun toggleButtonColors(): ToggleButtonColors {
        return if (LocalUseCustomColorChannels.current) {
            with(MaterialTheme.colorScheme) {
                ToggleButtonDefaults.toggleButtonColors(
                    containerColor = primary,
                    contentColor = onPrimary,
                    disabledContainerColor = surfaceVariant,
                    disabledContentColor = onSurfaceVariant,
                    checkedContainerColor = primary,
                    checkedContentColor = onPrimary
                )
            }
        } else ToggleButtonDefaults.toggleButtonColors()
    }
}
