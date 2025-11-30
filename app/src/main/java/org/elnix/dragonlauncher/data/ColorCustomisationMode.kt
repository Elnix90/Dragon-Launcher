package org.elnix.dragonlauncher.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.R

enum class ColorCustomisationMode { DEFAULT, NORMAL, ALL }

enum class DefaultThemes { LIGHT, DARK, AMOLED, SYSTEM }

@Composable
fun colorCustomizationModeName(mode: ColorCustomisationMode) =
    when (mode) {
        ColorCustomisationMode.DEFAULT -> stringResource(R.string.color_mode_default)
        ColorCustomisationMode.NORMAL -> stringResource(R.string.color_mode_normal)
        ColorCustomisationMode.ALL -> stringResource(R.string.color_mode_all)
    }


@Composable
fun defaultThemeName(mode: DefaultThemes) =
    when (mode) {
        DefaultThemes.LIGHT -> stringResource(R.string.light_theme)
        DefaultThemes.DARK -> stringResource(R.string.dark_theme)
        DefaultThemes.AMOLED -> stringResource(R.string.amoled_theme)
        DefaultThemes.SYSTEM -> stringResource(R.string.system_theme)
    }
