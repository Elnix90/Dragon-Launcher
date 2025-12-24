package org.elnix.dragonlauncher.data.stores

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.BackupTypeException
import org.elnix.dragonlauncher.data.BaseSettingsStore
import org.elnix.dragonlauncher.data.uiDatastore

object UiSettingsStore : BaseSettingsStore() {
    override val name: String = "Ui"

    private data class UiSettingsBackup(
        val rgbLoading: Boolean = true,
        val rgbLine: Boolean = true,
        val showLaunchingAppLabel: Boolean = true,
        val showLaunchingAppIcon: Boolean = true,
        val showAppLaunchPreviewCircle: Boolean = true,
        val fullscreen: Boolean = true,
        val showAppCirclePreview: Boolean = true,
        val showAppLinePreview: Boolean = true,
        val showAppAnglePreview: Boolean = true,
        val snapPoints: Boolean = true,
        val firstCircleDragDistance: Int = 400,
        val secondCircleDragDistance: Int = 700,
        val cancelZoneDragDistance: Int = 150,
        val showAppPreviewIconCenterStartPosition: Boolean = false,
        val linePreviewSnapToAction: Boolean = false,
        val minAngleFromAPointToActivateIt: Int = 30,
        val showAllActionsOnCurrentCircle: Boolean = false
    )

    private val defaults = UiSettingsBackup()

    private object Keys {
        val RGB_LOADING = booleanPreferencesKey(UiSettingsBackup::rgbLoading.name)
        val RGB_LINE = booleanPreferencesKey(UiSettingsBackup::rgbLine.name)
        val SHOW_LAUNCHING_APP_LABEL = booleanPreferencesKey(UiSettingsBackup::showLaunchingAppLabel.name)
        val SHOW_LAUNCHING_APP_ICON = booleanPreferencesKey(UiSettingsBackup::showLaunchingAppIcon.name)
        val SHOW_APP_LAUNCH_PREVIEW = booleanPreferencesKey(UiSettingsBackup::showAppLaunchPreviewCircle.name)
        val FULLSCREEN = booleanPreferencesKey(UiSettingsBackup::fullscreen.name)
        val SHOW_CIRCLE_PREVIEW = booleanPreferencesKey(UiSettingsBackup::showAppCirclePreview.name)
        val SHOW_LINE_PREVIEW = booleanPreferencesKey(UiSettingsBackup::showAppLinePreview.name)
        val SHOW_ANGLE_PREVIEW = booleanPreferencesKey("show_app_angle_preview")
        val SNAP_POINTS = booleanPreferencesKey(UiSettingsBackup::snapPoints.name)
        val FIRST_CIRCLE_DRAG_DISTANCE = intPreferencesKey(UiSettingsBackup::firstCircleDragDistance.name)
        val SECOND_CIRCLE_DRAG_DISTANCE = intPreferencesKey(UiSettingsBackup::secondCircleDragDistance.name)
        val CANCEL_ZONE_DRAG_DISTANCE = intPreferencesKey(UiSettingsBackup::cancelZoneDragDistance.name)
        val SHOW_APP_PREVIEW_ICON_CENTER_START_POSITION = booleanPreferencesKey("showAppPreviewIconCenterStartPosition")
        val LINE_PREVIEW_SNAP_TO_ACTION = booleanPreferencesKey("linePreviewSnapToAction")
        val MIN_ANGLE_FROM_A_POINT_TO_ACTIVATE_IT = intPreferencesKey("minAngleFromAPointToActivateIt")
        val SHOW_ALL_ACTIONS_ON_CURRENT_CIRCLE = booleanPreferencesKey("showAllActionsOnCurrentCircle")


        val ALL = listOf(
            RGB_LOADING,
            RGB_LINE,
            SHOW_LAUNCHING_APP_LABEL,
            SHOW_LAUNCHING_APP_ICON,
            SHOW_APP_LAUNCH_PREVIEW,
            FULLSCREEN,
            SHOW_CIRCLE_PREVIEW,
            SHOW_LINE_PREVIEW,
            SHOW_ANGLE_PREVIEW,
            SNAP_POINTS,
            FIRST_CIRCLE_DRAG_DISTANCE,
            SECOND_CIRCLE_DRAG_DISTANCE,
            CANCEL_ZONE_DRAG_DISTANCE,
            SHOW_APP_PREVIEW_ICON_CENTER_START_POSITION,
            LINE_PREVIEW_SNAP_TO_ACTION,
            MIN_ANGLE_FROM_A_POINT_TO_ACTIVATE_IT,
            SHOW_ALL_ACTIONS_ON_CURRENT_CIRCLE
        )
    }

    fun getRGBLoading(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[Keys.RGB_LOADING] ?: defaults.rgbLoading }

    suspend fun setRGBLoading(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[Keys.RGB_LOADING] = value }
    }

    fun getRGBLine(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[Keys.RGB_LINE] ?: defaults.rgbLine }

    suspend fun setRGBLine(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[Keys.RGB_LINE] = value }
    }

    fun getShowLaunchingAppLabel(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[Keys.SHOW_LAUNCHING_APP_LABEL] ?: defaults.showLaunchingAppLabel }

    suspend fun setShowLaunchingAppLabel(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[Keys.SHOW_LAUNCHING_APP_LABEL] = value }
    }

    fun getShowLaunchingAppIcon(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[Keys.SHOW_LAUNCHING_APP_ICON] ?: defaults.showLaunchingAppIcon }

    suspend fun setShowLaunchingAppIcon(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[Keys.SHOW_LAUNCHING_APP_ICON] = value }
    }

    fun getShowAppLaunchPreview(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[Keys.SHOW_APP_LAUNCH_PREVIEW] ?: defaults.showAppLaunchPreviewCircle }

    suspend fun setShowAppLaunchPreview(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[Keys.SHOW_APP_LAUNCH_PREVIEW] = value }
    }

    fun getFullscreen(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[Keys.FULLSCREEN] ?: defaults.fullscreen }

    suspend fun setFullscreen(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[Keys.FULLSCREEN] = value }
    }

    fun getShowCirclePreview(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[Keys.SHOW_CIRCLE_PREVIEW] ?: defaults.showAppCirclePreview }

    suspend fun setShowCirclePreview(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[Keys.SHOW_CIRCLE_PREVIEW] = value }
    }

    fun getShowLinePreview(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[Keys.SHOW_LINE_PREVIEW] ?: defaults.showAppLinePreview }

    suspend fun setShowLinePreview(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[Keys.SHOW_LINE_PREVIEW] = value }
    }

    fun getShowAnglePreview(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[Keys.SHOW_ANGLE_PREVIEW] ?: defaults.showAppAnglePreview }

    suspend fun setShowAnglePreview(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[Keys.SHOW_ANGLE_PREVIEW] = value }
    }

    fun getSnapPoints(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[Keys.SNAP_POINTS] ?: defaults.snapPoints }

    suspend fun setSnapPoints(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[Keys.SNAP_POINTS] = value }
    }


    fun getFirstCircleDragDistance(ctx: Context): Flow<Int> =
        ctx.uiDatastore.data.map { it[Keys.FIRST_CIRCLE_DRAG_DISTANCE] ?: defaults.firstCircleDragDistance }

    suspend fun setFirstCircleDragDistance(ctx: Context, value: Int) {
        ctx.uiDatastore.edit { it[Keys.FIRST_CIRCLE_DRAG_DISTANCE] = value }
    }

    fun getSecondCircleDragDistance(ctx: Context): Flow<Int> =
        ctx.uiDatastore.data.map { it[Keys.SECOND_CIRCLE_DRAG_DISTANCE] ?: defaults.secondCircleDragDistance }

    suspend fun setSecondCircleDragDistance(ctx: Context, value: Int) {
        ctx.uiDatastore.edit { it[Keys.SECOND_CIRCLE_DRAG_DISTANCE] = value }
    }

    fun getCancelZoneDragDistance(ctx: Context): Flow<Int> =
        ctx.uiDatastore.data.map { it[Keys.CANCEL_ZONE_DRAG_DISTANCE] ?: defaults.cancelZoneDragDistance }

    suspend fun setCancelZoneDragDistance(ctx: Context, value: Int) {
        ctx.uiDatastore.edit { it[Keys.CANCEL_ZONE_DRAG_DISTANCE] = value }
    }

    fun getShowAppPreviewIconCenterStartPosition(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[Keys.SHOW_APP_PREVIEW_ICON_CENTER_START_POSITION] ?: defaults.showAppPreviewIconCenterStartPosition }

    suspend fun setShowAppPreviewIconCenterStartPosition(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[Keys.SHOW_APP_PREVIEW_ICON_CENTER_START_POSITION] = value }
    }

    fun getLinePreviewSnapToAction(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[Keys.LINE_PREVIEW_SNAP_TO_ACTION] ?: defaults.linePreviewSnapToAction }

    suspend fun setLinePreviewSnapToAction(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[Keys.LINE_PREVIEW_SNAP_TO_ACTION] = value }
    }

    fun getMinAngleFromAPointToActivateIt(ctx: Context): Flow<Int> =
        ctx.uiDatastore.data.map { it[Keys.MIN_ANGLE_FROM_A_POINT_TO_ACTIVATE_IT] ?: defaults.minAngleFromAPointToActivateIt }

    suspend fun setMinAngleFromAPointToActivateIt(ctx: Context, value: Int) {
        ctx.uiDatastore.edit { it[Keys.MIN_ANGLE_FROM_A_POINT_TO_ACTIVATE_IT] = value }
    }

    fun getShowAllActionsOnCurrentCircle(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[Keys.SHOW_ALL_ACTIONS_ON_CURRENT_CIRCLE] ?: defaults.showAllActionsOnCurrentCircle }

    suspend fun setShowAllActionsOnCurrentCircle(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[Keys.SHOW_ALL_ACTIONS_ON_CURRENT_CIRCLE] = value }
    }

    // --------------------------------
    // BACKUP / RESTORE / RESET
    // --------------------------------


    override suspend fun resetAll(ctx: Context) {
        ctx.uiDatastore.edit { prefs ->
            Keys.ALL.forEach { prefs.remove(it) }
        }
    }

    suspend fun getAll(ctx: Context): Map<String, Any> {
        val prefs = ctx.uiDatastore.data.first()

        return buildMap {

            fun putIfChanged(key: Preferences.Key<Boolean>, default: Boolean) {
                val v = prefs[key]
                if (v != null && v != default) put(key.name, v)
            }

            fun putIfChanged(key: Preferences.Key<Int>, default: Int) {
                val v = prefs[key]
                if (v != null && v != default) put(key.name, v)
            }

            putIfChanged(Keys.RGB_LOADING, defaults.rgbLoading)
            putIfChanged(Keys.RGB_LINE, defaults.rgbLine)
            putIfChanged(Keys.SHOW_LAUNCHING_APP_LABEL, defaults.showLaunchingAppLabel)
            putIfChanged(Keys.SHOW_LAUNCHING_APP_ICON, defaults.showLaunchingAppIcon)
            putIfChanged(Keys.SHOW_APP_LAUNCH_PREVIEW, defaults.showAppLaunchPreviewCircle)
            putIfChanged(Keys.FULLSCREEN, defaults.fullscreen)
            putIfChanged(Keys.SHOW_CIRCLE_PREVIEW, defaults.showAppCirclePreview)
            putIfChanged(Keys.SHOW_LINE_PREVIEW, defaults.showAppLinePreview)
            putIfChanged(Keys.SHOW_ANGLE_PREVIEW, defaults.showAppAnglePreview)
            putIfChanged(Keys.SNAP_POINTS, defaults.snapPoints)
            putIfChanged(Keys.SHOW_APP_PREVIEW_ICON_CENTER_START_POSITION, defaults.showAppPreviewIconCenterStartPosition)
            putIfChanged(Keys.LINE_PREVIEW_SNAP_TO_ACTION, defaults.linePreviewSnapToAction)
            putIfChanged(Keys.SHOW_ALL_ACTIONS_ON_CURRENT_CIRCLE, defaults.showAllActionsOnCurrentCircle)

            putIfChanged(Keys.FIRST_CIRCLE_DRAG_DISTANCE, defaults.firstCircleDragDistance)
            putIfChanged(Keys.SECOND_CIRCLE_DRAG_DISTANCE, defaults.secondCircleDragDistance)
            putIfChanged(Keys.CANCEL_ZONE_DRAG_DISTANCE, defaults.cancelZoneDragDistance)
            putIfChanged(Keys.MIN_ANGLE_FROM_A_POINT_TO_ACTIVATE_IT, defaults.minAngleFromAPointToActivateIt)
        }
    }


    suspend fun setAll(ctx: Context, backup: Map<String, Any?>) {
        ctx.uiDatastore.edit { prefs ->

            fun applyBoolean(key: Preferences.Key<Boolean>) {
                val raw = backup[key.name] ?: return

                val boolValue = when (raw) {
                    is Boolean -> raw
                    is String -> raw.toBooleanStrictOrNull()
                        ?: throw BackupTypeException(
                            key.name,
                            expected = "Boolean",
                            actual = "String",
                            value = raw
                        )
                    else -> throw BackupTypeException(
                        key.name,
                        expected = "Boolean",
                        actual = raw::class.simpleName,
                        value = raw
                    )
                }

                prefs[key] = boolValue
            }

            fun applyInt(key: Preferences.Key<Int>) {
                val raw = backup[key.name] ?: return

                val intValue = when (raw) {
                    is Int -> raw
                    is String -> raw.toIntOrNull()
                        ?: throw BackupTypeException(
                            key.name,
                            expected = "Int",
                            actual = "String",
                            value = raw
                        )
                    else -> throw BackupTypeException(
                        key.name,
                        expected = "Int",
                        actual = raw::class.simpleName,
                        value = raw
                    )
                }

                prefs[key] = intValue
            }

            applyBoolean(Keys.RGB_LOADING)
            applyBoolean(Keys.RGB_LINE)
            applyBoolean(Keys.SHOW_LAUNCHING_APP_LABEL)
            applyBoolean(Keys.SHOW_LAUNCHING_APP_ICON)
            applyBoolean(Keys.SHOW_APP_LAUNCH_PREVIEW)
            applyBoolean(Keys.FULLSCREEN)
            applyBoolean(Keys.SHOW_CIRCLE_PREVIEW)
            applyBoolean(Keys.SHOW_LINE_PREVIEW)
            applyBoolean(Keys.SHOW_ANGLE_PREVIEW)
            applyBoolean(Keys.SNAP_POINTS)
            applyBoolean(Keys.SHOW_APP_PREVIEW_ICON_CENTER_START_POSITION)
            applyBoolean(Keys.LINE_PREVIEW_SNAP_TO_ACTION)
            applyBoolean(Keys.SHOW_ALL_ACTIONS_ON_CURRENT_CIRCLE)

            applyInt(Keys.FIRST_CIRCLE_DRAG_DISTANCE)
            applyInt(Keys.SECOND_CIRCLE_DRAG_DISTANCE)
            applyInt(Keys.CANCEL_ZONE_DRAG_DISTANCE)
            applyInt(Keys.MIN_ANGLE_FROM_A_POINT_TO_ACTIVATE_IT)
        }
    }
}
