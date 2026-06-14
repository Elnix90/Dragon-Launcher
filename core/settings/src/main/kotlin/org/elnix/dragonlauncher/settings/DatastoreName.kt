package org.elnix.dragonlauncher.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.elnix.dragonlauncher.settings.DataStoreName.ANGLE_LINE
import org.elnix.dragonlauncher.settings.DataStoreName.APP_OVERRIDES
import org.elnix.dragonlauncher.settings.DataStoreName.BACKUP
import org.elnix.dragonlauncher.settings.DataStoreName.BEHAVIOR
import org.elnix.dragonlauncher.settings.DataStoreName.COLOR
import org.elnix.dragonlauncher.settings.DataStoreName.COLOR_MODE
import org.elnix.dragonlauncher.settings.DataStoreName.DEBUG
import org.elnix.dragonlauncher.settings.DataStoreName.DRAWER
import org.elnix.dragonlauncher.settings.DataStoreName.HOLD_TO_ACTIVATE
import org.elnix.dragonlauncher.settings.DataStoreName.ICONS
import org.elnix.dragonlauncher.settings.DataStoreName.LANGUAGE
import org.elnix.dragonlauncher.settings.DataStoreName.NESTS
import org.elnix.dragonlauncher.settings.DataStoreName.POINTS
import org.elnix.dragonlauncher.settings.DataStoreName.PRIVATE_SETTINGS
import org.elnix.dragonlauncher.settings.DataStoreName.STATUS_BAR
import org.elnix.dragonlauncher.settings.DataStoreName.STATUS_BAR_JSON
import org.elnix.dragonlauncher.settings.DataStoreName.SWIPE_MAP
import org.elnix.dragonlauncher.settings.DataStoreName.UI
import org.elnix.dragonlauncher.settings.DataStoreName.WELLBEING
import org.elnix.dragonlauncher.settings.DataStoreName.WIDGETS
import org.elnix.dragonlauncher.settings.DataStoreName.WORKSPACES
import org.elnix.dragonlauncher.settings.bases.stores.BaseSettingsStore
import org.elnix.dragonlauncher.settings.stores.array.NestsSettingsStore
import org.elnix.dragonlauncher.settings.stores.array.PointsSettingsStore
import org.elnix.dragonlauncher.settings.stores.array.StatusBarJsonSettingsStore
import org.elnix.dragonlauncher.settings.stores.array.WidgetsSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.AngleLineSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.BackupSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.ColorModesSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.ColorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.HoldToActivateArcSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.IconsSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.LanguageSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.SwipeMapSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.WellbeingSettingsStore
import org.elnix.dragonlauncher.settings.stores.`object`.AppOverridesSettingsStore
import org.elnix.dragonlauncher.settings.stores.`object`.WorkspaceSettingsStore


enum class DataStoreName(
    val value: String,
    val backupKey: String,
    val userBackup: Boolean = true
) {
    UI("uiDatastore", "ui"),
    ICONS("iconsDatastore", "icons"),
    COLOR_MODE("colorModeDatastore", "color_mode"),
    COLOR("colorDatastore", "color"),
    PRIVATE_SETTINGS("privateSettingsStore", "private", false),
    POINTS("pointsDatastore", "points"),
    NESTS("nestsDatastore", "nests"),
    LANGUAGE("languageDatastore", "language"),
    DRAWER("drawerDatastore", "drawer"),
    DEBUG("debugDatastore", "debug"),
    APP_OVERRIDES("AppOverridesDatastore", "app_overrides"),
    WORKSPACES("workspacesDataStore", "workspaces"),
    BEHAVIOR("behaviorDatastore", "behavior"),
    BACKUP("backupDatastore", "backup"),
    STATUS_BAR("statusDatastore", "status_bar"),
    WIDGETS("widgetsDatastore", "widgets"),
    WELLBEING("wellbeingDatastore", "wellbeing"),
    SWIPE_MAP("swipeMapDataStore", "swipe_map"),
    STATUS_BAR_JSON("statusBarJsonDataStore", "status_bar_json"),
    ANGLE_LINE("AngleLineDatastore", "angle_line"),
    HOLD_TO_ACTIVATE("HoldTOActivateDatastore", "hold_to_activate")
}


private val dataStoreDelegates = DataStoreName.entries.associate { dataStoreName ->
    dataStoreName.value to preferencesDataStore(name = dataStoreName.value)
}

internal fun Context.resolveDataStore(dataStoreName: DataStoreName): DataStore<Preferences> {
    return dataStoreDelegates[dataStoreName.value]?.getValue(this, ::dataStoreDelegates)
        ?: error("No DataStore delegate found for ${dataStoreName.value}")
}

val allStores: Map<DataStoreName, BaseSettingsStore<*, *>> by lazy {
    DataStoreName.entries.associateWith {
        when (it) {
            UI -> UiSettingsStore
            ICONS -> IconsSettingsStore
            COLOR_MODE -> ColorModesSettingsStore
            COLOR -> ColorSettingsStore
            PRIVATE_SETTINGS -> PrivateSettingsStore
            POINTS -> PointsSettingsStore
            NESTS -> NestsSettingsStore
            LANGUAGE -> LanguageSettingsStore
            DRAWER -> DrawerSettingsStore
            DEBUG -> DebugSettingsStore
            APP_OVERRIDES -> AppOverridesSettingsStore
            WORKSPACES -> WorkspaceSettingsStore
            BEHAVIOR -> BehaviorSettingsStore
            BACKUP -> BackupSettingsStore
            STATUS_BAR -> StatusBarJsonSettingsStore
            WIDGETS -> WidgetsSettingsStore
            WELLBEING -> WellbeingSettingsStore
            SWIPE_MAP -> SwipeMapSettingsStore
            STATUS_BAR_JSON -> StatusBarJsonSettingsStore
            ANGLE_LINE -> AngleLineSettingsStore
            HOLD_TO_ACTIVATE -> HoldToActivateArcSettingsStore
        }
    }
}


val themeDataStores: Set<DataStoreName> = setOf(UI, COLOR_MODE, COLOR, ANGLE_LINE, HOLD_TO_ACTIVATE)

val backupableStores: Map<DataStoreName, BaseSettingsStore<*, *>> by lazy {
    allStores.filterKeys { it.userBackup }
}


suspend fun clearAllData(ctx: Context) = coroutineScope {
    DataStoreName.entries.map { dataStoreName ->
        async { ctx.resolveDataStore(dataStoreName).edit { it.clear() } }
    }.awaitAll()
}