@file:OptIn(ExperimentalCoroutinesApi::class)

package org.elnix.dragonlauncher.models

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.appoverrides.AppOverridesManager
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Workspace
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType
import org.elnix.dragonlauncher.icons.IconPackManager
import org.elnix.dragonlauncher.icons.IconService
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.recents.RecentsService
import org.elnix.dragonlauncher.workspaces.WorkspacesManager
import javax.inject.Inject


@HiltViewModel
class AppsViewModel @Inject constructor(
    application: android.app.Application,
    iconPackManager: IconPackManager,
    val appsRepository: AppRepository,
    private val recentsService: RecentsService,
    val iconsService: IconService,
    val workspaceManager: WorkspacesManager,
    val appOverrideManager: AppOverridesManager,
) : AndroidViewModel(application) {

    val allApps: Flow<ImmutableList<Application>> = appsRepository.getAllApps()

    fun isAppInstalled(packageName: String) : Flow<Boolean> {
        return allApps.map { apps ->
            apps.any { it.packageName == packageName }
        }
    }

    val iconPackList = iconPackManager.getInstalledIconPacks()
    val pointsIconsCache = iconsService.pointsIconsCache
    val drawerIconsCache = iconsService.drawerIconCache

    val packTint = iconsService.packTint
//    /**
//     * The list of icons available in the selected pack
//     */
//    private val _packIcons = MutableStateFlow<List<String>>(emptyList())
//    val packIcons: StateFlow<List<String>> = _packIcons.asStateFlow()

    private val _defaultPoint = MutableStateFlow(Point.defaultSwipePointsValues)
    val defaultPoint = _defaultPoint.asStateFlow()

    // Only used for preview, the real user apps getter are using the appsForWorkspace function
    val userApps: StateFlow<List<Application>> = allApps.map { list ->
        list.filter { it.isLaunchable && !it.isWork && !it.isSystem && !it.isPrivate }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


//    private val _selectedIconPack = MutableStateFlow<IconPackInfo?>(null)
//    val selectedIconPack: StateFlow<IconPackInfo?> = _selectedIconPack.asStateFlow()

//    private val iconPackCache = mutableMapOf<String, IconPackCache>()
//
//    private val pmCompat = PackageManagerCompat(ctx)





    val selectedWorkspaceId = workspaceManager.selectedWorkspaceId



    fun selectWorkspace(workspaceId: String) = workspaceManager.selectWorkspace(workspaceId)

    init {
        viewModelInitialized()
    }

//    /**
//     * Loads everything the AppViewModel needs
//     * Runs at start and when the user restore from a backup
//     */
//    suspend fun loadAll() = withContext(Dispatchers.IO) {
//
//        val savedPackTint = IconsSettingsStore.iconPackTint.get(ctx)
//        _packTint.value = savedPackTint.toArgb()
//
//
//        if (savedPackName.isNotBlank()) {
//            _selectedIconPack.value = _iconPacksList.value.find { it.packageName == savedPackName }
//        }
//
//        reloadApps()
//    }

    /**
     * Returns a filtered and sorted list of apps for the specified workspace as a reactive Flow.
     *
     * @param workspace The target workspace configuration defining app filtering rules
     * @param getOnlyAdded If true, returns ONLY apps explicitly added to this workspace [default: false]
     * @param getOnlyRemoved If true, returns ONLY apps hidden/removed from this workspace [default: false]
     * @return Flow of filtered, sorted, and resolved [Application] list
     *
     * @throws IllegalArgumentException if both [getOnlyAdded] and [getOnlyRemoved] are true
     *
     * @see WorkspaceType for base filtering behavior
     * @see AppOverride for override application details
     */
    fun appsForWorkspace(
        workspace: Workspace,
        getOnlyAdded: Boolean = false,
        getOnlyRemoved: Boolean = false
    ): StateFlow<List<Application>> {

        require(!(getOnlyAdded && getOnlyRemoved))

        val appIds = workspace.appIds ?: emptySet()
        val removedAppIds = workspace.removedAppIds ?: emptySet()

        return allApps.map { list ->
            when {
                getOnlyAdded -> list.filter { it.key in appIds }
                getOnlyRemoved -> list.filter { it.key in removedAppIds }
                else -> {
                    val base = when (workspace.type) {
                        WorkspaceType.ALL -> list
                        WorkspaceType.CUSTOM -> emptyList()
                        WorkspaceType.USER -> list.filter { !it.isWork && !it.isPrivate && it.isLaunchable }
                        WorkspaceType.SYSTEM -> list.filter { it.isSystem }
                        WorkspaceType.WORK -> list.filter { it.isWork && it.isLaunchable }
                        WorkspaceType.PRIVATE -> { list.filter { it.isPrivate && it.isLaunchable } }
                    }

                    val added = list.filter { it.key in appIds }


                    // Use the base list, and add the filtered manually-added apps, then remove explicitly removed ones
                    (base + added)
                        .distinctBy { it.key }
                        .filter { it.key !in removedAppIds }
                        .sortedBy { it.label.lowercase() }
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )
    }


//    suspend fun reloadApps() {
//        try {
//            val reloadAppsStartTime = System.currentTimeMillis()
//            logD(APPS_TAG) { "───────────── Starting reloadApps() ───────────── " }
//
//            val apps = withContext(Dispatchers.IO) {
//                pmCompat.getAllApps()
//            }
//
//            logD(APPS_TAG) { "Got allApps list in ${System.currentTimeMillis() - reloadAppsStartTime} ms, now handling them" }
//
//            val useDifferentialLoadingForPrivateSpace =
//                BehaviorSettingsStore.useDifferentialLoadingForPrivateSpace.get(ctx)
//
//            // Apply differential private-package marking if present
//            var finalApps = apps
//            if (useDifferentialLoadingForPrivateSpace) {
//                if (!pendingPrivateAssignments.isNullOrEmpty()) {
//                    val assignments = pendingPrivateAssignments ?: emptyMap()
//                    logI(APPS_TAG) {
//                        "Applying differential Private Space detection: ${assignments.size} app identities"
//                    }
//
//                    // Persist assignments
//                    try {
//                        val existingJson = PrivateAppsSettingsStore.jsonSetting.get(ctx)
//
//                        val existingMap: MutableMap<String, Int?> =
//                            if (existingJson.isNotBlankJson) mutableMapOf()
//                            else {
//                                json.decodeFromString<MutableMap<String, Int?>>(existingJson)
//                            }
//
//                        assignments.forEach { (identity, userId) ->
//                            existingMap[identity] = userId
//                        }
//
//                        PrivateAppsSettingsStore.jsonSetting.set(ctx, json.encodeToString(existingMap))
//                        logI(APPS_TAG) { "Persisted ${assignments.size} private app assignments" }
//                    } catch (e: Exception) {
//                        logE(APPS_TAG, e) { "Error persisting private package assignments" }
//                    }
//
//
//                    // Here's the hot logic, where the apps actually goes to the private space
//                    finalApps = apps.map { app ->
//                        val identity = app.key
//                        val cacheKeyString = identity.cacheKey
//
//                        val assignedUserId = assignments[cacheKeyString]
//                        if (assignedUserId != null || assignments.containsKey(cacheKeyString)) {
//                            logI(APPS_TAG) {
//                                "Marking ${app.packageName} as Private Space (diff), assigning userId=${assignedUserId ?: app.userId}"
//                            }
//                            app.copy(
//                                isPrivate = true,
//                                isWork = false,
//                                userId = assignedUserId ?: app.userId
//                            )
//                        } else app
//                    }
//                    // Clear pending after consumption
//                    pendingPrivateAssignments = null
//                } else {
//                    logI(APPS_TAG) { "Pending private assignments is empty : $pendingPrivateAssignments" }
//                }
//            }
//
//            logD(APPS_TAG) { "Total apps loaded: ${finalApps.size}" }
//            finalApps.count { it.isPrivate }.takeIf { it > 0 }?.let {
//                logD(APPS_TAG) { "Private apps: $it" }
//            }
//            finalApps.count { it.isWork }.takeIf { it > 0 }?.let {
//                logD(APPS_TAG) { "Work apps: $it" }
//            }
//            logD(APPS_TAG) { "User apps: ${finalApps.count { !it.isWork && !it.isPrivate }}" }
//
//
//            // Create new list to ensure StateFlow emission
//            _apps.value = finalApps.toList()
//            val appsSize = finalApps.size
//
//            /*  ─────────────  Apps Icons reloading  ─────────────  */
//            val appIconsReloadStartTime = System.currentTimeMillis()
//            _drawerIconsCache.updateMaxCacheSize(appsSize)
//
//            logI(ICONS_TAG) { "Updated apps-icons size; now = $appsSize" }
//            preloadAppIcons(finalApps, 128)
//
//            logD(APPS_TAG) { "Reloaded $appsSize app icons in ${System.currentTimeMillis() - appIconsReloadStartTime} ms" }
//
//
//            /*  ─────────────  Points Icons reloading  ─────────────  */
//            val pointsIconsReloadStartTime = System.currentTimeMillis()
//            val points = SwipeSettingsStore.getPoints(ctx)
//            val pointsSize = points.size
//
//            _pointsIconsCache.updateMaxCacheSize(pointsSize)
//            logI(ICONS_TAG) { "Updated point-icons size; now = $pointsSize" }
//
//            preloadPointIcons(points)
//            logD(APPS_TAG) { "Reloaded $pointsSize point icons in ${System.currentTimeMillis() - pointsIconsReloadStartTime} ms" }
//
//
//            val reloadAppsTotalTime = System.currentTimeMillis() - reloadAppsStartTime
//            logI(APPS_TAG) { "─────────────  Finished reloadApps(\uD83E\uDD0E) ($reloadAppsTotalTime ms) ─────────────\nReloaded ${apps.filter { it.isLaunchable == true }.size} launchable apps, ${apps.size} total apps" }
//
//        } catch (e: Exception) {
//            logE(APPS_TAG, e) { "Error in reloadApps" }
//        }
//    }


//
//    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
//    private suspend fun unlockPrivateSpace(): Boolean {
//
//        // Search for at leat one private workspace to avoid shouting the toast to new users
//        if (!ctx.isDefaultLauncher && _workspacesState.value.workspaces.find { it.type == WorkspaceType.PRIVATE }?.enabled ?: false) {
//            ctx.showToast(ctx.getString(R.string.need_to_be_default_launcher_to_use_private_space))
//            return false
//        }
//
//        val reallyLocked = withContext(Dispatchers.IO) {
//            PrivateSpaceUtils.isPrivateSpaceLocked(ctx) ?: true
//        }
//
//        if (!reallyLocked) {
//            _privateSpaceState.update {
//                it.copy(isLocked = false, isAuthenticating = false)
//            }
//            return true
//        }
//
//        _privateSpaceState.update { it.copy(isAuthenticating = true) }
//
//
//        // This only request the auth, it does not handle whether the private space was unlocked
//        PrivateSpaceUtils.requestUnlockPrivateSpace(ctx)
//
//        // Test with timeout the real unlock state
//        val unlocked = withTimeoutOrNull(10_000L) {
//            while (true) {
//                val locked = withContext(Dispatchers.IO) {
//                    PrivateSpaceUtils.isPrivateSpaceLocked(ctx) ?: true
//                }
//                if (!locked) break
//                delay(200)
//            }
//            true // return true when unlocked
//        } ?: false // if timeout
//
//
//        _privateSpaceState.update {
//            it.copy(
//                isAuthenticating = false,
//                isLocked = !unlocked
//            )
//        }
//
//        return unlocked
//    }


//    suspend fun unlockAndReloadPrivateSpace() {
//
//        if (!PrivateSpaceUtils.isPrivateSpaceSupported()) return
//
//        val useDifferentialLoadingForPrivateSpace =
//            BehaviorSettingsStore.useDifferentialLoadingForPrivateSpace.get(ctx)
//
//        if (useDifferentialLoadingForPrivateSpace) {
//            captureMainProfileSnapshotBeforeUnlock()
//        }
//
//        // Suspends until unlock, timeout or user cancel
//        val unlocked = unlockPrivateSpace()
//        if (!unlocked) return
//
//
//        // Set loading state before load
//        _privateSpaceState.update {
//            it.copy(
//                isLoading = true,
//            )
//        }
//
//        if (useDifferentialLoadingForPrivateSpace) {
//            detectPrivateAppsDiffAndReload()
//            logI(APPS_TAG) { "Available after full private space reload" }
//        } else {
//            reloadApps()
//            logI(APPS_TAG) { "Available after full apps reload (no differential reload)" }
//        }
//
//        // Finished loading
//        _privateSpaceState.update {
//            it.copy(
//                isLoading = false,
//            )
//        }
//    }

    fun cacheIconShape(iconShape: IconShape) = iconsService.cacheIconShape(iconShape)



//    /**
//     * Renders a [CustomIconSerializable] from a given orig [ImageBitmap]
//     * @param orig the base [ImageBitmap] that will be edited
//     * @param customIcon the custom icon to render with
//     * @param sizePx size of the output [ImageBitmap]
//     *
//     * @return [ImageBitmap] the rendered icon after customIcon process
//     */
//    private fun renderCustomIcon(
//        orig: ImageBitmap,
//        customIcon: CustomIconSerializable,
//        sizePx: Int
//    ): ImageBitmap {
//
//        val base: ImageBitmap =
//            if (customIcon.type == IconType.ICON_PACK) {
//
//                val source = customIcon.source
//
//                if (!source.isNullOrBlank() && ',' in source) {
//
//                    val (drawableName, packPkg) = source.split(',', limit = 2)
//
//                    loadIconFromPack(
//                        packPkg = packPkg,
//                        iconName = drawableName,
//                        targetPkg = "" // Manual selection
//                    )?.let { drawable ->
//                        loadDrawableAsBitmap(
//                            drawable = drawable,
//                            width = sizePx,
//                            height = sizePx,
//                            tint = packTint.value
//                        )
//                    } ?: orig
//
//                } else orig
//
//            } else orig
//
//        return resolveCustomIconBitmap(
//            base = base,
//            icon = customIcon,
//            sizePx = sizePx,
//            density = _density,
//            iconShape = _iconShape.value ?: IconShape.PlatformDefault,
//        )
//    }
//
//
//    /*  ────── THE MOST IMPORTANT FUNCTIONS BELOW, LOAD ALL ICONS ──────  */
//
//    /**
//     * Loads and renders the visual icon for a given swipe point.
//     *
//     * The final icon is computed in three steps:
//     *
//     * 1. Resolve the effective size in pixels, based on:
//     *    - The global default point size
//     *    - The point-specific size override (if any)
//     *    - The current screen density
//     *
//     * 2. Create the base (untinted) bitmap for the point's action.
//     *    If the action represents an app, the corresponding app icon is used.
//     *
//     * 3. Apply custom icon styling (shape, tint, etc.) and render
//     *    the final [ImageBitmap].
//     *
//     * If the point does not define a custom shape, the current global
//     * icon shape setting is applied.
//     *
//     * @param point The [SwipePointSerializable] containing action and optional
//     *              custom icon configuration.
//     *
//     * @return The fully rendered [ImageBitmap] ready for drawing.
//     */
//    private fun loadPointIcon(point: SwipePointSerializable): ImageBitmap {
//
//        val resolvedResolutionDp =
//            point.resolution ?: _defaultPoint.value.resolution
//            ?: point.size ?: _defaultPoint.value.size
//            ?: SwipePointSerializable.defaultSwipePointsValues.size!!
//
//        // Convert dp to pixels and enforce a minimum touch-safe size.
//        val sizePx = (resolvedResolutionDp * _density.density)
//            .toInt()
//
//        // Create the base untinted bitmap from the action.
//        val baseBitmap = createUntintedBitmap(
//            action = point.action,
//            ctx = ctx,
//            icons = _drawerIconsCache,
//            width = sizePx,
//            height = sizePx
//        )
//
//        // Resolve custom icon configuration:
//        // - If the point defines a shape, use it as-is.
//        // - Otherwise apply the current global icon shape.
//        val effectiveCustomIcon =
//            if (point.customIcon?.shape != null) {
//                point.customIcon!!
//            } else {
//                (point.customIcon ?: CustomIconSerializable()).copy(
//                    shape = _iconShape.value
//                )
//            }
//
//        // Render and return the final styled bitmap.
//        return renderCustomIcon(
//            orig = baseBitmap,
//            customIcon = effectiveCustomIcon,
//            sizePx = sizePx
//        )
//    }

//
//    // DO a single function to load icons instead of 2 separated and shitty
//    // No, in fact they are working and well now, no need to change
//
//    private fun loadSingleIcon(
//        app: Application,
//        customIcon: CustomIconSerializable?,
//        sizePx: Int
//    ): ImageBitmap {
//        val packageName = app.packageName
//        val userId = app.userId
//        val isPrivate = app.isPrivate
//        val cacheKey = app.key
//
//        var isIconPack = false
//        val packIconName = getCachedIconMapping(packageName)
//        val selectedPack = selectedIconPack.value
//
//        val drawable =
//            if (selectedPack != null) {
//                packIconName?.let { packName ->
//                    isIconPack = true
//                    loadIconFromPack(
//                        packPkg = selectedPack.packageName,
//                        iconName = packName,
//                        targetPkg = packageName
//                    )
//                }
//            } else {
//                null
//            } ?: run {
//                isIconPack = false
//                pmCompat.getAppIcon(packageName, userId ?: 0, isPrivate)
//            }
//
//
//        val orig = loadDrawableAsBitmap(
//            drawable = drawable,
//            width = sizePx,
//            height = sizePx,
//            tint = _packTint.value.takeIf { isIconPack }
//        )
//
//        logD(ICONS_TAG) { "Icon loaded for $cacheKey: $orig" }
//
//        val customIcon = customIcon ?: _workspacesState.value.appOverrides[cacheKey]?.customIcon
//
//        if (customIcon != null) {
//            return renderCustomIcon(
//                orig = orig,
//                customIcon = customIcon,
//                sizePx = sizePx
//            ).also {
//                logD(ICONS_TAG) { "Custom Icon rendered for $cacheKey: $it" }
//            }
//        }
//
//        return orig
//    }
//
//
//    /* ───────────── Reload Functions ───────────── */
//
//    /**
//     * Reload a single point icon to the icons list, override if already existing
//     *
//     * @param point which point's icon to load
//     */
//    fun reloadPointIcon(point: SwipePointSerializable) {
//        _pointsIconsCache.compute(point.id) {
//            loadPointIcon(point)
//        }
//    }
//
//    /**
//     * Update single icon (for app)
//     * Basically the same thing as [reloadPointIcon] but for an AppModel instead of the [SwipePointSerializable] you input an [AppModel]
//     *
//     * @param app
//     */
//    fun reloadAppIcon(
//        app: AppModel,
//        customIcon: CustomIconSerializable? = null,
//        sizePx: Int = 128
//    ) {
//        _drawerIconsCache.compute(app.key) {
//            loadSingleIcon(
//                app = app,
//                customIcon = customIcon,
//                sizePx = sizePx
//            )
//        }
//    }


//    /* ───────────── Multiple Load Functions ───────────── */
//
//
//    /**
//     * Preload a given list of point icons asynchronously and per icon updates the icons list
//     *
//     * @param points which points to load
//     */
//    fun preloadPointIcons(points: List<SwipePointSerializable>) {
//        logI(ICONS_TAG) { "Loading ${points.size} points icons" }
//
//        viewModelScope.launch(Dispatchers.IO) {
//            points.forEach { p ->
//                iconSemaphore.withPermit {
//                    reloadPointIcon(p)
//                }
//            }
//        }
//    }
//
//    /**
//     * Load app icons from a list of [AppModel]
//     *
//     * @param apps list of app icons to load
//     * @param sizePx size of the loaded [ImageBitmap]
//     */
//    private suspend fun preloadAppIcons(
//        apps: List<AppModel>,
//        sizePx: Int
//    ) = withContext(Dispatchers.IO) {
//        logI(ICONS_TAG) { "Loading ${apps.size} app icons..." }
//
//        apps.forEach { app ->
//            iconSemaphore.withPermit {
//                reloadAppIcon(
//                    app = app,
//                    customIcon = null,
//                    sizePx = sizePx
//                )
//            }
//        }
//    }


//    /**
//     * Loads a drawable from the specified icon pack using a resolved drawable name.
//     *
//     * The function attempts to resolve the provided [iconName] as a `drawable`
//     * resource within the icon pack identified by [packPkg]. If a matching
//     * resource is found, it is returned as a [Drawable].
//     *
//     * This method assumes that the correct drawable name has already been
//     * determined (e.g., via app-filter mapping or manual naming strategy).
//     * No additional fallback logic is performed here.
//     *
//     * @param packPkg Package name of the icon pack. If `null`, the function
//     *                returns `null` immediately.
//     * @param iconName Name of the drawable resource inside the icon pack.
//     * @param targetPkg Package name of the target application (used for logging/debugging).
//     *
//     * @return The resolved [Drawable] if found, or `null` if the drawable
//     *         resource does not exist in the icon pack.
//     */
//    @SuppressLint("DiscouragedApi")
//    fun loadIconFromPack(
//        packPkg: String?,
//        iconName: String,
//        targetPkg: String
//    ): Drawable? {
//
//        logD(ICONS_TAG) { "Resolving icon → app=$targetPkg pack=$packPkg resolvedName=$iconName" }
//
//        if (packPkg == null) return null
//
//        val packResources = try {
//            ctx.packageManager.getResourcesForApplication(packPkg)
//        } catch (e: Exception) {
//            logE(ICONS_TAG, e) { "Error fetching pack ressources: app is likely not installed" }
//            null
//        }
//
//        packResources?.let {
//            try {
//                val drawableId = packResources.getIdentifier(iconName, "drawable", packPkg)
//
//                logD(ICONS_TAG) { "Trying drawable: name=$iconName id=$drawableId" }
//                if (drawableId != 0) {
//                    return ResourcesCompat.getDrawable(packResources, drawableId, null)
//                }
//                logW(ICONS_TAG) { "drawableId is 0, wtf?" }
//
//            } catch (e: Exception) {
//                logE(ICONS_TAG, e) { "Error fetching pack drawable ressources" }
//            }
//        }
//
//        return null
//    }


//    /**
//     * Load all icons mappings from pack, used to display the picker list when user picks
//     * a certain icon from the pack
//     *
//     * Doesn't load the actual icons, but their names which is cheaper and faster
//     * the rendering is handled by the UI level IconPickerListDialog  (not accessible in this viewModelScope)
//     *
//     * @param pack the icon pack from where to load
//     */
//    fun loadAllIconsMappingsFromPack(pack: IconPackInfo) {
//
//        viewModelScope.launch(Dispatchers.IO) {
//            val cache = iconPackCache.getOrPut(pack.packageName) {
//                loadIconPackMappings(pack.packageName)
//            }
//
//            if (cache.pkgToDrawables.isEmpty()) {
//                _packIcons.value = emptyList()
//                return@launch
//            }
//
//            _packIcons.value = cache.pkgToDrawables.values.flatten().distinct()
//        }
//    }
//
//    /**
//     * Retrieves a cached icon mapping for the given application package.
//     *
//     * This method checks the currently selected icon pack for a drawable
//     * mapping corresponding to [pkgName]. It first attempts an exact
//     * component-level match using the app's launch intent. If no exact match
//     * is found, it falls back to a package-level match.
//     *
//     * The result is cached in [IconPackCache] to avoid repeatedly parsing
//     * icon pack resources.
//     *
//     * @param pkgName The package name of the target application.
//     *
//     * @return The drawable name from the icon pack if a mapping exists,
//     *         or `null` if no mapping is found.
//     */
//    private fun getCachedIconMapping(pkgName: String): String? {
//        val pack = selectedIconPack.value ?: return null
//        val cache = getCache(pack.packageName)
//
//        logD(ICONS_TAG) { "getCachedIconMapping → app=$pkgName pack=${pack.packageName}" }
//
//        val launchIntent = runCatching {
//            pm.getLaunchIntentForPackage(pkgName)
//        }.getOrNull()
//
//        val component = launchIntent?.component?.let {
//            normalizeComponent("${it.packageName}/${it.className}")
//        }
//
//        // Exact component match (best case)
//        component?.let {
//            cache.componentToDrawable[it]?.let { drawable ->
//                return drawable
//            }
//        }
//
//        // Package-level match
//        cache.pkgToDrawables[pkgName]?.firstOrNull()?.let {
//            return it
//        }
//
//        logD(ICONS_TAG) { "No mapping found for $pkgName" }
//        return null
//    }


//    fun selectIconPack(pack: IconPackInfo) {
//        _selectedIconPack.value = pack
//        viewModelScope.launch(Dispatchers.IO) {
//            UiSettingsStore.selectedIconPack.set(ctx, pack.packageName)
//            reloadApps()
//        }
//    }
//
//    private fun getCache(packPkg: String): IconPackCache {
//        return iconPackCache[packPkg]
//            ?: loadIconPackMappings(packPkg).also {
//                iconPackCache[packPkg] = it
//            }
//    }

//
//    fun clearIconPack() {
//        _selectedIconPack.value = null
//        viewModelScope.launch(Dispatchers.IO) {
//            UiSettingsStore.selectedIconPack.reset(ctx)
//            reloadApps()
//        }
//    }
//
//
//    fun loadIconPacks() {
//        val packs = mutableListOf<IconPackInfo>()
//        val allPackages = pmCompat.getInstalledPackages()
//
//
//        allPackages.forEach { pkgInfo ->
//            if (pkgInfo.packageName == ctx.packageName) return@forEach
//
//            try {
//                val packResources = pmCompat.getResourcesForApplication(pkgInfo.packageName)
//                val hasAppfilter = hasStandardAppFilter(packResources)
//
//                if (hasAppfilter) {
//                    val name = pkgInfo.applicationInfo?.loadLabel(pm).toString()
//                    logD(ICONS_TAG) {
//                        "FOUND icon pack: $name (${pkgInfo.packageName}"
//                    }
//
//                    packs.add(
//                        IconPackInfo(
//                            packageName = pkgInfo.packageName,
//                            name = name
//                        )
//                    )
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//        val uniquePacks = packs.distinctBy { it.packageName }
//        logD(ICONS_TAG) { "Total icon packs found: ${uniquePacks.size}" }
//        _iconPacksList.value = uniquePacks
//    }

//    fun loadIconPackMappings(packPkg: String): IconPackCache {
//        return try {
//            val entries = parseAppFilterXml(ctx, packPkg) ?: emptyList()
//
//            val componentToDrawable = mutableMapOf<String, String>()
//            val pkgToDrawables = mutableMapOf<String, MutableList<String>>()
//
//            entries.forEach { mapping ->
//                val normalized = normalizeComponent(mapping.component)
//                val pkg = normalized.substringBefore('/')
//
//                componentToDrawable[normalized] = mapping.drawable
//
//                val list = pkgToDrawables.getOrPut(pkg) { mutableListOf() }
//                if (!list.contains(mapping.drawable)) {
//                    list.add(mapping.drawable)
//                }
//            }
//
//            IconPackCache(
//                pkgToDrawables = pkgToDrawables,
//                componentToDrawable = componentToDrawable
//            )
//        } catch (e: Exception) {
//            logE(ICONS_TAG, e) { "Failed to load mappings for $packPkg" }
//            IconPackCache(emptyMap(), emptyMap())
//        }
//    }


//    private fun normalizeComponent(raw: String): String {
//        var comp = raw
//
//        if (comp.contains('{')) comp = comp.substringAfter('{')
//        if (comp.contains('}')) comp = comp.substringBefore('}')
//        comp = comp.trim()
//
//        if (!comp.contains('/')) return comp
//
//        val pkg = comp.substringBefore('/')
//        var cls = comp.substringAfter('/')
//
//        if (cls.startsWith(".")) {
//            cls = pkg + cls
//        }
//
//        return "$pkg/$cls"
//    }
}



///**
// * Checks whether the given [Resources] instance contains an `appfilter.xml`
// * inside the `assets/` directory.
// *
// * This is used as a lightweight heuristic to detect traditional icon packs
// * that ship a standard `appfilter.xml` file in their assets folder.
// *
// * Note:
// * - Some icon packs place `appfilter.xml` under `res/xml/` instead of `assets/`.
// * - A `false` result does not guarantee that no app filter exists, only that
// *   it was not found in the `assets` directory.
// *
// * @param res Resources of the icon pack application.
// * @return `true` if `assets/appfilter.xml` can be opened successfully,
// *         `false` otherwise.
// */
//private fun hasStandardAppFilter(res: Resources): Boolean {
//    return try {
//        res.assets.open("appfilter.xml").use { true }
//    } catch (e: Exception) {
//        e.printStackTrace()
//        false
//    }
//}
//
///**
// * Attempts to parse icon mappings from an icon pack's `appfilter.xml`.
// *
// * The function tries both common locations used by icon packs:
// *
// * 1. `assets/appfilter.xml`
// * 2. `res/xml/appfilter.xml`
// *
// * If mappings are successfully parsed from the first location, the second
// * is not attempted. If neither location yields valid mappings, `null`
// * is returned.
// *
// * This supports both traditional icon packs and variations that place
// * the filter file in different locations.
// *
// * @param ctx Context used to obtain the target application's resources.
// * @param packPkg Package name of the icon pack.
// * @return A list of [IconMapping] entries if parsing succeeds,
// *         or `null` if no valid `appfilter.xml` could be found or parsed.
// */
//@SuppressLint("DiscouragedApi")
//private fun parseAppFilterXml(ctx: Context, packPkg: String): List<IconMapping>? {
//    val packResources = ctx.packageManager.getResourcesForApplication(packPkg)
//    var mappings: List<IconMapping>? = null
//
//    // 1. Try assets/appfilter.xml first
//    try {
//        packResources.assets.open("appfilter.xml").use { input ->
//            val parser = Xml.newPullParser()
//            parser.setInput(input.reader())
//            mappings = parseXml(parser)
//        }
//        if (mappings?.isNotEmpty() == true) {
//            logD(ICONS_TAG) { "Loaded ${mappings.size} mappings from assets/appfilter.xml" }
//            return mappings
//        }
//    } catch (_: Exception) {
//        logD(ICONS_TAG) { "Assets appfilter.xml failed" }
//    }
//
//    // 2. Fallback to res/xml/appfilter.xml
//    val resId = packResources.getIdentifier("appfilter", "xml", packPkg)
//    if (resId == 0) return null
//
//    try {
//        val parser: XmlResourceParser = packResources.getXml(resId)
//        mappings = parseXml(parser)
//        logD(ICONS_TAG) { "Loaded ${mappings.size} mappings from res/xml/appfilter.xml" }
//    } catch (e: Exception) {
//        logE(ICONS_TAG, e) { "res/xml/appfilter.xml parse failed" }
//    }
//
//    return mappings
//}
//
///**
// * Parses an `appfilter.xml` document and extracts icon mapping entries.
// *
// * The parser scans for `<item>` tags and reads:
// * - `component` or `activity` attribute (component name)
// * - `drawable` attribute (icon resource name)
// *
// * Each valid pair is converted into an [IconMapping] and added to the result list.
// * Entries missing required attributes are ignored.
// *
// * @param parser An initialized [XmlPullParser] positioned at the start of
// *               an `appfilter.xml` document.
// * @return A list of parsed [IconMapping] objects. The list may be empty
// *         if no valid `<item>` entries are found.
// */
//private fun parseXml(parser: XmlPullParser): List<IconMapping> {
//    val mappings = mutableListOf<IconMapping>()
//    var eventType = parser.eventType
//    while (eventType != XmlPullParser.END_DOCUMENT) {
//        if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
//            val component = parser.getAttributeValue(null, "component") ?: parser.getAttributeValue(
//                null,
//                "activity"
//            )
//            val drawable = parser.getAttributeValue(null, "drawable")
//            if (!component.isNullOrEmpty() && !drawable.isNullOrEmpty()) {
//                mappings.add(IconMapping(component, drawable))
//            }
//        }
//        eventType = parser.next()
//    }
//    return mappings
//}
//
///**
// * Icon pack cache with normalized component mapping and package -> drawables list
// */
//data class IconPackCache(
//    val pkgToDrawables: Map<String, List<String>>,
//    val componentToDrawable: Map<String, String>
//)
