package org.elnix.dragonlauncher.data.stores

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.BaseSettingsStore
import org.elnix.dragonlauncher.data.appDrawerDataStore
import org.elnix.dragonlauncher.data.behaviorDataStore
import org.elnix.dragonlauncher.data.getBooleanStrict
import org.elnix.dragonlauncher.data.getIntStrict
import org.elnix.dragonlauncher.data.getStringStrict
import org.elnix.dragonlauncher.data.putIfNonDefault
import org.elnix.dragonlauncher.data.stores.UiSettingsStore.Keys.APP_LABEL_ICON_OVERLAY_TOP_PADDING
import org.elnix.dragonlauncher.data.stores.UiSettingsStore.Keys.AUTO_SEPARATE_POINTS
import org.elnix.dragonlauncher.data.stores.UiSettingsStore.Keys.FULLSCREEN
import org.elnix.dragonlauncher.data.stores.UiSettingsStore.Keys.ICON_PACK_KEY
import org.elnix.dragonlauncher.data.stores.UiSettingsStore.Keys.LINE_PREVIEW_SNAP_TO_ACTION
import org.elnix.dragonlauncher.data.stores.UiSettingsStore.Keys.MIN_ANGLE_FROM_A_POINT_TO_ACTIVATE_IT
import org.elnix.dragonlauncher.data.stores.UiSettingsStore.Keys.RGB_LINE
import org.elnix.dragonlauncher.data.stores.UiSettingsStore.Keys.RGB_LOADING
import org.elnix.dragonlauncher.data.stores.UiSettingsStore.Keys.SHOW_ACTION_ICON_BORDER
import org.elnix.dragonlauncher.data.stores.UiSettingsStore.Keys.SHOW_ALL_ACTIONS_ON_CURRENT_CIRCLE
import org.elnix.dragonlauncher.data.stores.UiSettingsStore.Keys.SHOW_ANGLE_PREVIEW
import org.elnix.dragonlauncher.data.stores.UiSettingsStore.Keys.SHOW_APP_LAUNCH_PREVIEW
import org.elnix.dragonlauncher.data.stores.UiSettingsStore.Keys.SHOW_APP_PREVIEW_ICON_CENTER_START_POSITION
import org.elnix.dragonlauncher.data.stores.UiSettingsStore.Keys.SHOW_CIRCLE_PREVIEW
import org.elnix.dragonlauncher.data.stores.UiSettingsStore.Keys.SHOW_LAUNCHING_APP_ICON
import org.elnix.dragonlauncher.data.stores.UiSettingsStore.Keys.SHOW_LAUNCHING_APP_LABEL
import org.elnix.dragonlauncher.data.stores.UiSettingsStore.Keys.SHOW_LINE_PREVIEW
import org.elnix.dragonlauncher.data.stores.UiSettingsStore.Keys.SNAP_POINTS
import org.elnix.dragonlauncher.data.uiDatastore

object UiSettingsStore : BaseSettingsStore<Map<String, Any?>>() {
    override val name: String = "Ui"

    private data class UiSettingsBackup(
        val rgbLoading: Boolean = true,
        val rgbLine: Boolean = true,
        val showLaunchingAppLabel: Boolean = true,
        val showLaunchingAppIcon: Boolean = true,
        val showAppLaunchPreview: Boolean = true,
        val fullscreen: Boolean = true,
        val showCirclePreview: Boolean = true,
        val showLinePreview: Boolean = true,
        val showAnglePreview: Boolean = true,
        val snapPoints: Boolean = true,
        val autoSeparatePoints: Boolean = true,
        val showAppPreviewIconCenterStartPosition: Boolean = false,
        val linePreviewSnapToAction: Boolean = false,
        val minAngleFromAPointToActivateIt: Int = 30,
        val showAllActionsOnCurrentCircle: Boolean = false,
        val iconPackKey: String? = null,
        val showActionIconBorder: Boolean = true,
        val appLabelIconOverlayTopPadding: Int = 30
    )

    private val defaults = UiSettingsBackup()

    private object Keys {
        val RGB_LOADING = booleanPreferencesKey(UiSettingsBackup::rgbLoading.name)
        val RGB_LINE = booleanPreferencesKey(UiSettingsBackup::rgbLine.name)
        val SHOW_LAUNCHING_APP_LABEL = booleanPreferencesKey(UiSettingsBackup::showLaunchingAppLabel.name)
        val SHOW_LAUNCHING_APP_ICON = booleanPreferencesKey(UiSettingsBackup::showLaunchingAppIcon.name)
        val SHOW_APP_LAUNCH_PREVIEW = booleanPreferencesKey(UiSettingsBackup::showAppLaunchPreview.name)
        val FULLSCREEN = booleanPreferencesKey(UiSettingsBackup::fullscreen.name)
        val SHOW_CIRCLE_PREVIEW = booleanPreferencesKey(UiSettingsBackup::showCirclePreview.name)
        val SHOW_LINE_PREVIEW = booleanPreferencesKey(UiSettingsBackup::showLinePreview.name)
        val SHOW_ANGLE_PREVIEW = booleanPreferencesKey("show_app_angle_preview")
        val SNAP_POINTS = booleanPreferencesKey(UiSettingsBackup::snapPoints.name)
        val AUTO_SEPARATE_POINTS = booleanPreferencesKey(UiSettingsBackup::autoSeparatePoints.name)
        val SHOW_APP_PREVIEW_ICON_CENTER_START_POSITION = booleanPreferencesKey("showAppPreviewIconCenterStartPosition")
        val LINE_PREVIEW_SNAP_TO_ACTION = booleanPreferencesKey("linePreviewSnapToAction")
        val MIN_ANGLE_FROM_A_POINT_TO_ACTIVATE_IT = intPreferencesKey("minAngleFromAPointToActivateIt")
        val SHOW_ALL_ACTIONS_ON_CURRENT_CIRCLE = booleanPreferencesKey("showAllActionsOnCurrentCircle")
        val ICON_PACK_KEY = stringPreferencesKey("selected_icon_pack")
        val SHOW_ACTION_ICON_BORDER = booleanPreferencesKey("showActionIconBorder")

        val APP_LABEL_ICON_OVERLAY_TOP_PADDING =
            intPreferencesKey("appLabelIconOverlayTopPadding")

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
            AUTO_SEPARATE_POINTS,
            SHOW_APP_PREVIEW_ICON_CENTER_START_POSITION,
            LINE_PREVIEW_SNAP_TO_ACTION,
            MIN_ANGLE_FROM_A_POINT_TO_ACTIVATE_IT,
            SHOW_ALL_ACTIONS_ON_CURRENT_CIRCLE,
            ICON_PACK_KEY,
            SHOW_ACTION_ICON_BORDER,
            APP_LABEL_ICON_OVERLAY_TOP_PADDING
        )
    }

    fun getRGBLoading(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[RGB_LOADING] ?: defaults.rgbLoading }

    suspend fun setRGBLoading(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[RGB_LOADING] = value }
    }

    fun getRGBLine(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[RGB_LINE] ?: defaults.rgbLine }

    suspend fun setRGBLine(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[RGB_LINE] = value }
    }

    fun getShowLaunchingAppLabel(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[SHOW_LAUNCHING_APP_LABEL] ?: defaults.showLaunchingAppLabel }

    suspend fun setShowLaunchingAppLabel(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[SHOW_LAUNCHING_APP_LABEL] = value }
    }

    fun getShowLaunchingAppIcon(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[SHOW_LAUNCHING_APP_ICON] ?: defaults.showLaunchingAppIcon }

    suspend fun setShowLaunchingAppIcon(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[SHOW_LAUNCHING_APP_ICON] = value }
    }

    fun getShowAppLaunchPreview(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[SHOW_APP_LAUNCH_PREVIEW] ?: defaults.showAppLaunchPreview }

    suspend fun setShowAppLaunchPreview(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[SHOW_APP_LAUNCH_PREVIEW] = value }
    }

    fun getFullscreen(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[FULLSCREEN] ?: defaults.fullscreen }

    suspend fun setFullscreen(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[FULLSCREEN] = value }
    }

    fun getShowCirclePreview(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[SHOW_CIRCLE_PREVIEW] ?: defaults.showCirclePreview }

    suspend fun setShowCirclePreview(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[SHOW_CIRCLE_PREVIEW] = value }
    }

    fun getShowLinePreview(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[SHOW_LINE_PREVIEW] ?: defaults.showLinePreview }

    suspend fun setShowLinePreview(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[SHOW_LINE_PREVIEW] = value }
    }

    fun getShowAnglePreview(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[SHOW_ANGLE_PREVIEW] ?: defaults.showAnglePreview }

    suspend fun setShowAnglePreview(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[SHOW_ANGLE_PREVIEW] = value }
    }

    fun getSnapPoints(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[SNAP_POINTS] ?: defaults.snapPoints }

    suspend fun setSnapPoints(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[SNAP_POINTS] = value }
    }

    fun getAutoSeparatePoints(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[AUTO_SEPARATE_POINTS] ?: defaults.autoSeparatePoints }

    suspend fun setAutoSeparatePoints(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[AUTO_SEPARATE_POINTS] = value }
    }

    fun getShowAppPreviewIconCenterStartPosition(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[SHOW_APP_PREVIEW_ICON_CENTER_START_POSITION] ?: defaults.showAppPreviewIconCenterStartPosition }

    suspend fun setShowAppPreviewIconCenterStartPosition(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[SHOW_APP_PREVIEW_ICON_CENTER_START_POSITION] = value }
    }

    fun getLinePreviewSnapToAction(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[LINE_PREVIEW_SNAP_TO_ACTION] ?: defaults.linePreviewSnapToAction }

    suspend fun setLinePreviewSnapToAction(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[LINE_PREVIEW_SNAP_TO_ACTION] = value }
    }

    fun getMinAngleFromAPointToActivateIt(ctx: Context): Flow<Int> =
        ctx.uiDatastore.data.map { it[MIN_ANGLE_FROM_A_POINT_TO_ACTIVATE_IT] ?: defaults.minAngleFromAPointToActivateIt }

    suspend fun setMinAngleFromAPointToActivateIt(ctx: Context, value: Int) {
        ctx.uiDatastore.edit { it[MIN_ANGLE_FROM_A_POINT_TO_ACTIVATE_IT] = value }
    }

    fun getShowAllActionsOnCurrentCircle(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[SHOW_ALL_ACTIONS_ON_CURRENT_CIRCLE] ?: defaults.showAllActionsOnCurrentCircle }

    suspend fun setShowAllActionsOnCurrentCircle(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[SHOW_ALL_ACTIONS_ON_CURRENT_CIRCLE] = value }
    }

    fun getShowActionIconBorder(ctx: Context): Flow<Boolean> =
        ctx.uiDatastore.data.map { it[SHOW_ACTION_ICON_BORDER] ?: defaults.showActionIconBorder }

    suspend fun setShowActionIconBorder(ctx: Context, value: Boolean) {
        ctx.uiDatastore.edit { it[SHOW_ACTION_ICON_BORDER] = value }
    }

    suspend fun getIconPack(ctx: Context): String? {
        return ctx.appDrawerDataStore.data
            .map { it[ICON_PACK_KEY] }
            .firstOrNull()
    }

    suspend fun setIconPack(ctx: Context, packName: String?) {
        ctx.appDrawerDataStore.edit { prefs ->
            if (packName == null) {
                prefs.remove(ICON_PACK_KEY)
            } else {
                prefs[ICON_PACK_KEY] = packName
            }
        }
    }

    suspend fun setAppLabelIconOverlayTopPadding(ctx: Context, value: Int) {
        ctx.behaviorDataStore.edit { it[APP_LABEL_ICON_OVERLAY_TOP_PADDING] = value }
    }

    fun getAppLabelIconOverlayTopPadding(ctx: Context): Flow<Int> =
        ctx.behaviorDataStore.data.map { it[APP_LABEL_ICON_OVERLAY_TOP_PADDING] ?: defaults.appLabelIconOverlayTopPadding }



    // --------------------------------
    // BACKUP / RESTORE / RESET
    // --------------------------------


    override suspend fun resetAll(ctx: Context) {
        ctx.uiDatastore.edit { prefs ->
            Keys.ALL.forEach { prefs.remove(it) }
        }
    }

    override suspend fun getAll(ctx: Context): Map<String, Any> {
        val prefs = ctx.uiDatastore.data.first()

        return buildMap {

            putIfNonDefault(
                RGB_LOADING,
                prefs[RGB_LOADING],
                defaults.rgbLoading
            )

            putIfNonDefault(
                RGB_LINE,
                prefs[RGB_LINE],
                defaults.rgbLine
            )

            putIfNonDefault(
                SHOW_LAUNCHING_APP_LABEL,
                prefs[SHOW_LAUNCHING_APP_LABEL],
                defaults.showLaunchingAppLabel
            )

            putIfNonDefault(
                SHOW_LAUNCHING_APP_ICON,
                prefs[SHOW_LAUNCHING_APP_ICON],
                defaults.showLaunchingAppIcon
            )

            putIfNonDefault(
                SHOW_APP_LAUNCH_PREVIEW,
                prefs[SHOW_APP_LAUNCH_PREVIEW],
                defaults.showAppLaunchPreview
            )

            putIfNonDefault(
                FULLSCREEN,
                prefs[FULLSCREEN],
                defaults.fullscreen
            )

            putIfNonDefault(
                SHOW_CIRCLE_PREVIEW,
                prefs[SHOW_CIRCLE_PREVIEW],
                defaults.showCirclePreview
            )

            putIfNonDefault(
                SHOW_LINE_PREVIEW,
                prefs[SHOW_LINE_PREVIEW],
                defaults.showLinePreview
            )

            putIfNonDefault(
                SHOW_ANGLE_PREVIEW,
                prefs[SHOW_ANGLE_PREVIEW],
                defaults.showAnglePreview
            )

            putIfNonDefault(
                SNAP_POINTS,
                prefs[SNAP_POINTS],
                defaults.snapPoints
            )

            putIfNonDefault(
                AUTO_SEPARATE_POINTS,
                prefs[AUTO_SEPARATE_POINTS],
                defaults.autoSeparatePoints
            )

            putIfNonDefault(
                SHOW_APP_PREVIEW_ICON_CENTER_START_POSITION,
                prefs[SHOW_APP_PREVIEW_ICON_CENTER_START_POSITION],
                defaults.showAppPreviewIconCenterStartPosition
            )

            putIfNonDefault(
                LINE_PREVIEW_SNAP_TO_ACTION,
                prefs[LINE_PREVIEW_SNAP_TO_ACTION],
                defaults.linePreviewSnapToAction
            )

            putIfNonDefault(
                SHOW_ALL_ACTIONS_ON_CURRENT_CIRCLE,
                prefs[SHOW_ALL_ACTIONS_ON_CURRENT_CIRCLE],
                defaults.showAllActionsOnCurrentCircle
            )

            putIfNonDefault(
                MIN_ANGLE_FROM_A_POINT_TO_ACTIVATE_IT,
                prefs[MIN_ANGLE_FROM_A_POINT_TO_ACTIVATE_IT],
                defaults.minAngleFromAPointToActivateIt
            )

            putIfNonDefault(
                ICON_PACK_KEY,
                prefs[ICON_PACK_KEY],
                ""
            )
            putIfNonDefault(
                SHOW_ACTION_ICON_BORDER,
                prefs[SHOW_ACTION_ICON_BORDER],
                defaults.showActionIconBorder
            )

            putIfNonDefault(
                APP_LABEL_ICON_OVERLAY_TOP_PADDING,
                prefs[APP_LABEL_ICON_OVERLAY_TOP_PADDING],
                defaults.appLabelIconOverlayTopPadding
            )
        }
    }



    override suspend fun setAll(ctx: Context, value: Map<String, Any?>) {
        ctx.uiDatastore.edit { prefs ->

            prefs[RGB_LOADING] =
                getBooleanStrict(value, RGB_LOADING, defaults.rgbLoading)

            prefs[RGB_LINE] =
                getBooleanStrict(value, RGB_LINE, defaults.rgbLine)

            prefs[SHOW_LAUNCHING_APP_LABEL] =
                getBooleanStrict(value, SHOW_LAUNCHING_APP_LABEL, defaults.showLaunchingAppLabel)

            prefs[SHOW_LAUNCHING_APP_ICON] =
                getBooleanStrict(value, SHOW_LAUNCHING_APP_ICON, defaults.showLaunchingAppIcon)

            prefs[SHOW_APP_LAUNCH_PREVIEW] =
                getBooleanStrict(value, SHOW_APP_LAUNCH_PREVIEW, defaults.showAppLaunchPreview)

            prefs[FULLSCREEN] =
                getBooleanStrict(value, FULLSCREEN, defaults.fullscreen)

            prefs[SHOW_CIRCLE_PREVIEW] =
                getBooleanStrict(value, SHOW_CIRCLE_PREVIEW, defaults.showCirclePreview)

            prefs[SHOW_LINE_PREVIEW] =
                getBooleanStrict(value, SHOW_LINE_PREVIEW, defaults.showLinePreview)

            prefs[SHOW_ANGLE_PREVIEW] =
                getBooleanStrict(value, SHOW_ANGLE_PREVIEW, defaults.showAnglePreview)

            prefs[SNAP_POINTS] =
                getBooleanStrict(value, SNAP_POINTS, defaults.snapPoints)

            prefs[AUTO_SEPARATE_POINTS] =
                getBooleanStrict(value, AUTO_SEPARATE_POINTS, defaults.autoSeparatePoints)

            prefs[SHOW_APP_PREVIEW_ICON_CENTER_START_POSITION] =
                getBooleanStrict(
                    value,
                    SHOW_APP_PREVIEW_ICON_CENTER_START_POSITION,
                    defaults.showAppPreviewIconCenterStartPosition
                )

            prefs[SHOW_ACTION_ICON_BORDER] =
                getBooleanStrict(
                    value,
                    SHOW_ACTION_ICON_BORDER,
                    defaults.showActionIconBorder
                )

            prefs[LINE_PREVIEW_SNAP_TO_ACTION] =
                getBooleanStrict(
                    value,
                    LINE_PREVIEW_SNAP_TO_ACTION,
                    defaults.linePreviewSnapToAction
                )

            prefs[SHOW_ALL_ACTIONS_ON_CURRENT_CIRCLE] =
                getBooleanStrict(
                    value,
                    SHOW_ALL_ACTIONS_ON_CURRENT_CIRCLE,
                    defaults.showAllActionsOnCurrentCircle
                )

            prefs[ICON_PACK_KEY] =
                getStringStrict(value, ICON_PACK_KEY, "")


            prefs[MIN_ANGLE_FROM_A_POINT_TO_ACTIVATE_IT] =
                getIntStrict(
                    value,
                    MIN_ANGLE_FROM_A_POINT_TO_ACTIVATE_IT,
                    defaults.minAngleFromAPointToActivateIt
                )

            prefs[APP_LABEL_ICON_OVERLAY_TOP_PADDING] =
                getIntStrict(
                    value,
                    APP_LABEL_ICON_OVERLAY_TOP_PADDING,
                    defaults.appLabelIconOverlayTopPadding
                )
        }
    }
}
