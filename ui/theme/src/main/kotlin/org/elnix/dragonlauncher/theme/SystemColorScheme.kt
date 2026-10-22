package org.elnix.dragonlauncher.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.elnix.dragonlauncher.base.theme.DarkDragonColorScheme
import org.elnix.dragonlauncher.base.theme.LightDragonColorScheme

@Composable
fun systemColorScheme(
	dynamicColors: Boolean,
	darkTheme: Boolean
): ColorScheme {
	val ctx = LocalContext.current

	return remember(darkTheme, dynamicColors, ctx) {
		when {
			dynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
				if (darkTheme) {
					dynamicDarkColorScheme(ctx)
				} else {
					dynamicLightColorScheme(ctx)
				}
			}

			darkTheme -> {
				DarkDragonColorScheme
			}

			else -> {
				LightDragonColorScheme
			}
		}
	}
}
