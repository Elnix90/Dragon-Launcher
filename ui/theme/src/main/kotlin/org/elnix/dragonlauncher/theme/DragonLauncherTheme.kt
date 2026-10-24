package org.elnix.dragonlauncher.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.model.enumsui.toggle.DefaultThemes
import org.elnix.dragonlauncher.base.theme.AmoledDragonColorScheme
import org.elnix.dragonlauncher.base.theme.DarkDragonColorScheme
import org.elnix.dragonlauncher.base.theme.DefaultExtraColors
import org.elnix.dragonlauncher.base.theme.LightDragonColorScheme
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.models.ColorsViewModel
import org.elnix.dragonlauncher.models.FontViewModel
import org.elnix.dragonlauncher.settings.stores.map.ColorModesSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.composition.LocalUseCustomColorChannels
import io.github.elnix90.shared.ui.composition.LocalUseCustomColorChannels as LocalSharedUseCustomColorChannels

@Composable
fun rememberCurrentColorScheme(
	colorsViewModel: ColorsViewModel = activityViewModel()
): State<ColorScheme> {
	val ctx = LocalContext.current

	val darkTheme = isSystemInDarkTheme()
	val dynamicColors by ColorModesSettingsStore.dynamicColors.asState()

	val defaultTheme by ColorModesSettingsStore.defaultTheme.asState()
	val customScheme by colorsViewModel.colorService.colors.collectAsState()
	val systemScheme = systemColorScheme(dynamicColors, darkTheme)

	return remember(
		defaultTheme,
		systemScheme,
		customScheme,
		dynamicColors,
		darkTheme
	) {
		derivedStateOf {
			when (defaultTheme) {
				DefaultThemes.Light -> if (dynamicColors &&
					Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
				) {
					dynamicLightColorScheme(ctx)
				} else {
					LightDragonColorScheme
				}

				DefaultThemes.Dark -> if (dynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) dynamicDarkColorScheme(ctx) else DarkDragonColorScheme

				DefaultThemes.Amoled -> AmoledDragonColorScheme

				DefaultThemes.System -> systemScheme

				DefaultThemes.Custom -> customScheme
			}
		}
	}
}

@Composable
fun DragonLauncherTheme(
	fontViewModel: FontViewModel = activityViewModel(),
	colorsViewModel: ColorsViewModel = activityViewModel(),
	content: @Composable () -> Unit
) {
	val useCustomColorChannels by ColorModesSettingsStore.useCustomColorChannels.asState()

	val extraColors by colorsViewModel.colorService.extraColors.collectAsState(DefaultExtraColors)
	val typography by fontViewModel.typography.collectAsState()
	val colorScheme by rememberCurrentColorScheme()

	CompositionLocalProvider(
		LocalExtraColors provides extraColors,
		LocalUseCustomColorChannels provides useCustomColorChannels,
		LocalSharedUseCustomColorChannels provides useCustomColorChannels
	) {
		MaterialExpressiveTheme(
			colorScheme = colorScheme,
			motionScheme = MotionScheme.expressive(),
			typography = typography,
			content = content
		)
	}
}
