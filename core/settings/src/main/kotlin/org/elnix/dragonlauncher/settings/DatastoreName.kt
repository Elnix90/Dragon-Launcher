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
import org.elnix.dragonlauncher.settings.DataStoreName.PRIVATE_APPS
import org.elnix.dragonlauncher.settings.DataStoreName.PRIVATE_SETTINGS
import org.elnix.dragonlauncher.settings.DataStoreName.STATUS_BAR
import org.elnix.dragonlauncher.settings.DataStoreName.STATUS_BAR_JSON
import org.elnix.dragonlauncher.settings.DataStoreName.SWIPE
import org.elnix.dragonlauncher.settings.DataStoreName.SWIPE_MAP
import org.elnix.dragonlauncher.settings.DataStoreName.UI
import org.elnix.dragonlauncher.settings.DataStoreName.WELLBEING
import org.elnix.dragonlauncher.settings.DataStoreName.WIDGETS
import org.elnix.dragonlauncher.settings.DataStoreName.WORKSPACES
import org.elnix.dragonlauncher.settings.bases.stores.BaseSettingsStore
import org.elnix.dragonlauncher.settings.stores.AngleLineSettingsStore
import org.elnix.dragonlauncher.settings.stores.AppOverridesSettingsStore
import org.elnix.dragonlauncher.settings.stores.BackupSettingsStore
import org.elnix.dragonlauncher.settings.stores.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.ColorModesSettingsStore
import org.elnix.dragonlauncher.settings.stores.ColorSettingsStore
import org.elnix.dragonlauncher.settings.stores.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.settings.stores.HoldToActivateArcSettingsStore
import org.elnix.dragonlauncher.settings.stores.IconsSettingsStore
import org.elnix.dragonlauncher.settings.stores.LanguageSettingsStore
import org.elnix.dragonlauncher.settings.stores.PrivateAppsSettingsStore
import org.elnix.dragonlauncher.settings.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.settings.stores.StatusBarJsonSettingsStore
import org.elnix.dragonlauncher.settings.stores.StatusBarSettingsStore
import org.elnix.dragonlauncher.settings.stores.SwipeMapSettingsStore
import org.elnix.dragonlauncher.settings.stores.SwipeSettingsStore
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore
import org.elnix.dragonlauncher.settings.stores.WellbeingSettingsStore
import org.elnix.dragonlauncher.settings.stores.WidgetsSettingsStore
import org.elnix.dragonlauncher.settings.stores.WorkspaceSettingsStore


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
    SWIPE("swipePointsDatastore", "new_actions"),
    LANGUAGE("languageDatastore", "language"),
    DRAWER("drawerDatastore", "drawer"),
    DEBUG("debugDatastore", "debug"),
    APP_OVERRIDES("AppOverridesDatastore", "app_overrides"),
    WORKSPACES("workspacesDataStore", "workspaces"),
    PRIVATE_APPS("privateAppsDatastore", "private_apps", false),
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

val allStores: Map<DataStoreName, BaseSettingsStore<*, *>>
    get() = mapOf(
        UI to UiSettingsStore,
        ICONS to IconsSettingsStore,
        COLOR_MODE to ColorModesSettingsStore,
        COLOR to ColorSettingsStore,
        PRIVATE_SETTINGS to PrivateSettingsStore,
        SWIPE to SwipeSettingsStore,
        LANGUAGE to LanguageSettingsStore,
        DRAWER to DrawerSettingsStore,
        DEBUG to DebugSettingsStore,
        WORKSPACES to WorkspaceSettingsStore,
        APP_OVERRIDES to AppOverridesSettingsStore,
        PRIVATE_APPS to PrivateAppsSettingsStore,
        BEHAVIOR to BehaviorSettingsStore,
        BACKUP to BackupSettingsStore,
        STATUS_BAR to StatusBarSettingsStore,
        WIDGETS to WidgetsSettingsStore,
        WELLBEING to WellbeingSettingsStore,
        SWIPE_MAP to SwipeMapSettingsStore,
        STATUS_BAR_JSON to StatusBarJsonSettingsStore,
        ANGLE_LINE to AngleLineSettingsStore,
        HOLD_TO_ACTIVATE to HoldToActivateArcSettingsStore
    )

val themeDataStores: Set<DataStoreName>
    get() = setOf(UI, COLOR_MODE, COLOR, ANGLE_LINE, HOLD_TO_ACTIVATE)

val backupableStores: Map<DataStoreName, BaseSettingsStore<*, *>>
    get() = allStores.filterKeys { it.userBackup }


suspend fun clearAllData(ctx: Context) = coroutineScope {
    DataStoreName.entries.map { dataStoreName ->
        async { ctx.resolveDataStore(dataStoreName).edit { it.clear() } }
    }.awaitAll()
}