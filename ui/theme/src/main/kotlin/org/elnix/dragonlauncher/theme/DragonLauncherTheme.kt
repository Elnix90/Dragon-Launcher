package org.elnix.dragonlauncher.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.theme.AmoledDragonColorScheme
import org.elnix.dragonlauncher.base.theme.DarkDragonColorScheme
import org.elnix.dragonlauncher.base.theme.DefaultExtraColors
import org.elnix.dragonlauncher.base.theme.LightDragonColorScheme
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.enumsui.toggle.DefaultThemes
import org.elnix.dragonlauncher.models.ColorsViewModel
import org.elnix.dragonlauncher.models.FontViewModel
import org.elnix.dragonlauncher.settings.stores.map.ColorModesSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.composition.LocalUseCustomColorChannels

@Composable
public fun rememberCurrentColorScheme(
    colorsViewModel: ColorsViewModel = activityViewModel()
): State<ColorScheme> {

    val defaultTheme by ColorModesSettingsStore.defaultTheme.asState()
    val customScheme by colorsViewModel.colorscheme.collectAsState()
    val systemScheme = systemColorScheme()

    return remember(
        defaultTheme,
        systemScheme,
        customScheme
    ) {
        derivedStateOf {
            when (defaultTheme) {
                DefaultThemes.Light -> LightDragonColorScheme
                DefaultThemes.Dark -> DarkDragonColorScheme
                DefaultThemes.Amoled -> AmoledDragonColorScheme
                DefaultThemes.System -> systemScheme
                DefaultThemes.Custom -> customScheme
            }
        }
    }
}

@Composable
public fun DragonLauncherTheme(
    fontViewModel: FontViewModel = activityViewModel(),
    colorsViewModel: ColorsViewModel = activityViewModel(),
    content: @Composable () -> Unit
) {
    val useCustomColorChannels by ColorModesSettingsStore.useCustomColorChannels.asState()

    val extraColors by colorsViewModel.extraColors.collectAsState(DefaultExtraColors)
    val typography by fontViewModel.typography.collectAsState()
    val colorScheme by rememberCurrentColorScheme()

    CompositionLocalProvider(
        LocalExtraColors provides extraColors,
        LocalUseCustomColorChannels provides useCustomColorChannels,
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = MotionScheme.expressive(),
            typography = typography,
            content = content
        )
    }
}
