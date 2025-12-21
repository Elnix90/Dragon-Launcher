package org.elnix.dragonlauncher.data.stores

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.BackupTypeException
import org.elnix.dragonlauncher.data.BaseSettingsStore
import org.elnix.dragonlauncher.data.ColorCustomisationMode
import org.elnix.dragonlauncher.data.DefaultThemes
import org.elnix.dragonlauncher.data.applyDefaultThemeColors
import org.elnix.dragonlauncher.data.colorModeDatastore
import org.elnix.dragonlauncher.data.helpers.ColorPickerMode

object ColorModesSettingsStore : BaseSettingsStore() {

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
        const val COLOR_PICKER_MODE = "colorPickerMode"
        const val COLOR_CUSTOMISATION_MODE = "colorCustomisationMode"
        const val DEFAULT_THEME = "defaultTheme"
    }

    private val COLOR_PICKER_MODE = stringPreferencesKey(Keys.COLOR_PICKER_MODE)
    private val COLOR_CUSTOMISATION_MODE = stringPreferencesKey(Keys.COLOR_CUSTOMISATION_MODE)
    private val DEFAULT_THEME = stringPreferencesKey(Keys.DEFAULT_THEME)

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
    suspend fun getAll(ctx: Context): Map<String, String> {
        val prefs = ctx.colorModeDatastore.data.first()

        return buildMap {
            fun putIfNonDefault(key: String, value: Any?, defaultVal: Any) {
                if (value != null && value != defaultVal) {
                    put(key, value.toString())
                }
            }

            putIfNonDefault(
                Keys.COLOR_PICKER_MODE,
                prefs[COLOR_PICKER_MODE],
                defaults.colorPickerMode.name
            )

            putIfNonDefault(
                Keys.COLOR_CUSTOMISATION_MODE,
                prefs[COLOR_CUSTOMISATION_MODE],
                defaults.colorCustomisationMode.name
            )

            putIfNonDefault(
                Keys.DEFAULT_THEME,
                prefs[DEFAULT_THEME],
                defaults.defaultTheme.name
            )
        }
    }

    // -------------------------------------------------------------------------
    // Backup import (strict)
    // -------------------------------------------------------------------------
    suspend fun setAll(ctx: Context, raw: Map<String, Any?>) {

        fun getEnumStrict(
            key: String,
            values: Array<out Enum<*>>,
            defaultValue: Enum<*>
        ): String {
            val v = raw[key] ?: return defaultValue.name
            if (v !is String) {
                throw BackupTypeException(
                    key, "String (Enum name)", v::class.simpleName, v
                )
            }
            if (values.none { it.name == v }) {
                throw BackupTypeException(
                    key, "one of ${values.joinToString()}", v, v
                )
            }
            return v
        }

        val backup = ColorModesSettingsBackup(
            colorPickerMode = ColorPickerMode.valueOf(
                getEnumStrict(
                    Keys.COLOR_PICKER_MODE,
                    ColorPickerMode.entries.toTypedArray(),
                    defaults.colorPickerMode
                )
            ),
            colorCustomisationMode = ColorCustomisationMode.valueOf(
                getEnumStrict(
                    Keys.COLOR_CUSTOMISATION_MODE,
                    ColorCustomisationMode.entries.toTypedArray(),
                    defaults.colorCustomisationMode
                )
            ),
            defaultTheme = DefaultThemes.valueOf(
                getEnumStrict(
                    Keys.DEFAULT_THEME,
                    DefaultThemes.entries.toTypedArray(),
                    defaults.defaultTheme
                )
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
