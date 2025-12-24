package org.elnix.dragonlauncher.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import org.elnix.dragonlauncher.data.stores.AppsSettingsStore
import org.elnix.dragonlauncher.data.stores.BackupSettingsStore
import org.elnix.dragonlauncher.data.stores.BehaviorSettingsStore
import org.elnix.dragonlauncher.data.stores.ColorModesSettingsStore
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore
import org.elnix.dragonlauncher.data.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.data.stores.LanguageSettingsStore
import org.elnix.dragonlauncher.data.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.data.stores.SwipeSettingsStore
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.data.stores.WallpaperSettingsStore
import org.elnix.dragonlauncher.data.stores.WorkspaceSettingsStore

enum class DataStoreName(val value: String, val backupKey: String?, val store: BaseSettingsStore) {
    UI("uiDatastore", "ui", UiSettingsStore),
    COLOR_MODE("colorModeDatastore", "color_mode", ColorModesSettingsStore),
    COLOR("colorDatastore", "color", ColorSettingsStore),
    PRIVATE_SETTINGS("privateSettingsStore", null, PrivateSettingsStore),
    SWIPE("swipePointsDatastore", "actions", SwipeSettingsStore),
    LANGUAGE("languageDatastore", "language", LanguageSettingsStore),
    DRAWER("drawerDatastore", "drawer", DrawerSettingsStore),
    DEBUG("debugDatastore", "debug", DebugSettingsStore),
    WORKSPACES("workspacesDataStore", "workspaces", WorkspaceSettingsStore),
    APPS("appsDatastore",null, AppsSettingsStore),
    BEHAVIOR("behaviorDatastore", "behavior", BehaviorSettingsStore),
    BACKUP("backupDatastore", "backup", BackupSettingsStore),
    WALLPAPER("wallpaperDatastore", "wallpaper", WallpaperSettingsStore)
}

val Context.uiDatastore by preferencesDataStore(name = DataStoreName.UI.value)
val Context.colorModeDatastore by preferencesDataStore(name = DataStoreName.COLOR_MODE.value)
val Context.colorDatastore by preferencesDataStore(name = DataStoreName.COLOR.value)
val Context.privateSettingsStore by preferencesDataStore(name = DataStoreName.PRIVATE_SETTINGS.value)
val Context.swipeDataStore by preferencesDataStore(name = DataStoreName.SWIPE.value)
val Context.languageDatastore by preferencesDataStore(name = DataStoreName.LANGUAGE.value)
val Context.drawerDataStore by preferencesDataStore(name = DataStoreName.DRAWER.value)
val Context.debugDatastore by preferencesDataStore(name = DataStoreName.DEBUG.value)
val Context.workspaceDataStore by preferencesDataStore(name = DataStoreName.WORKSPACES.value)
val Context.appDrawerDataStore by preferencesDataStore(name = DataStoreName.APPS.value)
val Context.behaviorDataStore by preferencesDataStore(name = DataStoreName.BEHAVIOR.value)
val Context.backupDatastore by preferencesDataStore(name = DataStoreName.BACKUP.value)
val Context.wallpaperSettingsStore by preferencesDataStore(name = DataStoreName.WALLPAPER.value)
