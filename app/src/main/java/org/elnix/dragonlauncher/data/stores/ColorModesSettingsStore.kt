package org.elnix.dragonlauncher.data.stores

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.BaseSettingsStore
import org.elnix.dragonlauncher.data.ColorCustomisationMode
import org.elnix.dragonlauncher.data.DefaultThemes
import org.elnix.dragonlauncher.data.applyDefaultThemeColors
import org.elnix.dragonlauncher.data.colorModeDatastore
import org.elnix.dragonlauncher.data.getEnumStrict
import org.elnix.dragonlauncher.data.helpers.ColorPickerMode
import org.elnix.dragonlauncher.data.putIfNonDefault
import org.elnix.dragonlauncher.data.stores.ColorModesSettingsStore.Keys.COLOR_CUSTOMISATION_MODE
import org.elnix.dragonlauncher.data.stores.ColorModesSettingsStore.Keys.COLOR_PICKER_MODE
import org.elnix.dragonlauncher.data.stores.ColorModesSettingsStore.Keys.DEFAULT_THEME

object ColorModesSettingsStore : BaseSettingsStore<Map<String, Any?>>() {

    override val name: String = "Color Modes"

    // -------------------------------------------------------------------------
    // Backup data class
    // -------------------------------------------------------------------------
    private data class ColorModesSettingsBackup(
        val colorPickerMode: ColorPickerMode = ColorPickerMode.SLIDERS,
        val colorCustomisationMode: ColorCustomisationMode = ColorCustomisationMode.DEFAULT,
        val defaultTheme: DefaultThemes = DefaultThemes.AMOLED,
    )

    private val defaults = ColorModesSettingsBackup()

    // -------------------------------------------------------------------------
    // Keys (String names used for backup + preference keys)
    // -------------------------------------------------------------------------
    private object Keys {
        val COLOR_PICKER_MODE = stringPreferencesKey("colorPickerMode")
        val COLOR_CUSTOMISATION_MODE = stringPreferencesKey("colorCustomisationMode")
        val DEFAULT_THEME = stringPreferencesKey("defaultTheme")
    }



    // -------------------------------------------------------------------------
    // Accessors + Mutators
    // -------------------------------------------------------------------------
    fun getColorPickerMode(ctx: Context): Flow<ColorPickerMode> =
        ctx.colorModeDatastore.data.map { prefs ->
            prefs[COLOR_PICKER_MODE]?.let { ColorPickerMode.valueOf(it) }
                ?: defaults.colorPickerMode
        }

    suspend fun setColorPickerMode(ctx: Context, mode: ColorPickerMode) {
        ctx.colorModeDatastore.edit { it[COLOR_PICKER_MODE] = mode.name }
    }

    fun getColorCustomisationMode(ctx: Context): Flow<ColorCustomisationMode> =
        ctx.colorModeDatastore.data.map { prefs ->
            prefs[COLOR_CUSTOMISATION_MODE]?.let { ColorCustomisationMode.valueOf(it) }
                ?: defaults.colorCustomisationMode
        }

    suspend fun setColorCustomisationMode(ctx: Context, mode: ColorCustomisationMode) {
        ctx.colorModeDatastore.edit { it[COLOR_CUSTOMISATION_MODE] = mode.name }
    }

    fun getDefaultTheme(ctx: Context): Flow<DefaultThemes> =
        ctx.colorModeDatastore.data.map { prefs ->
            prefs[DEFAULT_THEME]?.let { DefaultThemes.valueOf(it) }
                ?: defaults.defaultTheme
        }

    suspend fun setDefaultTheme(ctx: Context, mode: DefaultThemes) {
        ctx.colorModeDatastore.edit { it[DEFAULT_THEME] = mode.name }
    }

    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------
    override suspend fun resetAll(ctx: Context) {
        ctx.colorModeDatastore.edit { prefs ->
            prefs.remove(COLOR_PICKER_MODE)
            prefs.remove(COLOR_CUSTOMISATION_MODE)
            prefs.remove(DEFAULT_THEME)
        }
    }

    // -------------------------------------------------------------------------
    // Backup export
    // -------------------------------------------------------------------------
    override suspend fun getAll(ctx: Context): Map<String, Any> {
        val prefs = ctx.colorModeDatastore.data.first()

        return buildMap {

            putIfNonDefault(
                COLOR_PICKER_MODE,
                prefs[COLOR_PICKER_MODE],
                defaults.colorPickerMode.name
            )

            putIfNonDefault(
                COLOR_CUSTOMISATION_MODE,
                prefs[COLOR_CUSTOMISATION_MODE],
                defaults.colorCustomisationMode.name
            )

            putIfNonDefault(
                DEFAULT_THEME,
                prefs[DEFAULT_THEME],
                defaults.defaultTheme.name
            )
        }
    }


    override suspend fun setAll(ctx: Context, value: Map<String, Any?>) {

        val backup = ColorModesSettingsBackup(
            colorPickerMode = getEnumStrict(
                value,
                COLOR_PICKER_MODE,
                defaults.colorPickerMode
            ),
            colorCustomisationMode = getEnumStrict(
                value,
                COLOR_CUSTOMISATION_MODE,
                defaults.colorCustomisationMode
            ),
            defaultTheme = getEnumStrict(
                value,
                DEFAULT_THEME,
                defaults.defaultTheme
            )
        )

        ctx.colorModeDatastore.edit { prefs ->
            prefs[COLOR_PICKER_MODE] = backup.colorPickerMode.name
            prefs[COLOR_CUSTOMISATION_MODE] = backup.colorCustomisationMode.name
            prefs[DEFAULT_THEME] = backup.defaultTheme.name
        }

        // Apply colorscheme
        applyDefaultThemeColors(ctx, backup.defaultTheme)
    }
}
