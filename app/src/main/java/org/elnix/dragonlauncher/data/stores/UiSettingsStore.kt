package org.elnix.dragonlauncher.data.stores

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.uiDatastore


object UiSettingsStore {

    data class UiSettingsBackup(
        val rgbLoading: Boolean = true,
        val rgbLine: Boolean = true,
        val showLaunchingAppLabel: Boolean = true,
        val showLaunchingAppIcon: Boolean = true,
        val showAppLaunchPreviewCircle: Boolean = true,
        val fullscreen: Boolean = true,
        val autoOpenSingleMatch: Boolean = true,
    )


    private val RGB_LOADING = booleanPreferencesKey("rgb_loading")
    fun getRGBLoading(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[RGB_LOADING] ?: true }
    suspend fun setRGBLoading(ctx: Context, enabled: Boolean) {
        ctx.uiDatastore.edit { it[RGB_LOADING] = enabled }
    }

    private val RGB_LINE = booleanPreferencesKey("rgb_line")
    fun getRGBLine(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[RGB_LINE] ?: true }
    suspend fun setRGBLine(ctx: Context, enabled: Boolean) {
        ctx.uiDatastore.edit { it[RGB_LINE] = enabled }
    }
    private val SHOW_LAUNCHING_APP_LABEL = booleanPreferencesKey("show_launching_app_label")
    fun getShowLaunchingAppLabel(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[SHOW_LAUNCHING_APP_LABEL] ?: true }
    suspend fun setShowLaunchingAppLabel(ctx: Context, enabled: Boolean) {
        ctx.uiDatastore.edit { it[SHOW_LAUNCHING_APP_LABEL] = enabled }
    }

    private val SHOW_LAUNCHING_APP_ICON = booleanPreferencesKey("show_launching_app_icon")
    fun getShowLaunchingAppIcon(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[SHOW_LAUNCHING_APP_ICON] ?: true }
    suspend fun setShowLaunchingAppIcon(ctx: Context, enabled: Boolean) {
        ctx.uiDatastore.edit { it[SHOW_LAUNCHING_APP_ICON] = enabled }
    }

    private val SHOW_APP_LAUNCH_PREVIEW = booleanPreferencesKey("show_ap_launch_preview")
    fun getShowAppLaunchPreview(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[SHOW_APP_LAUNCH_PREVIEW] ?: true }
    suspend fun setShowAppLaunchPreview(ctx: Context, enabled: Boolean) {
        ctx.uiDatastore.edit { it[SHOW_APP_LAUNCH_PREVIEW] = enabled }
    }

    private val FULLSCREEN = booleanPreferencesKey("fullscreen")
    fun getFullscreen(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[FULLSCREEN] ?: true }
    suspend fun setFullscreen(ctx: Context, enabled: Boolean) {
        ctx.uiDatastore.edit { it[FULLSCREEN] = enabled }
    }

    private val AUTO_LAUNCH_SINGLE_MATCH = booleanPreferencesKey("auto_launch_single_match")
    fun getAutoLaunchSingleMatch(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[AUTO_LAUNCH_SINGLE_MATCH] ?: true }
    suspend fun setAutoLaunchSingleMatch(ctx: Context, enabled: Boolean) {
        ctx.uiDatastore.edit { it[AUTO_LAUNCH_SINGLE_MATCH] = enabled }
    }

    suspend fun resetAll(ctx: Context) {
        ctx.uiDatastore.edit { prefs ->
            prefs.remove(RGB_LOADING)
            prefs.remove(RGB_LINE)
            prefs.remove(SHOW_LAUNCHING_APP_LABEL)
            prefs.remove(SHOW_LAUNCHING_APP_ICON)
            prefs.remove(SHOW_APP_LAUNCH_PREVIEW)
            prefs.remove(FULLSCREEN)
            prefs.remove(AUTO_LAUNCH_SINGLE_MATCH)
        }
    }

    suspend fun getAll(ctx: Context): Map<String, String> {
        val prefs = ctx.uiDatastore.data.first()
        val defaults = UiSettingsBackup()

        return buildMap {

            fun putIfNonDefault(key: String, value: Any?, default: Any?) {
                if (value != null && value != default) {
                    put(key, value.toString())
                }
            }

            putIfNonDefault(RGB_LOADING.name, prefs[RGB_LOADING], defaults.rgbLoading)
            putIfNonDefault(RGB_LINE.name, prefs[RGB_LINE], defaults.rgbLine)
            putIfNonDefault(SHOW_LAUNCHING_APP_LABEL.name, prefs[SHOW_LAUNCHING_APP_LABEL], defaults.showLaunchingAppLabel)
            putIfNonDefault(SHOW_LAUNCHING_APP_ICON.name, prefs[SHOW_LAUNCHING_APP_ICON], defaults.showLaunchingAppIcon)
            putIfNonDefault(SHOW_APP_LAUNCH_PREVIEW.name, prefs[SHOW_APP_LAUNCH_PREVIEW], defaults.showAppLaunchPreviewCircle)
            putIfNonDefault(FULLSCREEN.name, prefs[FULLSCREEN], defaults.fullscreen)
            putIfNonDefault(AUTO_LAUNCH_SINGLE_MATCH.name, prefs[AUTO_LAUNCH_SINGLE_MATCH], defaults.autoOpenSingleMatch)
        }
    }


    suspend fun setAll(ctx: Context, backup: Map<String, String>) {
        ctx.uiDatastore.edit { prefs ->

            backup[RGB_LOADING.name]?.let {
                prefs[RGB_LOADING] = it.toBoolean()
            }

            backup[RGB_LINE.name]?.let {
                prefs[RGB_LINE] = it.toBoolean()
            }

            backup[SHOW_LAUNCHING_APP_LABEL.name]?.let {
                prefs[SHOW_LAUNCHING_APP_LABEL] = it.toBoolean()
            }

            backup[SHOW_LAUNCHING_APP_ICON.name]?.let {
                prefs[SHOW_LAUNCHING_APP_ICON] = it.toBoolean()
            }

            backup[SHOW_APP_LAUNCH_PREVIEW.name]?.let {
                prefs[SHOW_APP_LAUNCH_PREVIEW] = it.toBoolean()
            }

            backup[FULLSCREEN.name]?.let {
                prefs[FULLSCREEN] = it.toBoolean()
            }

            backup[AUTO_LAUNCH_SINGLE_MATCH.name]?.let {
                prefs[AUTO_LAUNCH_SINGLE_MATCH] = it.toBoolean()
            }
        }
    }
}