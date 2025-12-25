package org.elnix.dragonlauncher.utils.models

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
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
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(
            intent,
            PackageManager.MATCH_ALL
        )

        val apps = resolveInfos
            .mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                val appInfo = activityInfo.applicationInfo

                // Enabled state (launcher-relevant)
                val isEnabled =
                    activityInfo.enabled &&
                            appInfo.enabled &&
                            pm.getApplicationEnabledSetting(appInfo.packageName) !=
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED

                if (!isEnabled) return@mapNotNull null

                // Updated system apps = user apps (Gmail, Google, Maps, etc.)
                val isUpdatedSystemApp =
                    (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

                // Pure system apps (OS internals)
                val isPureSystemApp =
                    (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                            !isUpdatedSystemApp

                // Launcher-grade system classification
                val isSystem =
                    isPureSystemApp &&
                            (
                                    appInfo.packageName.startsWith("com.android.") ||
                                            appInfo.packageName.startsWith("android")
                                    )

                AppModel(
                    name = resolveInfo.loadLabel(pm).toString(),
                    packageName = appInfo.packageName,
                    isEnabled = true,
                    isSystem = isSystem,
                    isWorkProfile = false // handled properly via LauncherApps if you add it
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.name.lowercase() }

        _apps.value = apps

        val iconMap = apps.associate { app ->
            val drawable = try {
                val appInfo = pm.getApplicationInfo(app.packageName, 0)
                appInfo.loadUnbadgedIcon(pm)
            } catch (_: Exception) {
                ContextCompat.getDrawable(ctx, R.drawable.ic_app_default)!!
            }

            app.packageName to loadDrawableAsBitmap(drawable, 128, 128)
        }

        _icons.value = iconMap

        AppsSettingsStore.saveCachedApps(ctx, gson.toJson(apps))
    }
}
