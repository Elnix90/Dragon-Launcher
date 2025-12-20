package org.elnix.dragonlauncher.utils.models

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.stores.AppsSettingsStore
import org.elnix.dragonlauncher.ui.drawer.AppModel
import org.elnix.dragonlauncher.ui.drawer.AppOverride
import org.elnix.dragonlauncher.ui.drawer.Workspace
import org.elnix.dragonlauncher.ui.drawer.WorkspaceType
import org.elnix.dragonlauncher.ui.drawer.resolveApp
import org.elnix.dragonlauncher.utils.actions.loadDrawableAsBitmap


class AppDrawerViewModel(application: Application) : AndroidViewModel(application) {

    private val _apps = MutableStateFlow<List<AppModel>>(emptyList())
    val allApps: StateFlow<List<AppModel>> = _apps.asStateFlow()
    private val _icons = MutableStateFlow<Map<String, ImageBitmap>>(emptyMap())
    val icons: StateFlow<Map<String, ImageBitmap>> = _icons

    val userApps: StateFlow<List<AppModel>> = _apps.map { list ->
        list.filter { !it.isSystem }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
//
//    val workProfileApps: StateFlow<List<AppModel>> = _apps.map { list ->
//        list.filter { it.isWorkProfile && !it.isSystem }
//    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
//
//    val systemApps: StateFlow<List<AppModel>> = _apps.map { list ->
//        list.filter { it.isSystem }
//    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val pm: PackageManager = application.packageManager
    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext
    private val gson = Gson()
    init {
        loadApps()
    }


    fun appsForWorkspace(
        workspace: Workspace,
        overrides: Map<String, AppOverride>
    ): Flow<List<AppModel>> =
        allApps.map { list ->
            val base = when (workspace.type) {
                WorkspaceType.ALL -> list
                WorkspaceType.USER -> list.filter { !it.isSystem && !it.isWorkProfile }
                WorkspaceType.SYSTEM -> list.filter { it.isSystem }
                WorkspaceType.WORK -> list.filter { it.isWorkProfile }
                WorkspaceType.CUSTOM ->
                    list.filter { it.packageName in workspace.appIds }
            }

            // May be null cause I added the removed app ids lately, so some user may still have the old app model without it
            val removed = workspace.removedAppIds ?: emptyList()

            val added = list.filter { it.packageName in workspace.appIds }

            // Use the base list, and add the new ones (present in added list) and filter them, to remove the removed packages from the workspace
            (base + added)
                .distinctBy { it.packageName }
                .filter { it.packageName !in removed }
                .sortedBy { it.name.lowercase() }
                .map { resolveApp(it,overrides) }
        }



    private fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            // Load cached apps first
            val cachedJson = AppsSettingsStore.getCachedApps(ctx)

            if (!cachedJson.isNullOrEmpty()) {
                val type = object : TypeToken<List<AppModel>>() {}.type
                _apps.value = gson.fromJson(cachedJson, type)
            }

            // Refresh in background
            viewModelScope.launch {
                reloadApps(ctx)
            }
        }
    }

    /**
     * Reloads apps fresh from PackageManager.
     * Saves updated list into DataStore.
     * This is used by the BroadcastReceiver.
     */
    suspend fun reloadApps(ctx: Context) {
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .map { appInfo ->

                val userId = appInfo.uid / 100000  // Extract user ID from UID
                val isWorkProfile = userId > 10    // Work profiles typically start at user 10+


                val enabledState = pm.getApplicationEnabledSetting(appInfo.packageName)
                val isEnabled = enabledState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
                enabledState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT


                AppModel(
                    name = appInfo.loadLabel(pm).toString(),
                    packageName = appInfo.packageName,
                    isEnabled = isEnabled,
                    isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    isWorkProfile = isWorkProfile
                )
            }
            .sortedBy { it.name.lowercase() }

        _apps.value = installedApps

        val iconMap = installedApps.associate { app ->
            val drawable = try {
                // Method 1: Use loadUnbadgedIcon() for launcher-style icons
                val appInfo = pm.getApplicationInfo(app.packageName, PackageManager.GET_META_DATA)
                appInfo.loadUnbadgedIcon(pm)

            } catch (_: Exception) {
                try {
                    // Method 2: Fallback to adaptive icon with proper flags
                    pm.getApplicationIcon(app.packageName)
                } catch (_: Exception) {
                    // Method 3: Final fallback to default icon
                    ContextCompat.getDrawable(ctx, R.drawable.ic_app_default)!!
                }
            }

            val bmp = loadDrawableAsBitmap(drawable, 128, 128)
            app.packageName to bmp
        }

        _icons.value = iconMap

        // Save list into datastore
        val json = gson.toJson(installedApps)
        AppsSettingsStore.saveCachedApps(ctx, json)
    }
}
