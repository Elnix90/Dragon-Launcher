package org.elnix.dragonlauncher.utils.models

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.XmlResourceParser
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.content.res.ResourcesCompat
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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.data.stores.AppsSettingsStore
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.drawer.AppModel
import org.elnix.dragonlauncher.ui.drawer.AppOverride
import org.elnix.dragonlauncher.ui.drawer.IconMapping
import org.elnix.dragonlauncher.ui.drawer.IconPackInfo
import org.elnix.dragonlauncher.ui.drawer.Workspace
import org.elnix.dragonlauncher.ui.drawer.WorkspaceType
import org.elnix.dragonlauncher.ui.drawer.resolveApp
import org.elnix.dragonlauncher.utils.PackageManagerCompat
import org.elnix.dragonlauncher.utils.TAG
import org.elnix.dragonlauncher.utils.actions.loadDrawableAsBitmap
import org.xmlpull.v1.XmlPullParser


class AppsViewModel(application: Application) : AndroidViewModel(application) {



    private val _apps = MutableStateFlow<List<AppModel>>(emptyList())
    val allApps: StateFlow<List<AppModel>> = _apps.asStateFlow()
    private val _icons = MutableStateFlow<Map<String, ImageBitmap>>(emptyMap())
    val icons: StateFlow<Map<String, ImageBitmap>> = _icons


    // Only used for preview, the real user apps getter are using the appsForWorkspace function
    val userApps: StateFlow<List<AppModel>> = _apps.map { list ->
        list.filter { it.isLaunchable == true }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())



    private val _selectedIconPack = MutableStateFlow<IconPackInfo?>(null)
    val selectedIconPack: StateFlow<IconPackInfo?> = _selectedIconPack.asStateFlow()

    private val iconPackCache = mutableMapOf<String, Map<String, String>>()


    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext

    private val pm: PackageManager = application.packageManager
    private val pmCompat = PackageManagerCompat(pm, ctx)
    private val resourceIdCache = mutableMapOf<String, Int>()


    private val gson = Gson()
    init {
        loadApps()
        viewModelScope.launch {
            val savedPackName = UiSettingsStore.getIconPack(ctx)
            savedPackName?.let { pkg ->
                val packs = findIconPacks()
                _selectedIconPack.value = packs.find { it.packageName == pkg }
            }
        }
    }



    /**
     * Returns a filtered and sorted list of apps for the specified workspace as a reactive Flow.
     *
     * @param workspace The target workspace configuration defining app filtering rules
     * @param overrides Custom app overrides to apply (icon/label changes, etc.)
     * @param getOnlyAdded If true, returns ONLY apps explicitly added to this workspace [default: false]
     * @param getOnlyRemoved If true, returns ONLY apps hidden/removed from this workspace [default: false]
     * @return Flow of filtered, sorted, and resolved [AppModel] list
     *
     * @throws IllegalArgumentException if both [getOnlyAdded] and [getOnlyRemoved] are true
     *
     * @see WorkspaceType for base filtering behavior
     * @see AppOverride for override application details
     * @see resolveApp for final app resolution logic
     */
    fun appsForWorkspace(
        workspace: Workspace,
        overrides: Map<String, AppOverride>,
        getOnlyAdded: Boolean = false,
        getOnlyRemoved: Boolean = false
    ): Flow<List<AppModel>> {

        require(!(getOnlyAdded && getOnlyRemoved))

        // May be null cause I added the removed app ids lately, so some user may still have the old app model without it
        val removed = workspace.removedAppIds ?: emptyList()

        return allApps.map { list ->
            when {
                getOnlyAdded -> list.filter { it.packageName in workspace.appIds }
                getOnlyRemoved -> list.filter { it.packageName in removed }
                else -> {
                    val base = when (workspace.type) {
                        WorkspaceType.ALL, WorkspaceType.CUSTOM -> list
                        WorkspaceType.USER -> list.filter { !it.isSystem && !it.isWorkProfile && it.isLaunchable == true }
                        WorkspaceType.SYSTEM -> list.filter { it.isSystem }
                        WorkspaceType.WORK -> list.filter { it.isWorkProfile }
                    }

                    val added = list.filter { it.packageName in workspace.appIds }

                    // Use the base list, and add the new ones (present in added list) and filter them,
                    // to remove the removed packages from the workspace
                    (base + added)
                        .distinctBy { it.packageName }
                        .filter { it.packageName !in removed }
                        .sortedBy { it.name.lowercase() }
                        .map { resolveApp(it, overrides) }
                }
            }
        }
    }




    private fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val cachedJson = AppsSettingsStore.getCachedApps(ctx)

            if (!cachedJson.isNullOrEmpty()) {
                try {
                    val type = object : TypeToken<List<AppModel>>() {}.type
                    _apps.value = gson.fromJson(cachedJson, type) ?: emptyList()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse cached apps, clearing: ${e.message}")
                    AppsSettingsStore.saveCachedApps(ctx, "") // Clear bad cache
                    _apps.value = emptyList()
                }
            }

            viewModelScope.launch { reloadApps(ctx) }
        }
    }


    /**
     * Reloads apps fresh from PackageManager.
     * Saves updated list into DataStore.
     * This is used by the BroadcastReceiver.
     */
    suspend fun reloadApps(ctx: Context) {
        val apps = withContext(Dispatchers.IO) {
            pmCompat.getAllApps()
        }

        _apps.value = apps
        _icons.value = loadIcons(apps)

        withContext(Dispatchers.IO) {
            AppsSettingsStore.saveCachedApps(ctx, gson.toJson(apps))
        }
        Log.e("AppsVm", "Reloaded packages.")
    }


    private fun loadIcons(apps: List<AppModel>): Map<String, ImageBitmap> =
        runBlocking(Dispatchers.IO) {  // Off main thread
            apps.associate { app ->
                val packIconName = getCachedIconMapping(app.packageName)
                val drawable = packIconName?.let { loadIconFromPack(selectedIconPack.value?.packageName, it) }
                val finalDrawable = drawable ?: pmCompat.getAppIcon(app.packageName)
                app.packageName to loadDrawableAsBitmap(finalDrawable, 128, 128)
            }
        }

    @SuppressLint("DiscouragedApi")
    private fun loadIconFromPack(packPkg: String?, iconName: String): android.graphics.drawable.Drawable? {
        if (packPkg == null || iconName.isEmpty()) return null

        return try {
            val packResources = ctx.packageManager.getResourcesForApplication(packPkg)
            val resId = packResources.getIdentifier(iconName, "drawable", packPkg)
            if (resId != 0) {
                ResourcesCompat.getDrawable(packResources, resId, null)
            } else null
        } catch (_: Exception) {
            Log.e("icon_pack", "Failed to load icon $iconName from $packPkg")
            null
        }
    }


    private fun getCachedIconMapping(pkgName: String): String? {
        return selectedIconPack.value?.let { pack ->
            iconPackCache.getOrPut(pack.packageName) {
                loadIconPackMappings(ctx, pack.packageName)
            }[pkgName]
        }
    }

    fun loadSavedIconPack(ctx: Context) {
        viewModelScope.launch {
            val savedPackName = UiSettingsStore.getIconPack(ctx)
            savedPackName?.let { pkg ->
                val packs = findIconPacks()
                packs.find { it.packageName == pkg }?.let { pack ->
                    _selectedIconPack.value = pack
                }
            }
        }
    }

    fun selectIconPack(pack: IconPackInfo) {
        _selectedIconPack.value = pack
        viewModelScope.launch(Dispatchers.IO) {
            UiSettingsStore.setIconPack(ctx, pack.packageName)
            iconPackCache[pack.packageName] = loadIconPackMappings(ctx, pack.packageName)
            reloadApps(ctx)
        }
    }


    fun clearIconPack() {
        _selectedIconPack.value = null
        viewModelScope.launch(Dispatchers.IO) {
            UiSettingsStore.setIconPack(ctx, null)
            reloadApps(ctx)
        }
    }


    fun findIconPacks(): List<IconPackInfo> {
        val packs = mutableListOf<IconPackInfo>()
        val allPackages = pmCompat.getInstalledPackages()

        Log.d("icon_pack", "Scanning ${allPackages.size} packages...")

        allPackages.forEach { pkgInfo ->
            try {
                val packResources = pmCompat.getResourcesForApplication(pkgInfo.packageName)
                val hasAppfilter = hasAppfilterResource(packResources, pkgInfo.packageName)

                if (hasAppfilter && pkgInfo.packageName != ctx.packageName) {
                    val label = pkgInfo.applicationInfo?.loadLabel(pm).toString()
                    Log.d("icon_pack", "FOUND icon pack: $label (${pkgInfo.packageName})")
                    packs.add(IconPackInfo(pkgInfo.packageName, label))
                }
            } catch (_: Exception) { }
        }

        val uniquePacks = packs.distinctBy { it.packageName }
        Log.d("icon_pack", "Total icon packs found: ${uniquePacks.size}")
        return uniquePacks
    }



    @SuppressLint("DiscouragedApi")
    private fun hasAppfilterResource(resources: android.content.res.Resources, pkgName: String): Boolean {
        val locations = listOf("appfilter", "theme_appfilter", "icon_appfilter")
        return locations.any { name ->
            val cacheKey = "$pkgName:$name"
            val resId = resourceIdCache.getOrPut(cacheKey) {
                resources.getIdentifier(name, "xml", pkgName)
            }
            resId != 0
        }
    }

    fun loadIconPackMappings(ctx: Context, packPkg: String): Map<String, String> {
        return try {
            parseAppFilterXml(ctx, packPkg)?.associate {
                val pkg = it.component.substringAfter('{').substringBefore('/')
                pkg to it.drawable
            } ?: emptyMap()
        } catch (e: Exception) {
            Log.e("icon_pack", "Failed to load mappings for $packPkg: ${e.message}")
            emptyMap()
        }
    }
}

@SuppressLint("DiscouragedApi")
private fun parseAppFilterXml(ctx: Context, packPkg: String): List<IconMapping>? {
    return try {
        val packResources = ctx.packageManager.getResourcesForApplication(packPkg)
        val resId = packResources.getIdentifier("appfilter", "xml", packPkg)
        if (resId == 0) return null

        val parser: XmlResourceParser = packResources.getXml(resId)
        val mappings = mutableListOf<IconMapping>()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                val component = parser.getAttributeValue(null, "component")
                val drawable = parser.getAttributeValue(null, "drawable")
                if (!component.isNullOrEmpty() && !drawable.isNullOrEmpty()) {
                    mappings.add(IconMapping(component, drawable))
                }
            }
            eventType = parser.next()
        }
        parser.close()
        mappings
    } catch (e: Exception) {
        Log.e("icon_pack", "XML parse failed: ${e.message}")
        null
    }
}
