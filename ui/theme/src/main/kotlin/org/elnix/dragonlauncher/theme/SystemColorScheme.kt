package org.elnix.dragonlauncher.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.theme.DarkDragonColorScheme
import org.elnix.dragonlauncher.base.theme.LightDragonColorScheme
import org.elnix.dragonlauncher.settings.stores.map.ColorModesSettingsStore

@Composable
fun systemColorScheme(): ColorScheme {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()
    val dynamicColors by ColorModesSettingsStore.dynamicColors.asState()

    return remember(darkTheme, dynamicColors, context) {
        when {
            dynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                if (darkTheme) {
                    dynamicDarkColorScheme(context)
                } else {
                    dynamicLightColorScheme(context)
                }

            darkTheme ->
                DarkDragonColorScheme

            else ->
                LightDragonColorScheme
        }
    }
}
