package org.elnix.dragonlauncher.utils

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.ui.drawer.AppModel

val Context.appDrawerDataStore by preferencesDataStore("app_drawer")

class AppDrawerViewModel(application: Application) : AndroidViewModel(application) {

    private val _apps = MutableStateFlow<List<AppModel>>(emptyList())
    val allApps: StateFlow<List<AppModel>> = _apps.asStateFlow()
    val userApps: StateFlow<List<AppModel>> = _apps.map { list ->
        list.filter { !it.isSystem }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val systemApps: StateFlow<List<AppModel>> = _apps.map { list ->
        list.filter { it.isSystem }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val pm: PackageManager = application.packageManager
    private val ctx = application.applicationContext
    private val gson = Gson()
    @Suppress("PrivatePropertyName")
    private val DATASTORE_KEY = stringPreferencesKey("cached_apps_json")

    init {
        viewModelScope.launch {
            loadApps()
        }
    }

    private suspend fun loadApps() {
        // 1️⃣ Load cached apps from DataStore
        val cachedJson = ctx.appDrawerDataStore.data
            .map { it[DATASTORE_KEY] }
            .firstOrNull()

        if (!cachedJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<AppModel>>() {}.type
            _apps.value = gson.fromJson(cachedJson, type)
        }

        // 2️⃣ Refresh from PackageManager asynchronously
        viewModelScope.launch {
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .map { appInfo ->
                    AppModel(
                        name = appInfo.loadLabel(pm).toString(),
                        packageName = appInfo.packageName,
                        isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    )
                }
                .sortedBy { it.name.lowercase() }

            _apps.value = installedApps

            // 3️⃣ Save to DataStore as JSON
            val json = gson.toJson(installedApps)
            ctx.appDrawerDataStore.edit { prefs ->
                prefs[DATASTORE_KEY] = json
            }
        }
    }
}
