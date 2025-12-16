package org.elnix.dragonlauncher.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

enum class DataStoreName(val value: String, val backupKey: String) {
    UI("uiDatastore", "ui"),
    COLOR_MODE("colorModeDatastore", "color_mode"),
    COLOR("colorDatastore", "color"),
    PRIVATE_SETTINGS("privateSettingsStore", "debug"),
    SWIPE("swipePointsDatastore", "actions"),
    LANGUAGE("languageDatastore", "language"),
    DRAWER("drawerDatastore", "drawer"),

    DEBUG("debugDatastore", "debug"),
    WORKSPACES("workspacesDataStore", "workspaces"),
    APPS("appsDatastore","apps")
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
