package org.elnix.dragonlauncher.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.elnix.dragonlauncher.base.theme.AmoledDragonColorScheme
import org.elnix.dragonlauncher.base.theme.DarkDragonColorScheme
import org.elnix.dragonlauncher.base.theme.LightDragonColorScheme
import org.elnix.dragonlauncher.enumsui.toggle.DefaultThemes

@Composable
fun getSystemColorScheme(
    defaultTheme: DefaultThemes,
    dynamicColor: Boolean
): ColorScheme {
    val darkTheme = isSystemInDarkTheme()
    return when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }

        darkTheme -> {
            if (defaultTheme == DefaultThemes.AMOLED) AmoledDragonColorScheme
            else DarkDragonColorScheme
        }

        else -> LightDragonColorScheme
    }
}