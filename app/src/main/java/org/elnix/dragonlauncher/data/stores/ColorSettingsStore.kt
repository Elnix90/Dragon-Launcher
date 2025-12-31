package org.elnix.dragonlauncher.data.stores

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.BaseSettingsStore
import org.elnix.dragonlauncher.data.ColorCustomisationMode
import org.elnix.dragonlauncher.data.DefaultThemes
import org.elnix.dragonlauncher.data.colorDatastore
import org.elnix.dragonlauncher.data.getDefaultColorScheme
import org.elnix.dragonlauncher.data.getIntStrict
import org.elnix.dragonlauncher.data.putIfNonDefault
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.ALL
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.ANGLE_LINE_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.BACKGROUND_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.CIRCLE_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.CONTROL_PANEL_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.ERROR_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.GO_PARENT_NEST
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.LAUNCHER_SETTINGS_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.LAUNCH_APP_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.LOCK_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.NOTIFICATION_SHADE_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.ON_BACKGROUND_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.ON_ERROR_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.ON_PRIMARY_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.ON_SECONDARY_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.ON_SURFACE_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.ON_TERTIARY_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.OPEN_APP_DRAWER_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.OPEN_CIRCLE_NEST
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.OPEN_FILE_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.OPEN_RECENT_APPS
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.OPEN_URL_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.OUTLINE_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.PRIMARY_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.RELOAD_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.SECONDARY_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.SURFACE_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.Keys.TERTIARY_COLOR
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setAngleLineColor
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setBackground
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setCircleColor
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setControlPanelColor
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setError
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setGoParentNest
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setLaunchAppColor
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setLauncherSettingsColor
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setLockColor
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setNotificationShadeColor
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setOnBackground
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setOnError
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setOnPrimary
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setOnSecondary
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setOnSurface
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setOnTertiary
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setOpenAppDrawerColor
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setOpenCircleNest
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setOpenFileColor
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setOpenUrlColor
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setOutline
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setPrimary
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setReloadColor
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setSecondary
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setSurface
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setTertiary
import org.elnix.dragonlauncher.data.uiDatastore
import org.elnix.dragonlauncher.ui.theme.AmoledDefault
import org.elnix.dragonlauncher.ui.theme.ThemeColors
import org.elnix.dragonlauncher.utils.colors.randomColor

object ColorSettingsStore : BaseSettingsStore<Map<String, Any?>>() {
    override val name: String = "Colors"


    private object Keys {
        val PRIMARY_COLOR = intPreferencesKey("primary_color")
        val ON_PRIMARY_COLOR = intPreferencesKey("on_primary_color")
        val SECONDARY_COLOR = intPreferencesKey("secondary_color")
        val ON_SECONDARY_COLOR = intPreferencesKey("on_secondary_color")
        val TERTIARY_COLOR = intPreferencesKey("tertiary_color")
        val ON_TERTIARY_COLOR = intPreferencesKey("on_tertiary_color")
        val BACKGROUND_COLOR = intPreferencesKey("background_color")
        val ON_BACKGROUND_COLOR = intPreferencesKey("on_background_color")
        val SURFACE_COLOR = intPreferencesKey("surface_color")
        val ON_SURFACE_COLOR = intPreferencesKey("on_surface_color")
        val ERROR_COLOR = intPreferencesKey("error_color")
        val ON_ERROR_COLOR = intPreferencesKey("on_error_color")
        val OUTLINE_COLOR = intPreferencesKey("outline_color")
        val ANGLE_LINE_COLOR = intPreferencesKey("delete_color")
        val CIRCLE_COLOR = intPreferencesKey("circle_color")
        val LAUNCH_APP_COLOR = intPreferencesKey("launch_app_color")
        val OPEN_URL_COLOR = intPreferencesKey("open_url_color")
        val NOTIFICATION_SHADE_COLOR = intPreferencesKey("notification_shade_color")
        val CONTROL_PANEL_COLOR = intPreferencesKey("control_panel_color")
        val OPEN_APP_DRAWER_COLOR = intPreferencesKey("open_app_drawer_color")
        val LAUNCHER_SETTINGS_COLOR = intPreferencesKey("launcher_settings_color")
        val LOCK_COLOR = intPreferencesKey("lock_color")
        val OPEN_FILE_COLOR = intPreferencesKey("open_file_color")
        val RELOAD_COLOR = intPreferencesKey("reload_color")
        val OPEN_RECENT_APPS = intPreferencesKey("open_recent_apps")

        val OPEN_CIRCLE_NEST = intPreferencesKey("open_circle_nest")
        val GO_PARENT_NEST = intPreferencesKey("go_parent_nest")

        val ALL = listOf(
            PRIMARY_COLOR,
            ON_PRIMARY_COLOR,
            SECONDARY_COLOR,
            ON_SECONDARY_COLOR,
            TERTIARY_COLOR,
            ON_TERTIARY_COLOR,
            BACKGROUND_COLOR,
            ON_BACKGROUND_COLOR,
            SURFACE_COLOR,
            ON_SURFACE_COLOR,
            ERROR_COLOR,
            ON_ERROR_COLOR,
            OUTLINE_COLOR,
            ANGLE_LINE_COLOR,
            CIRCLE_COLOR,
            LAUNCH_APP_COLOR,
            OPEN_URL_COLOR,
            NOTIFICATION_SHADE_COLOR,
            CONTROL_PANEL_COLOR,
            OPEN_APP_DRAWER_COLOR,
            LAUNCHER_SETTINGS_COLOR,
            LOCK_COLOR,
            OPEN_FILE_COLOR,
            RELOAD_COLOR,
            OPEN_RECENT_APPS,
            OPEN_CIRCLE_NEST,
            GO_PARENT_NEST
        )
    }



    // ------------------------------------------
    //            NORMAL COLORS
    // ------------------------------------------

    fun getPrimary(ctx: Context) =
        ctx.colorDatastore.data.map { it[PRIMARY_COLOR]?.let { color -> Color(color) } }

    suspend fun setPrimary(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[PRIMARY_COLOR] = color.toArgb() }
    }

    fun getOnPrimary(ctx: Context) =
        ctx.colorDatastore.data.map { it[ON_PRIMARY_COLOR]?.let { color -> Color(color) } }

    suspend fun setOnPrimary(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[ON_PRIMARY_COLOR] = color.toArgb() }
    }

    fun getSecondary(ctx: Context) =
        ctx.colorDatastore.data.map { it[SECONDARY_COLOR]?.let { color -> Color(color) } }

    suspend fun setSecondary(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[SECONDARY_COLOR] = color.toArgb() }
    }

    fun getOnSecondary(ctx: Context) =
        ctx.colorDatastore.data.map { it[ON_SECONDARY_COLOR]?.let { color -> Color(color) } }

    suspend fun setOnSecondary(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[ON_SECONDARY_COLOR] = color.toArgb() }
    }

    fun getTertiary(ctx: Context) =
        ctx.colorDatastore.data.map { it[TERTIARY_COLOR]?.let { color -> Color(color) } }

    suspend fun setTertiary(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[TERTIARY_COLOR] = color.toArgb() }
    }

    fun getOnTertiary(ctx: Context) =
        ctx.colorDatastore.data.map { it[ON_TERTIARY_COLOR]?.let { color -> Color(color) } }

    suspend fun setOnTertiary(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[ON_TERTIARY_COLOR] = color.toArgb() }
    }

    fun getBackground(ctx: Context) =
        ctx.colorDatastore.data.map { it[BACKGROUND_COLOR]?.let { color -> Color(color) } }

    suspend fun setBackground(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[BACKGROUND_COLOR] = color.toArgb() }
    }

    fun getOnBackground(ctx: Context) =
        ctx.colorDatastore.data.map { it[ON_BACKGROUND_COLOR]?.let { color -> Color(color) } }

    suspend fun setOnBackground(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[ON_BACKGROUND_COLOR] = color.toArgb() }
    }

    fun getSurface(ctx: Context) =
        ctx.colorDatastore.data.map { it[SURFACE_COLOR]?.let { color -> Color(color) } }

    suspend fun setSurface(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[SURFACE_COLOR] = color.toArgb() }
    }

    fun getOnSurface(ctx: Context) =
        ctx.colorDatastore.data.map { it[ON_SURFACE_COLOR]?.let { color -> Color(color) } }

    suspend fun setOnSurface(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[ON_SURFACE_COLOR] = color.toArgb() }
    }

    fun getError(ctx: Context) =
        ctx.colorDatastore.data.map { it[ERROR_COLOR]?.let { color -> Color(color) } }

    suspend fun setError(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[ERROR_COLOR] = color.toArgb() }
    }

    fun getOnError(ctx: Context) =
        ctx.colorDatastore.data.map { it[ON_ERROR_COLOR]?.let { color -> Color(color) } }

    suspend fun setOnError(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[ON_ERROR_COLOR] = color.toArgb() }
    }

    fun getOutline(ctx: Context) =
        ctx.colorDatastore.data.map { it[OUTLINE_COLOR]?.let { color -> Color(color) } }

    suspend fun setOutline(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[OUTLINE_COLOR] = color.toArgb() }
    }

    // ------------------------------------------
    //            CUSTOM COLORS
    // ------------------------------------------

    fun getAngleLineColor(ctx: Context) =
        ctx.colorDatastore.data.map { it[ANGLE_LINE_COLOR]?.let { color -> Color(color) } }

    suspend fun setAngleLineColor(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[ANGLE_LINE_COLOR] = color.toArgb() }
    }

    fun getCircleColor(ctx: Context) =
        ctx.colorDatastore.data.map {
            it[CIRCLE_COLOR]?.let { color -> Color(color) } ?: AmoledDefault.CircleColor
        }

    suspend fun setCircleColor(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[CIRCLE_COLOR] = color.toArgb() }
    }

    fun getLaunchAppColor(ctx: Context) =
        ctx.colorDatastore.data.map { it[LAUNCH_APP_COLOR]?.let { c -> Color(c) } }

    suspend fun setLaunchAppColor(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[LAUNCH_APP_COLOR] = color.toArgb() }
    }

    fun getOpenUrlColor(ctx: Context) =
        ctx.colorDatastore.data.map { it[OPEN_URL_COLOR]?.let { c -> Color(c) } }

    suspend fun setOpenUrlColor(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[OPEN_URL_COLOR] = color.toArgb() }
    }

    fun getNotificationShadeColor(ctx: Context) =
        ctx.colorDatastore.data.map { it[NOTIFICATION_SHADE_COLOR]?.let { c -> Color(c) } }

    suspend fun setNotificationShadeColor(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[NOTIFICATION_SHADE_COLOR] = color.toArgb() }
    }

    fun getControlPanelColor(ctx: Context) =
        ctx.colorDatastore.data.map { it[CONTROL_PANEL_COLOR]?.let { c -> Color(c) } }

    suspend fun setControlPanelColor(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[CONTROL_PANEL_COLOR] = color.toArgb() }
    }

    fun getOpenAppDrawerColor(ctx: Context) =
        ctx.colorDatastore.data.map { it[OPEN_APP_DRAWER_COLOR]?.let { c -> Color(c) } }

    suspend fun setOpenAppDrawerColor(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[OPEN_APP_DRAWER_COLOR] = color.toArgb() }
    }

    fun getLauncherSettingsColor(ctx: Context) =
        ctx.colorDatastore.data.map { it[LAUNCHER_SETTINGS_COLOR]?.let { c -> Color(c) } }

    suspend fun setLauncherSettingsColor(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[LAUNCHER_SETTINGS_COLOR] = color.toArgb() }
    }

    fun getLockColor(ctx: Context) =
        ctx.colorDatastore.data.map { it[LOCK_COLOR]?.let { c -> Color(c) } }

    suspend fun setLockColor(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[LOCK_COLOR] = color.toArgb() }
    }

    fun getOpenFileColor(ctx: Context) =
        ctx.colorDatastore.data.map { it[OPEN_FILE_COLOR]?.let { c -> Color(c) } }

    suspend fun setOpenFileColor(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[OPEN_FILE_COLOR] = color.toArgb() }
    }

    fun getReloadColor(ctx: Context) =
        ctx.colorDatastore.data.map { it[RELOAD_COLOR]?.let { c -> Color(c) } }

    suspend fun setReloadColor(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[RELOAD_COLOR] = color.toArgb() }
    }

    fun getOpenRecentApps(ctx: Context) =
        ctx.colorDatastore.data.map { it[OPEN_RECENT_APPS]?.let { c -> Color(c) } }

    suspend fun setOpenRecentApps(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[OPEN_RECENT_APPS] = color.toArgb() }
    }

    fun getOpenCircleNest(ctx: Context) =
        ctx.colorDatastore.data.map { it[OPEN_CIRCLE_NEST]?.let { c -> Color(c) } }

    suspend fun setOpenCircleNest(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[OPEN_CIRCLE_NEST] = color.toArgb() }
    }

    fun getGoParentNest(ctx: Context) =
        ctx.colorDatastore.data.map { it[GO_PARENT_NEST]?.let { c -> Color(c) } }

    suspend fun setGoParentNest(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[GO_PARENT_NEST] = color.toArgb() }
    }

    suspend fun resetColors(
        ctx: Context,
        selectedColorCustomisationMode: ColorCustomisationMode,
        selectedMode: DefaultThemes
    ) {

        val themeColors: ThemeColors = when (selectedColorCustomisationMode) {
            ColorCustomisationMode.DEFAULT -> getDefaultColorScheme(ctx, selectedMode)
            ColorCustomisationMode.NORMAL, ColorCustomisationMode.ALL -> AmoledDefault
        }

        applyThemeColors(ctx, themeColors)
    }


    suspend fun setAllRandomColors(ctx: Context) {
        val random = { randomColor() }

        setPrimary(ctx, random())
        setOnPrimary(ctx, random())
        setSecondary(ctx, random())
        setOnSecondary(ctx, random())
        setTertiary(ctx, random())
        setOnTertiary(ctx, random())
        setBackground(ctx, random())
        setOnBackground(ctx, random())
        setSurface(ctx, random())
        setOnSurface(ctx, random())
        setError(ctx, random())
        setOnError(ctx, random())
        setOutline(ctx, random())
        setAngleLineColor(ctx, random())
        setCircleColor(ctx, random())

        setLaunchAppColor(ctx, random())
        setOpenUrlColor(ctx, random())
        setNotificationShadeColor(ctx, random())
        setControlPanelColor(ctx, random())
        setOpenAppDrawerColor(ctx, random())
        setLauncherSettingsColor(ctx, random())
        setLockColor(ctx, random())
        setOpenFileColor(ctx, random())
        setReloadColor(ctx, random())
        setOpenRecentApps(ctx, random())
        setOpenCircleNest(ctx, random())
        setGoParentNest(ctx, random())
    }

    override suspend fun resetAll(ctx: Context) {
        ctx.uiDatastore.edit { prefs ->
            ALL.forEach { prefs.remove(it) }
        }
    }

    override suspend fun getAll(ctx: Context): Map<String, Any> {
        val prefs = ctx.colorDatastore.data.first()
        val colorMode = ColorModesSettingsStore.getColorCustomisationMode(ctx).first()
        val defaultTheme = ColorModesSettingsStore.getDefaultTheme(ctx).first()

        val default = if (colorMode == ColorCustomisationMode.DEFAULT) getDefaultColorScheme(
            ctx,
            defaultTheme
        )
        else AmoledDefault
        return buildMap {

            putIfNonDefault(PRIMARY_COLOR, prefs[PRIMARY_COLOR], default.Primary)
            putIfNonDefault(ON_PRIMARY_COLOR, prefs[ON_PRIMARY_COLOR], default.OnPrimary)
            putIfNonDefault(SECONDARY_COLOR, prefs[SECONDARY_COLOR], default.Secondary)
            putIfNonDefault(ON_SECONDARY_COLOR, prefs[ON_SECONDARY_COLOR], default.OnSecondary)
            putIfNonDefault(TERTIARY_COLOR, prefs[TERTIARY_COLOR], default.Tertiary)
            putIfNonDefault(ON_TERTIARY_COLOR, prefs[ON_TERTIARY_COLOR], default.OnTertiary)
            putIfNonDefault(BACKGROUND_COLOR, prefs[BACKGROUND_COLOR], default.Background)
            putIfNonDefault(ON_BACKGROUND_COLOR, prefs[ON_BACKGROUND_COLOR], default.OnBackground)
            putIfNonDefault(SURFACE_COLOR, prefs[SURFACE_COLOR], default.Surface)
            putIfNonDefault(ON_SURFACE_COLOR, prefs[ON_SURFACE_COLOR], default.OnSurface)
            putIfNonDefault(ERROR_COLOR, prefs[ERROR_COLOR], default.Error)
            putIfNonDefault(ON_ERROR_COLOR, prefs[ON_ERROR_COLOR], default.OnError)
            putIfNonDefault(OUTLINE_COLOR, prefs[OUTLINE_COLOR], default.Outline)
            putIfNonDefault(ANGLE_LINE_COLOR, prefs[ANGLE_LINE_COLOR], default.AngleLineColor)
            putIfNonDefault(CIRCLE_COLOR, prefs[CIRCLE_COLOR], default.CircleColor)

            putIfNonDefault(LAUNCH_APP_COLOR, prefs[LAUNCH_APP_COLOR], default.LaunchAppColor)
            putIfNonDefault(OPEN_URL_COLOR, prefs[OPEN_URL_COLOR], default.OpenUrlColor)
            putIfNonDefault(
                NOTIFICATION_SHADE_COLOR,
                prefs[NOTIFICATION_SHADE_COLOR],
                default.NotificationShadeColor
            )
            putIfNonDefault(
                CONTROL_PANEL_COLOR,
                prefs[CONTROL_PANEL_COLOR],
                default.ControlPanelColor
            )
            putIfNonDefault(
                OPEN_APP_DRAWER_COLOR,
                prefs[OPEN_APP_DRAWER_COLOR],
                default.OpenAppDrawerColor
            )
            putIfNonDefault(
                LAUNCHER_SETTINGS_COLOR,
                prefs[LAUNCHER_SETTINGS_COLOR],
                default.LauncherSettingsColor
            )
            putIfNonDefault(LOCK_COLOR, prefs[LOCK_COLOR], default.LockColor)
            putIfNonDefault(OPEN_FILE_COLOR, prefs[OPEN_FILE_COLOR], default.OpenFileColor)
            putIfNonDefault(RELOAD_COLOR, prefs[RELOAD_COLOR], default.ReloadColor)
            putIfNonDefault(OPEN_RECENT_APPS, prefs[OPEN_RECENT_APPS], default.OpenRecentAppsColor)
            putIfNonDefault(OPEN_CIRCLE_NEST, prefs[OPEN_CIRCLE_NEST], default.OpenCircleNestColor)
            putIfNonDefault(GO_PARENT_NEST, prefs[GO_PARENT_NEST], default.GoParentNestColor)

        }
    }

    override suspend fun setAll(ctx: Context, value: Map<String, Any?>) {
        ctx.colorDatastore.edit { prefs ->

            value[PRIMARY_COLOR.name]?.let {
                prefs[PRIMARY_COLOR] =
                    getIntStrict(value, PRIMARY_COLOR, prefs[PRIMARY_COLOR] ?: 0)
            }

            value[ON_PRIMARY_COLOR.name]?.let {
                prefs[ON_PRIMARY_COLOR] =
                    getIntStrict(value, ON_PRIMARY_COLOR, prefs[ON_PRIMARY_COLOR] ?: 0)
            }

            value[SECONDARY_COLOR.name]?.let {
                prefs[SECONDARY_COLOR] =
                    getIntStrict(value, SECONDARY_COLOR, prefs[SECONDARY_COLOR] ?: 0)
            }

            value[ON_SECONDARY_COLOR.name]?.let {
                prefs[ON_SECONDARY_COLOR] =
                    getIntStrict(value, ON_SECONDARY_COLOR, prefs[ON_SECONDARY_COLOR] ?: 0)
            }

            value[TERTIARY_COLOR.name]?.let {
                prefs[TERTIARY_COLOR] =
                    getIntStrict(value, TERTIARY_COLOR, prefs[TERTIARY_COLOR] ?: 0)
            }

            value[ON_TERTIARY_COLOR.name]?.let {
                prefs[ON_TERTIARY_COLOR] =
                    getIntStrict(value, ON_TERTIARY_COLOR, prefs[ON_TERTIARY_COLOR] ?: 0)
            }

            value[BACKGROUND_COLOR.name]?.let {
                prefs[BACKGROUND_COLOR] =
                    getIntStrict(value, BACKGROUND_COLOR, prefs[BACKGROUND_COLOR] ?: 0)
            }

            value[ON_BACKGROUND_COLOR.name]?.let {
                prefs[ON_BACKGROUND_COLOR] =
                    getIntStrict(value, ON_BACKGROUND_COLOR, prefs[ON_BACKGROUND_COLOR] ?: 0)
            }

            value[SURFACE_COLOR.name]?.let {
                prefs[SURFACE_COLOR] =
                    getIntStrict(value, SURFACE_COLOR, prefs[SURFACE_COLOR] ?: 0)
            }

            value[ON_SURFACE_COLOR.name]?.let {
                prefs[ON_SURFACE_COLOR] =
                    getIntStrict(value, ON_SURFACE_COLOR, prefs[ON_SURFACE_COLOR] ?: 0)
            }

            value[ERROR_COLOR.name]?.let {
                prefs[ERROR_COLOR] =
                    getIntStrict(value, ERROR_COLOR, prefs[ERROR_COLOR] ?: 0)
            }

            value[ON_ERROR_COLOR.name]?.let {
                prefs[ON_ERROR_COLOR] =
                    getIntStrict(value, ON_ERROR_COLOR, prefs[ON_ERROR_COLOR] ?: 0)
            }

            value[OUTLINE_COLOR.name]?.let {
                prefs[OUTLINE_COLOR] =
                    getIntStrict(value, OUTLINE_COLOR, prefs[OUTLINE_COLOR] ?: 0)
            }

            value[ANGLE_LINE_COLOR.name]?.let {
                prefs[ANGLE_LINE_COLOR] =
                    getIntStrict(value, ANGLE_LINE_COLOR, prefs[ANGLE_LINE_COLOR] ?: 0)
            }

            value[CIRCLE_COLOR.name]?.let {
                prefs[CIRCLE_COLOR] =
                    getIntStrict(value, CIRCLE_COLOR, prefs[CIRCLE_COLOR] ?: 0)
            }

            value[LAUNCH_APP_COLOR.name]?.let {
                prefs[LAUNCH_APP_COLOR] =
                    getIntStrict(value, LAUNCH_APP_COLOR, prefs[LAUNCH_APP_COLOR] ?: 0)
            }

            value[OPEN_URL_COLOR.name]?.let {
                prefs[OPEN_URL_COLOR] =
                    getIntStrict(value, OPEN_URL_COLOR, prefs[OPEN_URL_COLOR] ?: 0)
            }

            value[NOTIFICATION_SHADE_COLOR.name]?.let {
                prefs[NOTIFICATION_SHADE_COLOR] =
                    getIntStrict(
                        value,
                        NOTIFICATION_SHADE_COLOR,
                        prefs[NOTIFICATION_SHADE_COLOR] ?: 0
                    )
            }

            value[CONTROL_PANEL_COLOR.name]?.let {
                prefs[CONTROL_PANEL_COLOR] =
                    getIntStrict(value, CONTROL_PANEL_COLOR, prefs[CONTROL_PANEL_COLOR] ?: 0)
            }

            value[OPEN_APP_DRAWER_COLOR.name]?.let {
                prefs[OPEN_APP_DRAWER_COLOR] =
                    getIntStrict(value, OPEN_APP_DRAWER_COLOR, prefs[OPEN_APP_DRAWER_COLOR] ?: 0)
            }

            value[LAUNCHER_SETTINGS_COLOR.name]?.let {
                prefs[LAUNCHER_SETTINGS_COLOR] =
                    getIntStrict(
                        value,
                        LAUNCHER_SETTINGS_COLOR,
                        prefs[LAUNCHER_SETTINGS_COLOR] ?: 0
                    )
            }

            value[LOCK_COLOR.name]?.let {
                prefs[LOCK_COLOR] =
                    getIntStrict(value, LOCK_COLOR, prefs[LOCK_COLOR] ?: 0)
            }

            value[OPEN_FILE_COLOR.name]?.let {
                prefs[OPEN_FILE_COLOR] =
                    getIntStrict(value, OPEN_FILE_COLOR, prefs[OPEN_FILE_COLOR] ?: 0)
            }

            value[RELOAD_COLOR.name]?.let {
                prefs[RELOAD_COLOR] =
                    getIntStrict(value, RELOAD_COLOR, prefs[RELOAD_COLOR] ?: 0)
            }

            value[OPEN_RECENT_APPS.name]?.let {
                prefs[OPEN_RECENT_APPS] =
                    getIntStrict(value, OPEN_RECENT_APPS, prefs[OPEN_RECENT_APPS] ?: 0)
            }

            value[OPEN_CIRCLE_NEST.name]?.let {
                prefs[OPEN_CIRCLE_NEST] =
                    getIntStrict(value, OPEN_CIRCLE_NEST, prefs[OPEN_CIRCLE_NEST] ?: 0)
            }

            value[GO_PARENT_NEST.name]?.let {
                prefs[GO_PARENT_NEST] =
                    getIntStrict(value, GO_PARENT_NEST, prefs[GO_PARENT_NEST] ?: 0)
            }
        }
    }
}


private suspend fun applyThemeColors(ctx: Context, colors: ThemeColors) {
    setPrimary(ctx, colors.Primary)
    setOnPrimary(ctx, colors.OnPrimary)
    setSecondary(ctx, colors.Secondary)
    setOnSecondary(ctx, colors.OnSecondary)
    setTertiary(ctx, colors.Tertiary)
    setOnTertiary(ctx, colors.OnTertiary)
    setBackground(ctx, colors.Background)
    setOnBackground(ctx, colors.OnBackground)
    setSurface(ctx, colors.Surface)
    setOnSurface(ctx, colors.OnSurface)
    setError(ctx, colors.Error)
    setOnError(ctx, colors.OnError)
    setOutline(ctx, colors.Outline)
    setAngleLineColor(ctx, colors.AngleLineColor)
    setCircleColor(ctx, colors.CircleColor)

    setLaunchAppColor(ctx, colors.LaunchAppColor)
    setOpenUrlColor(ctx, colors.OpenUrlColor)
    setNotificationShadeColor(ctx, colors.NotificationShadeColor)
    setControlPanelColor(ctx, colors.ControlPanelColor)
    setOpenAppDrawerColor(ctx, colors.OpenAppDrawerColor)
    setLauncherSettingsColor(ctx, colors.LauncherSettingsColor)
    setLockColor(ctx, colors.LockColor)
    setOpenFileColor(ctx, colors.OpenFileColor)
    setReloadColor(ctx, colors.ReloadColor)
    setOpenCircleNest(ctx, colors.OpenCircleNestColor)
    setGoParentNest(ctx, colors.GoParentNestColor)
}
