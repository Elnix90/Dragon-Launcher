package org.elnix.dragonlauncher.theme


import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.elnix.dragonlauncher.base.theme.DefaultExtraColors
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.models.ColorsViewModel
import org.elnix.dragonlauncher.models.FontViewModel
import org.elnix.dragonlauncher.settings.stores.map.ColorModesSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.composition.LocalUseCustomColorChannels


//@Composable
//fun getCustomColorScheme(dynamicColor: Boolean): ColorScheme =
//    rememberCustomColorScheme(getSystemColorScheme(SYSTEM, dynamicColor))
//

//@Composable
//private fun getDefaultColorScheme(
//    defaultTheme: DefaultThemes,
//    dynamicColor: Boolean
//): ColorScheme =
//    when (defaultTheme) {
//        LIGHT -> LightDragonColorScheme
//        DARK -> DarkDragonColorScheme
//        AMOLED -> AmoledDragonColorScheme
//        SYSTEM -> getSystemColorScheme(defaultTheme, dynamicColor)
//        CUSTOM -> getCustomColorScheme(dynamicColor)
//    }

@Composable
fun DragonLauncherTheme(
    content: @Composable () -> Unit
) {
    val dynamicColor by ColorModesSettingsStore.dynamicColor.asState()
    val defaultTheme by ColorModesSettingsStore.defaultTheme.asState()
    val useCustomColorChannels by UiSettingsStore.useCustomColorChannels.asState()

    val colorsViewModel: ColorsViewModel = activityViewModel()
    val colorService = colorsViewModel.colorService

    val colorScheme by colorService.colors.collectAsState(getSystemColorScheme(defaultTheme, dynamicColor))
    val extraColors by colorService.extraColors.collectAsState(DefaultExtraColors)

    val fontViewModel: FontViewModel = hiltViewModel()
    val typography by fontViewModel.typography.collectAsState()


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
