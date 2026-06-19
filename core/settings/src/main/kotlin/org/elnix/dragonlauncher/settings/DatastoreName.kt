package org.elnix.dragonlauncher.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.elnix.dragonlauncher.settings.DataStoreName.AngleLine
import org.elnix.dragonlauncher.settings.DataStoreName.AppOverrides
import org.elnix.dragonlauncher.settings.DataStoreName.Backup
import org.elnix.dragonlauncher.settings.DataStoreName.Behavior
import org.elnix.dragonlauncher.settings.DataStoreName.Color
import org.elnix.dragonlauncher.settings.DataStoreName.ColorMode
import org.elnix.dragonlauncher.settings.DataStoreName.Debug
import org.elnix.dragonlauncher.settings.DataStoreName.Drawer
import org.elnix.dragonlauncher.settings.DataStoreName.HoldToActivate
import org.elnix.dragonlauncher.settings.DataStoreName.Icons
import org.elnix.dragonlauncher.settings.DataStoreName.Language
import org.elnix.dragonlauncher.settings.DataStoreName.Nests
import org.elnix.dragonlauncher.settings.DataStoreName.Points
import org.elnix.dragonlauncher.settings.DataStoreName.Private
import org.elnix.dragonlauncher.settings.DataStoreName.StatusBar
import org.elnix.dragonlauncher.settings.DataStoreName.StatusBarJson
import org.elnix.dragonlauncher.settings.DataStoreName.Swipe
import org.elnix.dragonlauncher.settings.DataStoreName.Ui
import org.elnix.dragonlauncher.settings.DataStoreName.Wellbeing
import org.elnix.dragonlauncher.settings.DataStoreName.Widgets
import org.elnix.dragonlauncher.settings.DataStoreName.Workspaces
import org.elnix.dragonlauncher.settings.bases.stores.SettingsStore
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
import org.elnix.dragonlauncher.settings.stores.array.AppOverridesSettingsStore
import org.elnix.dragonlauncher.settings.stores.array.WorkspaceSettingsStore


enum class DataStoreName(
    val userBackup: Boolean = true
) {
    Ui,
    Icons,
    ColorMode,
    Color,
    Private(false),
    Points,
    Nests,
    Language,
    Drawer,
    Debug,
    AppOverrides,
    Workspaces,
    Behavior,
    Backup,
    StatusBar,
    Widgets,
    Wellbeing,
    Swipe,
    StatusBarJson,
    AngleLine,
    HoldToActivate
}


private val dataStoreDelegates = DataStoreName.entries.associate { dataStoreName ->
    dataStoreName.name to preferencesDataStore(name = dataStoreName.name)
}

internal fun Context.resolveDataStore(dataStoreName: DataStoreName): DataStore<Preferences> {
    return dataStoreDelegates[dataStoreName.name]?.getValue(this, ::dataStoreDelegates)
        ?: error("No DataStore delegate found for ${dataStoreName.name}")
}

val allStores: Map<DataStoreName, SettingsStore<*, *>> by lazy {
    DataStoreName.entries.associateWith {
        when (it) {
            Ui -> UiSettingsStore
            Icons -> IconsSettingsStore
            ColorMode -> ColorModesSettingsStore
            Color -> ColorSettingsStore
            Private -> PrivateSettingsStore
            Points -> PointsSettingsStore
            Nests -> NestsSettingsStore
            Language -> LanguageSettingsStore
            Drawer -> DrawerSettingsStore
            Debug -> DebugSettingsStore
            AppOverrides -> AppOverridesSettingsStore
            Workspaces -> WorkspaceSettingsStore
            Behavior -> BehaviorSettingsStore
            Backup -> BackupSettingsStore
            StatusBar -> StatusBarJsonSettingsStore
            Widgets -> WidgetsSettingsStore
            Wellbeing -> WellbeingSettingsStore
            Swipe -> SwipeMapSettingsStore
            StatusBarJson -> StatusBarJsonSettingsStore
            AngleLine -> AngleLineSettingsStore
            HoldToActivate -> HoldToActivateArcSettingsStore
        }
    }
}

val themeDataStores: Set<DataStoreName> by lazy {
    setOf(Ui, ColorMode, Color, AngleLine, HoldToActivate)
}

val backupableStores: Map<DataStoreName, SettingsStore<*, *>> by lazy {
    allStores.filterKeys { it.userBackup }
}

suspend fun clearAllData(ctx: Context) = coroutineScope {
    DataStoreName.entries.map { dataStoreName ->
        async { ctx.resolveDataStore(dataStoreName).edit { it.clear() } }
    }.awaitAll()
}