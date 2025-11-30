package org.elnix.dragonlauncher.data


import android.content.Context
import android.content.res.Configuration
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore
import org.elnix.dragonlauncher.ui.theme.AmoledDefault
import org.elnix.dragonlauncher.ui.theme.DarkDefault
import org.elnix.dragonlauncher.ui.theme.LightDefault
import org.elnix.dragonlauncher.ui.theme.ThemeColors

suspend fun applyDefaultThemeColors(ctx: Context, theme: DefaultThemes) {
    setThemeColors(ctx, getDefaultColorScheme(ctx, theme))
}

fun getDefaultColorScheme(ctx: Context, theme: DefaultThemes) = when (theme) {
    DefaultThemes.LIGHT -> LightDefault
    DefaultThemes.DARK -> DarkDefault
    DefaultThemes.AMOLED -> AmoledDefault
    DefaultThemes.SYSTEM -> {
        val nightModeFlags = ctx.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            DarkDefault
        } else {
            LightDefault
        }
    }
}


private suspend fun setThemeColors(ctx: Context, colors: ThemeColors) {
    ColorSettingsStore.setPrimary(ctx, colors.Primary)
    ColorSettingsStore.setOnPrimary(ctx, colors.OnPrimary)
    ColorSettingsStore.setSecondary(ctx, colors.Secondary)
    ColorSettingsStore.setOnSecondary(ctx, colors.OnSecondary)
    ColorSettingsStore.setTertiary(ctx, colors.Tertiary)
    ColorSettingsStore.setOnTertiary(ctx, colors.OnTertiary)
    ColorSettingsStore.setBackground(ctx, colors.Background)
    ColorSettingsStore.setOnBackground(ctx, colors.OnBackground)
    ColorSettingsStore.setSurface(ctx, colors.Surface)
    ColorSettingsStore.setOnSurface(ctx, colors.OnSurface)
    ColorSettingsStore.setError(ctx, colors.Error)
    ColorSettingsStore.setOnError(ctx, colors.OnError)
    ColorSettingsStore.setOutline(ctx, colors.Outline)
    ColorSettingsStore.setAngleLineColor(ctx, colors.AngleLineColor)
    ColorSettingsStore.setCircleColor(ctx, colors.CircleColor)
//    ColorSettingsStore.setDelete(ctx, colors.Delete)
//    ColorSettingsStore.setEdit(ctx, colors.Edit)
//    ColorSettingsStore.setComplete(ctx, colors.Complete)
}