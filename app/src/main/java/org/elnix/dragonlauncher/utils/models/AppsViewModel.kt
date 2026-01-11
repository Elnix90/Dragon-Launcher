package org.elnix.dragonlauncher.utils.models

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.XmlResourceParser
import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.content.res.ResourcesCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.data.dummySwipePoint
import org.elnix.dragonlauncher.data.helpers.CustomIconSerializable
import org.elnix.dragonlauncher.data.helpers.SwipePointSerializable
import org.elnix.dragonlauncher.data.stores.AppsSettingsStore
import org.elnix.dragonlauncher.data.stores.SwipeSettingsStore
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.data.stores.WorkspaceSettingsStore
import org.elnix.dragonlauncher.ui.drawer.AppModel
import org.elnix.dragonlauncher.ui.drawer.AppOverride
import org.elnix.dragonlauncher.ui.drawer.IconMapping
import org.elnix.dragonlauncher.ui.drawer.IconPackInfo
import org.elnix.dragonlauncher.ui.drawer.Workspace
import org.elnix.dragonlauncher.ui.drawer.WorkspaceState
import org.elnix.dragonlauncher.ui.drawer.WorkspaceType
import org.elnix.dragonlauncher.ui.drawer.defaultWorkspaces
import org.elnix.dragonlauncher.ui.drawer.resolveApp
import org.elnix.dragonlauncher.utils.APPS_TAG
import org.elnix.dragonlauncher.utils.ICONS_TAG
import org.elnix.dragonlauncher.utils.PackageManagerCompat
import org.elnix.dragonlauncher.utils.TAG
import org.elnix.dragonlauncher.utils.actions.createUntintedBitmap
import org.elnix.dragonlauncher.utils.actions.loadDrawableAsBitmap
import org.elnix.dragonlauncher.utils.actions.resolveCustomIconBitmap
import org.elnix.dragonlauncher.utils.logs.logD
import org.elnix.dragonlauncher.utils.logs.logE
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser


class AppsViewModel(
    application: Application,
    coroutineScope: CoroutineScope
) {
    private val scope = coroutineScope

    private val _apps = MutableStateFlow<List<AppModel>>(emptyList())
    val allApps: StateFlow<List<AppModel>> = _apps.asStateFlow()

    private val _iconPacksList = MutableStateFlow<List<IconPackInfo>>(emptyList())
    val iconPacksList = _iconPacksList.asStateFlow()


    private val _packIcons = MutableStateFlow<List<String>>(emptyList())
    val packIcons: StateFlow<List<String>> = _packIcons.asStateFlow()

    private val _icons = MutableStateFlow<Map<String, ImageBitmap>>(emptyMap())
    val icons = _icons.asStateFlow()

    private val _pointIcons = MutableStateFlow<Map<String, ImageBitmap>>(emptyMap())
    val pointIcons = _pointIcons.asStateFlow()


    // Only used for preview, the real user apps getter are using the appsForWorkspace function
    val userApps: StateFlow<List<AppModel>> = _apps.map { list ->
        list.filter { it.isLaunchable == true }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())


    private val _selectedIconPack = MutableStateFlow<IconPackInfo?>(null)
    val selectedIconPack: StateFlow<IconPackInfo?> = _selectedIconPack.asStateFlow()

    private val iconPackCache = mutableMapOf<String, Map<String, String>>()


    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext

    private val pm: PackageManager = application.packageManager
    private val pmCompat = PackageManagerCompat(pm, ctx)
    private val resourceIdCache = mutableMapOf<String, Int>()

    /**
     * Used to correctly dispatch the heavy background load, as long as I understand
     */
    private val iconSemaphore = Semaphore(4)


    private val gson = Gson()


    /* ───────────── Workspace things ───────────── */
    private val _workspacesState = MutableStateFlow(
        WorkspaceState(
            workspaces = defaultWorkspaces,
            appOverrides = emptyMap()
        )
    )
    val state: StateFlow<WorkspaceState> = _workspacesState.asStateFlow()

    /** Get enabled workspaces only */
    val enabledState: StateFlow<WorkspaceState> = _workspacesState
        .map { state ->
            state.copy(
                workspaces = state.workspaces.filter { it.enabled }
            )
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = WorkspaceState(
                workspaces = emptyList(),
                appOverrides = emptyMap()
            )
        )


    private val _selectedWorkspaceId = MutableStateFlow("user")
    val selectedWorkspaceId: StateFlow<String> = _selectedWorkspaceId.asStateFlow()


    init {
        loadWorkspaces()
        loadApps()
        scope.launch {
            val savedPackName = UiSettingsStore.getIconPack(ctx)
            savedPackName?.let { pkg ->
                loadIconsPacks()
                _selectedIconPack.value = _iconPacksList.value.find { it.packageName == pkg }
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
    ): StateFlow<List<AppModel>> {

        require(!(getOnlyAdded && getOnlyRemoved))

        // May be null cause I added the removed app ids lately, so some user may still have the old app model without it
        val removed = workspace.removedAppIds ?: emptyList()

        return _apps.map { list ->
            when {
                getOnlyAdded -> list.filter { it.packageName in workspace.appIds }
                getOnlyRemoved -> list.filter { it.packageName in removed }
                else -> {
                    val base = when (workspace.type) {
                        WorkspaceType.ALL, WorkspaceType.CUSTOM -> list
                        WorkspaceType.USER -> list.filter { !it.isWorkProfile && it.isLaunchable == true }
                        WorkspaceType.SYSTEM -> list.filter { it.isSystem }
                        WorkspaceType.WORK -> list.filter { it.isWorkProfile && it.isLaunchable == true }
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
        }.stateIn(
            scope,
            SharingStarted.Eagerly,
            emptyList()
        )
    }


    private fun loadApps() {
        scope.launch(Dispatchers.IO) {
            val cachedJson = AppsSettingsStore.getCachedApps(ctx)

            if (!cachedJson.isNullOrEmpty()) {
                try {
                    val type = object : TypeToken<List<AppModel>>() {}.type
                    _apps.value = gson.fromJson(cachedJson, type) ?: emptyList()
                } catch (e: Exception) {
                    logE(TAG, "Failed to parse cached apps, clearing: ${e.message}")
                    AppsSettingsStore.saveCachedApps(ctx, "") // Clear bad cache
                    _apps.value = emptyList()
                }
            }

            scope.launch { reloadApps() }
        }
    }


    /**
     * Reloads apps fresh from PackageManager.
     * Saves updated list into DataStore.
     * This is used by the BroadcastReceiver.
     */
    suspend fun reloadApps() {
        try {

            val apps = withContext(Dispatchers.IO) {
                pmCompat.getAllApps()
            }

            _apps.value = apps.toList()
            _icons.value = loadIcons(apps)

            invalidateAllPointIcons()

            val points = SwipeSettingsStore.getPoints(ctx)

            preloadPointIcons(
                points = points,
                reloadAll = true
            )


            withContext(Dispatchers.IO) {
                AppsSettingsStore.saveCachedApps(ctx, gson.toJson(apps))
            }

            logE(
                APPS_TAG,
                "Reloaded packages, ${apps.filter { it.isLaunchable == true }.size} total apps, (${apps.size} user apps)"
            )

        } catch (e: Exception) {
            logE(APPS_TAG, e.toString())
        }
    }


    fun renderPointIcon(
        point: SwipePointSerializable,
        sizePx: Int
    ): ImageBitmap {

        val base = createUntintedBitmap(
            icons = _icons.value,
            action = point.action,
            ctx = ctx,
            width = sizePx,
            height = sizePx
        )

        val final = if (point.customIcon != null) {
            resolveCustomIconBitmap(
                base = base,
                icon = point.customIcon,
                sizePx = sizePx
            )
        } else {
            base
        }

        return final
    }

    private fun invalidateAllPointIcons() {
        _pointIcons.value = emptyMap()
    }

    fun preloadPointIcons(
        points: List<SwipePointSerializable>,
        sizePx: Int = 64,
        reloadAll: Boolean = false
    ) {
        scope.launch(Dispatchers.Default) {
            val newIcons = buildMap {
                points.forEach { p ->
                    val id = p.id
                    if (_pointIcons.value.containsKey(id) && !reloadAll) return@forEach

                    put(
                        id,
                        renderPointIcon(
                            point = p,
                            sizePx = sizePx
                        )
                    )
                }
            }

            if (newIcons.isNotEmpty()) {
                _pointIcons.update { it + newIcons }
            }
        }
    }


    fun reloadPointIcon(
        point: SwipePointSerializable,
        sizePx: Int = 64
    ) {
        val id = point.id

        scope.launch(Dispatchers.IO) {
            val bmp = renderPointIcon(
                point = point,
                sizePx = sizePx
            )

            _pointIcons.update { it + (id to bmp) }
        }
    }


    private suspend fun loadIcons(
        apps: List<AppModel>
    ): Map<String, ImageBitmap> =
        withContext(Dispatchers.IO) {
            apps.mapNotNull { app ->
                runCatching {
                    iconSemaphore.withPermit {
                        loadSingleIcon(app, true)
                    }
                }.getOrNull()
            }.toMap()
        }


    fun updateSingleIcon(
        app: AppModel,
        useOverride: Boolean
    ) {
        _icons.update { it + loadSingleIcon(app, useOverride) }
    }

    fun loadSingleIcon(
        app: AppModel,
        useOverrides: Boolean
    ): Pair<String, ImageBitmap> {
        // blocking I/O & bitmap decoding

        val packIconName = getCachedIconMapping(app.packageName)
        val drawable =
            packIconName?.let {
                loadIconFromPack(
                    selectedIconPack.value?.packageName,
                    it
                )
            } ?: pmCompat.getAppIcon(app.packageName, app.userId ?: 0)


        val base = loadDrawableAsBitmap(
            drawable, 128, 128
        )

        if (useOverrides) {
            _workspacesState.value.appOverrides[app.packageName]?.customIcon?.let { customIcon ->

                return app.packageName to resolveCustomIconBitmap(
                    base = base,
                    icon = customIcon,
                    sizePx = 128
                )
            }
        }


        return app.packageName to base
    }


    @SuppressLint("DiscouragedApi")
    fun loadIconFromPack(packPkg: String?, iconName: String): android.graphics.drawable.Drawable? {
        if (packPkg == null || iconName.isEmpty()) return null

        return try {
            val packResources = ctx.packageManager.getResourcesForApplication(packPkg)
            val resId = packResources.getIdentifier(iconName, "drawable", packPkg)
            if (resId != 0) {
                ResourcesCompat.getDrawable(packResources, resId, null)
            } else null
        } catch (_: Exception) {
            logE(ICONS_TAG, "Failed to load icon $iconName from $packPkg")
            null
        }
    }

    fun loadAllIconsFromPack(pack: IconPackInfo) {

        scope.launch(Dispatchers.IO) {
            val mappings = iconPackCache.getOrPut(pack.packageName) {
                loadIconPackMappings(pack.packageName)
            }

            if (mappings.isEmpty()) {
                _packIcons.value = emptyList()
                return@launch
            }

            _packIcons.value = mappings.values.distinct()
        }
    }


    private fun getCachedIconMapping(pkgName: String): String? {
        return selectedIconPack.value?.let { pack ->
            iconPackCache.getOrPut(pack.packageName) {
                loadIconPackMappings(pack.packageName)
            }[pkgName]
        }
    }

    fun loadSavedIconPack() {
        scope.launch {
            val savedPackName = UiSettingsStore.getIconPack(ctx)
            savedPackName?.let { pkg ->
                _iconPacksList.value.find { it.packageName == pkg }?.let { pack ->
                    _selectedIconPack.value = pack
                }
            }
        }
    }

    fun selectIconPack(pack: IconPackInfo) {
        _selectedIconPack.value = pack
        scope.launch(Dispatchers.IO) {
            UiSettingsStore.setIconPack(ctx, pack.packageName)
            iconPackCache[pack.packageName] = loadIconPackMappings(pack.packageName)
            reloadApps()
        }
    }


    fun clearIconPack() {
        _selectedIconPack.value = null
        scope.launch(Dispatchers.IO) {
            UiSettingsStore.setIconPack(ctx, null)
            reloadApps()
        }
    }


    fun loadIconsPacks() {
        val packs = mutableListOf<IconPackInfo>()
        val allPackages = pmCompat.getInstalledPackages()

        logD(ICONS_TAG, "Scanning ${allPackages.size} packages...")

        allPackages.forEach { pkgInfo ->
            try {
                val packResources = pmCompat.getResourcesForApplication(pkgInfo.packageName)
                val hasAppfilter = hasAppfilterResource(packResources, pkgInfo.packageName)

                if (hasAppfilter && pkgInfo.packageName != ctx.packageName) {
                    val label = pkgInfo.applicationInfo?.loadLabel(pm).toString()
                    logD(ICONS_TAG, "FOUND icon pack: $label (${pkgInfo.packageName})")
                    packs.add(IconPackInfo(pkgInfo.packageName, label))
                }
            } catch (_: Exception) {
            }
        }

        val uniquePacks = packs.distinctBy { it.packageName }
        logD(ICONS_TAG, "Total icon packs found: ${uniquePacks.size}")
        _iconPacksList.value = uniquePacks
    }


    @SuppressLint("DiscouragedApi")
    private fun hasAppfilterResource(
        resources: android.content.res.Resources,
        pkgName: String
    ): Boolean {
        val locations = listOf("appfilter", "theme_appfilter", "icon_appfilter")
        return locations.any { name ->
            val cacheKey = "$pkgName:$name"
            val resId = resourceIdCache.getOrPut(cacheKey) {
                resources.getIdentifier(name, "xml", pkgName)
            }
            resId != 0
        }
    }

    fun loadIconPackMappings(packPkg: String): Map<String, String> {
        return try {
            parseAppFilterXml(ctx, packPkg)?.associate {
                val pkg = it.component.substringAfter('{').substringBefore('/')
                pkg to it.drawable
            } ?: emptyMap()
        } catch (e: Exception) {
            logE(ICONS_TAG, "Failed to load mappings for $packPkg: ${e.message}")
            emptyMap()
        }
    }


    /** Load the user's workspaces into the _state var, enforced safety due to some crash at start */
    private fun loadWorkspaces() = scope.launch(Dispatchers.IO) {
        try {
            val json = WorkspaceSettingsStore.getAll(ctx).toString()

            // Correct generic type: WorkspaceState with List<Workspace>
            val type = object : TypeToken<WorkspaceState>() {}.type
            val loadedState: WorkspaceState? = gson.fromJson(json, type)

            _workspacesState.value = loadedState?.copy(
                workspaces = loadedState.workspaces,
                appOverrides = loadedState.appOverrides
            ) ?: WorkspaceState(
                workspaces = defaultWorkspaces,
                appOverrides = emptyMap()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            _workspacesState.value = WorkspaceState(
                workspaces = defaultWorkspaces,
                appOverrides = emptyMap()
            )
        }

        // Load the appOverrides in the pointsIcons too
        _workspacesState.value.appOverrides.forEach { (packageName, override) ->
            override.customIcon?.let { customIcon ->
                reloadPointIcon(
                    point = dummySwipePoint(SwipeActionSerializable.LaunchApp(packageName)).copy(
                        customIcon = customIcon,
                        id = packageName
                    )
                )
            }
        }
    }


    private fun persist() = scope.launch(Dispatchers.IO) {
        WorkspaceSettingsStore.setAll(
            ctx,
            JSONObject(gson.toJson(_workspacesState.value))
        )
    }

    fun selectWorkspace(id: String) {
        _selectedWorkspaceId.value = id
    }


    /** Enable/disable a workspace */
    fun setWorkspaceEnabled(id: String, enabled: Boolean) {
        _workspacesState.value = _workspacesState.value.copy(
            workspaces = _workspacesState.value.workspaces.map { workspace ->
                if (workspace.id == id) {
                    workspace.copy(enabled = enabled)
                } else {
                    workspace
                }
            }
        )
        persist()
    }

    fun createWorkspace(name: String, type: WorkspaceType) {
        _workspacesState.value = _workspacesState.value.copy(
            workspaces = _workspacesState.value.workspaces +
                    Workspace(
                        id = System.currentTimeMillis().toString(),
                        name = name,
                        type = type,
                        enabled = true,
                        removedAppIds = emptyList(),
                        appIds = emptyList()
                    )
        )
        persist()
    }

    fun editWorkspace(id: String, name: String, type: WorkspaceType) {
        _workspacesState.value = _workspacesState.value.copy(
            workspaces = _workspacesState.value.workspaces.map {
                if (it.id == id) it.copy(name = name, type = type) else it
            }
        )
        persist()
    }

    fun deleteWorkspace(id: String) {
        _workspacesState.value = _workspacesState.value.copy(
            workspaces = _workspacesState.value.workspaces.filterNot { it.id == id }
        )
        persist()
    }

    fun setWorkspaceOrder(newOrder: List<Workspace>) {
        _workspacesState.value = _workspacesState.value.copy(workspaces = newOrder)
        persist()
    }


    fun resetWorkspace(id: String) {
        _workspacesState.value = _workspacesState.value.copy(
            workspaces = _workspacesState.value.workspaces.map {
                if (it.id == id) it.copy(removedAppIds = emptyList(), appIds = emptyList()) else it
            }
        )
        persist()
    }


    // Apps operations
    fun addAppToWorkspace(workspaceId: String, packageName: String) {
        _workspacesState.value = _workspacesState.value.copy(
            workspaces = _workspacesState.value.workspaces.map { ws ->
                if (ws.id != workspaceId) return@map ws

                val removed = ws.removedAppIds ?: emptySet()

                ws.copy(
                    appIds = ws.appIds + packageName,
                    removedAppIds = if (packageName in removed)
                        removed - packageName
                    else
                        ws.removedAppIds
                )
            }
        )
        persist()
    }


    fun removeAppFromWorkspace(workspaceId: String, packageName: String) {
        _workspacesState.value = _workspacesState.value.copy(
            workspaces = _workspacesState.value.workspaces.map { ws ->
                if (ws.id != workspaceId) return@map ws

                // remove the app packageName from appsIds, and add it to removedAppIDs
                ws.copy(
                    appIds = ws.appIds - packageName,
                    removedAppIds = (ws.removedAppIds ?: emptyList()) + packageName
                )
            }
        )
        persist()
    }


    fun renameApp(packageName: String, name: String) {
        _workspacesState.value = _workspacesState.value.copy(
            appOverrides = _workspacesState.value.appOverrides +
                    (packageName to AppOverride(packageName, name))
        )
        persist()
    }

    fun setAppIcon(packageName: String, customIcon: CustomIconSerializable?) {
        val prev = _workspacesState.value.appOverrides[packageName]
        _workspacesState.value = _workspacesState.value.copy(
            appOverrides = _workspacesState.value.appOverrides +
                    (packageName to (prev?.copy(customIcon = customIcon)
                        ?: AppOverride(packageName, customIcon = customIcon)))
        )
        persist()
    }

    fun applyIconToApps(
        icon: CustomIconSerializable?
    ) {
        scope.launch {
            iconSemaphore.withPermit {

                // Store icon ONCE
                val sharedIcon = icon?.copy()

                _workspacesState.value = _workspacesState.value.copy(
                    appOverrides = _apps.value.associate {
                        (it.packageName to AppOverride(it.packageName, customIcon = sharedIcon))
                    }
                )
            }
        }
        persist()
    }


    fun resetAppName(packageName: String) {
        val prev = _workspacesState.value.appOverrides[packageName] ?: return

        val updated = prev.copy(customLabel = null)

        _workspacesState.value = _workspacesState.value.copy(
            appOverrides =
                if (updated.customIcon == null)
                    _workspacesState.value.appOverrides - packageName
                else
                    _workspacesState.value.appOverrides + (packageName to updated)
        )
        scope.launch {
            reloadApps()
        }
        persist()
    }

    fun resetAppIcon(packageName: String) {
        val prev = _workspacesState.value.appOverrides[packageName] ?: return

        val updated = prev.copy(customIcon = null)

        _workspacesState.value = _workspacesState.value.copy(
            appOverrides =
                if (updated.customLabel == null)
                    _workspacesState.value.appOverrides - packageName
                else
                    _workspacesState.value.appOverrides + (packageName to updated)
        )

        scope.launch {
            reloadApps()
        }
        persist()
    }


    fun resetWorkspacesAndOverrides() {
        _workspacesState.value = WorkspaceState(
            workspaces = defaultWorkspaces,
            appOverrides = emptyMap()
        )
        persist()
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
        ctx.logE(ICONS_TAG, "XML parse failed: ${e.message}")
        null
    }
}
