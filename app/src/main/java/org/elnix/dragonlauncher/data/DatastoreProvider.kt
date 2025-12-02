package org.elnix.dragonlauncher.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.uiDatastore by preferencesDataStore(name = "uiDatastore")
val Context.colorModeDatastore by preferencesDataStore("colorModeDatastore")
val Context.colorDatastore by preferencesDataStore("colorDatastore")
val Context.privateSettingsStore by preferencesDataStore("privateSettingsStore")
val Context.swipeDataStore by preferencesDataStore("swipePointsDatastore")
val Context.debugDatastore by preferencesDataStore("debugDatastore")
val Context.languageDatastore by preferencesDataStore("languageDatastore")
val Context.drawerDataStore by preferencesDataStore("drawerDatastore")