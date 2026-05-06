package org.elnix.dragonlauncher.models

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Xml
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.serializables.AppModel
import org.elnix.dragonlauncher.common.serializables.AppOverride
import org.elnix.dragonlauncher.common.serializables.CacheKey
import org.elnix.dragonlauncher.common.serializables.CustomIconSerializable
import org.elnix.dragonlauncher.common.serializables.IconMapping
import org.elnix.dragonlauncher.common.serializables.IconPackInfo
import org.elnix.dragonlauncher.common.serializables.IconShape
import org.elnix.dragonlauncher.common.serializables.IconType
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable
import org.elnix.dragonlauncher.common.serializables.Workspace
import org.elnix.dragonlauncher.common.serializables.WorkspaceState
import org.elnix.dragonlauncher.common.serializables.WorkspaceType
import org.elnix.dragonlauncher.common.serializables.defaultWorkspaces
import org.elnix.dragonlauncher.common.serializables.resolveApp
import org.elnix.dragonlauncher.common.serializables.splitCacheKey
import org.elnix.dragonlauncher.common.utils.ConnectivityUtils.isDefaultLauncher
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.APPS_TAG
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.BROADCAST_TAG
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.ICONS_TAG
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.WORKSPACES_TAG
import org.elnix.dragonlauncher.common.messyfolder.IconsCache
import org.elnix.dragonlauncher.common.utils.ImageUtils.createUntintedBitmap
import org.elnix.dragonlauncher.common.utils.ImageUtils.loadDrawableAsBitmap
import org.elnix.dragonlauncher.common.utils.ImageUtils.resolveCustomIconBitmap
import org.elnix.dragonlauncher.common.messyfolder.PackageManagerCompat
import org.elnix.dragonlauncher.common.utils.PrivateSpaceUtils
import org.elnix.dragonlauncher.common.messyfolder.isNotBlankJson
import org.elnix.dragonlauncher.common.messyfolder.showToast
import org.elnix.dragonlauncher.enumsui.other.PrivateSpaceLoadingState
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logI
import org.elnix.dragonlauncher.logging.logW
import org.elnix.dragonlauncher.models.sinleton.AppsRepository
import org.elnix.dragonlauncher.settings.stores.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.settings.stores.PrivateAppsSettingsStore
import org.elnix.dragonlauncher.settings.stores.SwipeSettingsStore
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore
import org.elnix.dragonlauncher.settings.stores.WorkspaceSettingsStore
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import javax.inject.Inject


@HiltViewModel
class AppsViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {

    private val _apps = MutableStateFlow<List<AppModel>>(emptyList())
    val allApps: StateFlow<List<AppModel>> = _apps.asStateFlow()

    private val _iconPacksList = MutableStateFlow<List<IconPackInfo>>(emptyList())
    val iconPacksList = _iconPacksList.asStateFlow()


    /**
     * The list of icons available in the selected pack
     */
    private val _packIcons = MutableStateFlow<List<String>>(emptyList())
    val packIcons: StateFlow<List<String>> = _packIcons.asStateFlow()

    private val _packTint = MutableStateFlow<Int?>(null)
    val packTint = _packTint.asStateFlow()

    private val json = Json {
        allowStructuredMapKeys = true
    }

    /**
     * Drawer icons cache, initialize at 100 apps
     */
    private val _drawerIconsCache = IconsCache<CacheKey>(100)
    val drawerIconCache = _drawerIconsCache

    private val _pointsIconsCache = IconsCache<String>(100)
    val pointsIconsCache = _pointsIconsCache


    private val _defaultPoint = MutableStateFlow(SwipePointSerializable.defaultSwipePointsValues)
    val defaultPoint = _defaultPoint.asStateFlow()

    // Only used for preview, the real user apps getter are using the appsForWorkspace function
    val userApps: StateFlow<List<AppModel>> = _apps.map { list ->
        list.filter { it.isLaunchable == true && !it.isWorkProfile && !it.isSystem }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


    private val _selectedIconPack = MutableStateFlow<IconPackInfo?>(null)
    val selectedIconPack: StateFlow<IconPackInfo?> = _selectedIconPack.asStateFlow()

    private val iconPackCache = mutableMapOf<String, IconPackCache>()

    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext

    private val pm: PackageManager = application.packageManager
    private val pmCompat = PackageManagerCompat(pm, ctx)

    /**
     * Used to correctly dispatch the heavy background load, as long as I understand
     */
    private val iconSemaphore = Semaphore(4)


    /* ───────────── Workspace things ───────────── */
    private val _workspacesState = MutableStateFlow(
        WorkspaceState()
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
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = WorkspaceState()
        )


    private val _selectedWorkspaceId = MutableStateFlow("user")
    val selectedWorkspaceId: StateFlow<String> = _selectedWorkspaceId.asStateFlow()

    /* ───────────── Recently Used Apps ───────────── */
    private val _recentlyUsedPackages = MutableStateFlow<List<String>>(emptyList())

    init {
        viewModelScope.launch {
            _selectedWorkspaceId.value = DrawerSettingsStore.lastWorkspaceUsed.get(ctx)
        }

        // Receive package receiver events to trigger a app reload (app installed, etc..)
        viewModelScope.launch {
            AppsRepository.reloadTrigger.collect { event ->
                logI(BROADCAST_TAG) { "Received $event from AppsRepository, reloading apps!" }
                reloadApps()
            }
        }
    }

    /**
     * Loads everything the AppViewModel needs
     * Runs at start and when the user restore from a backup
     */
    suspend fun loadAll() = withContext(Dispatchers.IO) {
        // Parallel loading of non-dependent data
        val workspacesJob = launch { loadWorkspaces() }
        val recentAppsJob = launch { loadRecentlyUsedApps() }
        val iconPacksJob = launch { loadIconPacks() }

        val savedPackTint = UiSettingsStore.iconPackTint.get(ctx)
        _packTint.value = savedPackTint.toArgb()

        val savedPackName = UiSettingsStore.selectedIconPack.get(ctx)

        // Wait for basic config before proceeding to icons/apps
        workspacesJob.join()
        recentAppsJob.join()
        iconPacksJob.join()

        if (savedPackName.isNotBlank()) {
            _selectedIconPack.value = _iconPacksList.value.find { it.packageName == savedPackName }
        }

        reloadApps()
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
     *
     */
    fun appsForWorkspace(
        workspace: Workspace,
        overrides: Map<CacheKey, AppOverride>,
        getOnlyAdded: Boolean = false,
        getOnlyRemoved: Boolean = false
    ): StateFlow<List<AppModel>> {

        require(!(getOnlyAdded && getOnlyRemoved))

        // May be null cause I added the removed app ids lately, so some user may still have the old app model without it
        val removed = workspace.removedAppIds ?: emptyList()

        return _apps.map { list ->
            when {
                getOnlyAdded -> list.filter { it.iconCacheKey in workspace.appIds }
                getOnlyRemoved -> list.filter { it.iconCacheKey in removed }
                else -> {
                    val base = when (workspace.type) {
                        WorkspaceType.ALL -> list
                        WorkspaceType.CUSTOM -> emptyList()
                        WorkspaceType.USER -> list.filter { !it.isWorkProfile && !it.isPrivateProfile && it.isLaunchable == true }
                        WorkspaceType.SYSTEM -> list.filter { it.isSystem }
                        WorkspaceType.WORK -> list.filter { it.isWorkProfile && it.isLaunchable == true }
                        WorkspaceType.PRIVATE -> {
                            val privateApps =
                                list.filter { it.isPrivateProfile && it.isLaunchable == true }
                            logI(APPS_TAG) {
                                "Private workspace filter: ${privateApps.size} apps from ${list.size} total (${list.count { it.isPrivateProfile }} private in total)"
                            }
                            if (privateApps.isNotEmpty()) {
                                logI(APPS_TAG) {
                                    "Private apps: ${privateApps.joinToString(", ") { it.name }}"
                                }
                            }
                            privateApps
                        }
                    }

                    val added = list.filter { it.iconCacheKey in workspace.appIds }


                    // Use the base list, and add the filtered manually-added apps, then remove explicitly removed ones
                    (base + added)
                        .distinctBy { it.iconCacheKey }
                        .filter { it.iconCacheKey !in removed }
                        .sortedBy { it.name.lowercase() }
                        .map { resolveApp(it, overrides) }
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )
    }


    /**
     * Reloads apps fresh from PackageManager.
     * Saves updated list into DataStore.
     * This is used by the BroadcastReceiver.
     */
    // Snapshot / differential fields for Private Space detection
    private var privateSnapshotBefore: Set<String>? = null
    private var pendingPrivateAssignments: Map<String, Int?>? = null


    /**
     * _private space state, describe the current state of the private space
     * reflected in UI in the AppDrawerScreen and PrivateSpaceUnlockScreen
     */
    private val _privateSpaceState = MutableStateFlow(
        PrivateSpaceLoadingState(
            isLocked = true,
            isLoading = false,
            isAuthenticating = false
        )
    )
    val privateSpaceState = _privateSpaceState.asStateFlow()

    fun setPrivateSpaceLocked() {
        _privateSpaceState.update {
            it.copy(
                isLocked = true,
                isLoading = false,
                isAuthenticating = false
            )
        }
    }

    // Debounce / coalesce reloads
    private var scheduledReloadJob: Job? = null
    private val reloadMutex = Mutex()


    suspend fun reloadApps() {
        try {
            val reloadAppsStartTime = System.currentTimeMillis()
            logD(APPS_TAG) { "───────────── Starting reloadApps() ───────────── " }

            val apps = withContext(Dispatchers.IO) {
                pmCompat.getAllApps()
            }

            logD(APPS_TAG) { "Got allApps list in ${System.currentTimeMillis() - reloadAppsStartTime} ms, now handling them" }

            val useDifferentialLoadingForPrivateSpace =
                BehaviorSettingsStore.useDifferentialLoadingForPrivateSpace.get(ctx)

            // Apply differential private-package marking if present
            var finalApps = apps
            if (useDifferentialLoadingForPrivateSpace) {
                if (!pendingPrivateAssignments.isNullOrEmpty()) {
                    val assignments = pendingPrivateAssignments ?: emptyMap()
                    logI(APPS_TAG) {
                        "Applying differential Private Space detection: ${assignments.size} app identities"
                    }

                    // Persist assignments
                    try {
                        val existingJson = PrivateAppsSettingsStore.jsonSetting.get(ctx)

                        val existingMap: MutableMap<String, Int?> =
                            if (existingJson.isNotBlankJson) mutableMapOf()
                            else {
                                json.decodeFromString<MutableMap<String, Int?>>(existingJson)
                            }

                        assignments.forEach { (identity, userId) ->
                            existingMap[identity] = userId
                        }

                        PrivateAppsSettingsStore.jsonSetting.set(ctx, json.encodeToString(existingMap))
                        logI(APPS_TAG) { "Persisted ${assignments.size} private app assignments" }
                    } catch (e: Exception) {
                        logE(APPS_TAG, e) { "Error persisting private package assignments" }
                    }


                    // Here's the hot logic, where the apps actually goes to the private space
                    finalApps = apps.map { app ->
                        val identity = app.iconCacheKey
                        val cacheKeyString = identity.cacheKey

                        val assignedUserId = assignments[cacheKeyString]
                        if (assignedUserId != null || assignments.containsKey(cacheKeyString)) {
                            logI(APPS_TAG) {
                                "Marking ${app.packageName} as Private Space (diff), assigning userId=${assignedUserId ?: app.userId}"
                            }
                            app.copy(
                                isPrivateProfile = true,
                                isWorkProfile = false,
                                userId = assignedUserId ?: app.userId
                            )
                        } else app
                    }
                    // Clear pending after consumption
                    pendingPrivateAssignments = null
                } else {
                    logI(APPS_TAG) { "Pending private assignments is empty : $pendingPrivateAssignments" }
                }
            }

            logD(APPS_TAG) { "Total apps loaded: ${finalApps.size}" }
            finalApps.count { it.isPrivateProfile }.takeIf { it > 0 }?.let {
                logD(APPS_TAG) { "Private apps: $it" }
            }
            finalApps.count { it.isWorkProfile }.takeIf { it > 0 }?.let {
                logD(APPS_TAG) { "Work apps: $it" }
            }
            logD(APPS_TAG) { "User apps: ${finalApps.count { !it.isWorkProfile && !it.isPrivateProfile }}" }


            // Create new list to ensure StateFlow emission
            _apps.value = finalApps.toList()
            val appsSize = finalApps.size

            /*  ─────────────  Apps Icons reloading  ─────────────  */
            val appIconsReloadStartTime = System.currentTimeMillis()
            _drawerIconsCache.updateMaxCacheSize(appsSize)

            logI(ICONS_TAG) { "Updated apps-icons size; now = $appsSize" }
            preloadAppIcons(finalApps, 128)

            logD(APPS_TAG) { "Reloaded $appsSize app icons in ${System.currentTimeMillis() - appIconsReloadStartTime} ms" }


            /*  ─────────────  Points Icons reloading  ─────────────  */
            val pointsIconsReloadStartTime = System.currentTimeMillis()
            val points = SwipeSettingsStore.getPoints(ctx)
            val pointsSize = points.size

            _pointsIconsCache.updateMaxCacheSize(pointsSize)
            logI(ICONS_TAG) { "Updated point-icons size; now = $pointsSize" }

            preloadPointIcons(points)
            logD(APPS_TAG) { "Reloaded $pointsSize point icons in ${System.currentTimeMillis() - pointsIconsReloadStartTime} ms" }


            val reloadAppsTotalTime = System.currentTimeMillis() - reloadAppsStartTime
            logI(APPS_TAG) { "─────────────  Finished reloadApps(\uD83E\uDD0E) ($reloadAppsTotalTime ms) ─────────────\nReloaded ${apps.filter { it.isLaunchable == true }.size} launchable apps, ${apps.size} total apps" }

        } catch (e: Exception) {
            logE(APPS_TAG, e) { "Error in reloadApps" }
        }
    }

    /**
     * Differential Private Space detection helpers
     */
    private suspend fun captureMainProfileSnapshotBeforeUnlock() {
        try {
            logD(APPS_TAG) { "Capturing visible app snapshot before Private Space unlock..." }
            privateSnapshotBefore = withContext(Dispatchers.IO) {
                pmCompat.getAllApps(skipAnyKnownPrivate = true)
                    .filter { it.isLaunchable == true }
                    .map { it.iconCacheKey.cacheKey }
                    .toSet()
            }
            logD(APPS_TAG) { "Snapshot captured: ${privateSnapshotBefore?.size ?: 0} packages" }
        } catch (e: Exception) {
            logE(APPS_TAG, e) { "Error capturing main profile snapshot" }
            privateSnapshotBefore = null
        }
    }

    private suspend fun detectPrivateAppsDiffAndReload() {
        try {
            logD(APPS_TAG) { "Detecting Private Space apps via differential snapshot..." }
            val before = privateSnapshotBefore ?: emptySet()
            val afterApps = withContext(Dispatchers.IO) {
                pmCompat.getAllApps().filter { it.isLaunchable == true }
            }

            val after = afterApps.map { it.iconCacheKey.cacheKey }.toSet()
            val diffKeys = after.subtract(before)
            val diffApps = afterApps.filter { it.iconCacheKey.cacheKey in diffKeys }

            logI(APPS_TAG) {
                "Differential detection: found ${diffApps.size} candidate private apps: ${
                    diffApps.joinToString(", ") { "${it.packageName}@${it.userId}" }
                }"
            }

            pendingPrivateAssignments =
                diffApps.associate { it.iconCacheKey.cacheKey to it.userId }

            // Remove any of these packages from USER workspaces (they belong to Private)
            try {
                val userWorkspaces =
                    _workspacesState.value.workspaces.filter { it.type == WorkspaceType.USER }
                diffApps.distinct().forEach { app ->
                    val cacheKey = app.iconCacheKey

                    userWorkspaces.forEach { ws ->
                        if (cacheKey in ws.appIds) {
                            logI(APPS_TAG) {
                                "Removing $cacheKey from USER workspace (${ws.id}) because it's Private"
                            }
                            removeAppFromWorkspace(ws.id, cacheKey)
                        }
                    }
                }
            } catch (e: Exception) {
                logE(APPS_TAG, e) { "Error removing packages from USER workspaces" }
            }

            // Clear the before snapshot
            privateSnapshotBefore = null

            // Start Private Space loading state and schedule a debounced reload
            _privateSpaceState.update {
                it.copy(
                    isLoading = true,
                )
            }

            // Schedule reload (debounced) and wait for it to complete
            scheduledReloadJob?.cancel()
            scheduledReloadJob = viewModelScope.launch {
                delay(300) // short debounce to coalesce multiple triggers
                reloadMutex.withLock {
                    reloadApps()
                }
            }
            scheduledReloadJob?.join()

        } catch (e: Exception) {
            logE(APPS_TAG, e) { "Error during differential private detection" }
            pendingPrivateAssignments = null
            privateSnapshotBefore = null

            setPrivateSpaceLocked()

            // best-effort fallback: full reload
            reloadApps()
        }
    }


    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private suspend fun unlockPrivateSpace(): Boolean {

        // Search for at leat one private workspace to avoid shouting the toast to new users
        if (!ctx.isDefaultLauncher && _workspacesState.value.workspaces.find { it.type == WorkspaceType.PRIVATE }?.enabled ?: false) {
            ctx.showToast(ctx.getString(R.string.need_to_be_default_launcher_to_use_private_space))
            return false
        }

        val reallyLocked = withContext(Dispatchers.IO) {
            PrivateSpaceUtils.isPrivateSpaceLocked(ctx) ?: true
        }

        if (!reallyLocked) {
            _privateSpaceState.update {
                it.copy(isLocked = false, isAuthenticating = false)
            }
            return true
        }

        _privateSpaceState.update { it.copy(isAuthenticating = true) }


        // This only request the auth, it does not handle whether the private space was unlocked
        PrivateSpaceUtils.requestUnlockPrivateSpace(ctx)

        // Test with timeout the real unlock state
        val unlocked = withTimeoutOrNull(10_000L) {
            while (true) {
                val locked = withContext(Dispatchers.IO) {
                    PrivateSpaceUtils.isPrivateSpaceLocked(ctx) ?: true
                }
                if (!locked) break
                delay(200)
            }
            true // return true when unlocked
        } ?: false // if timeout


        _privateSpaceState.update {
            it.copy(
                isAuthenticating = false,
                isLocked = !unlocked
            )
        }

        return unlocked
    }


    suspend fun unlockAndReloadPrivateSpace() {

        if (!PrivateSpaceUtils.isPrivateSpaceSupported()) return

        val useDifferentialLoadingForPrivateSpace =
            BehaviorSettingsStore.useDifferentialLoadingForPrivateSpace.get(ctx)

        if (useDifferentialLoadingForPrivateSpace) {
            captureMainProfileSnapshotBeforeUnlock()
        }

        // Suspends until unlock, timeout or user cancel
        val unlocked = unlockPrivateSpace()
        if (!unlocked) return


        // Set loading state before load
        _privateSpaceState.update {
            it.copy(
                isLoading = true,
            )
        }

        if (useDifferentialLoadingForPrivateSpace) {
            detectPrivateAppsDiffAndReload()
            logI(APPS_TAG) { "Available after full private space reload" }
        } else {
            reloadApps()
            logI(APPS_TAG) { "Available after full apps reload (no differential reload)" }
        }

        // Finished loading
        _privateSpaceState.update {
            it.copy(
                isLoading = false,
            )
        }
    }


    private val _density = Density(ctx.resources.displayMetrics.density)

    private val _iconShape = MutableStateFlow<IconShape?>(null)

    fun cacheIconShape(iconShape: IconShape) {
        _iconShape.value = iconShape
    }


    /**
     * Renders a [CustomIconSerializable] from a given orig [ImageBitmap]
     * @param orig the base [ImageBitmap] that will be edited
     * @param customIcon the custom icon to render with
     * @param sizePx size of the output [ImageBitmap]
     *
     * @return [ImageBitmap] the rendered icon after customIcon process
     */
    private fun renderCustomIcon(
        orig: ImageBitmap,
        customIcon: CustomIconSerializable,
        sizePx: Int
    ): ImageBitmap {

        val base: ImageBitmap =
            if (customIcon.type == IconType.ICON_PACK) {

                val source = customIcon.source

                if (!source.isNullOrBlank() && ',' in source) {

                    val (drawableName, packPkg) = source.split(',', limit = 2)

                    loadIconFromPack(
                        packPkg = packPkg,
                        iconName = drawableName,
                        targetPkg = "" // Manual selection
                    )?.let { drawable ->
                        loadDrawableAsBitmap(
                            drawable = drawable,
                            width = sizePx,
                            height = sizePx,
                            tint = packTint.value
                        )
                    } ?: orig

                } else orig

            } else orig

        return resolveCustomIconBitmap(
            base = base,
            icon = customIcon,
            sizePx = sizePx,
            density = _density,
            iconShape = _iconShape.value ?: IconShape.PlatformDefault,
        )
    }


    /*  ────── THE MOST IMPORTANT FUNCTIONS BELOW, LOAD ALL ICONS ──────  */

    /**
     * Loads and renders the visual icon for a given swipe point.
     *
     * The final icon is computed in three steps:
     *
     * 1. Resolve the effective size in pixels, based on:
     *    - The global default point size
     *    - The point-specific size override (if any)
     *    - The current screen density
     *
     * 2. Create the base (untinted) bitmap for the point's action.
     *    If the action represents an app, the corresponding app icon is used.
     *
     * 3. Apply custom icon styling (shape, tint, etc.) and render
     *    the final [ImageBitmap].
     *
     * If the point does not define a custom shape, the current global
     * icon shape setting is applied.
     *
     * @param point The [SwipePointSerializable] containing action and optional
     *              custom icon configuration.
     *
     * @return The fully rendered [ImageBitmap] ready for drawing.
     */
    private fun loadPointIcon(point: SwipePointSerializable): ImageBitmap {

        val resolvedResolutionDp =
            point.resolution ?: _defaultPoint.value.resolution
            ?: point.size ?: _defaultPoint.value.size
            ?: SwipePointSerializable.defaultSwipePointsValues.size!!

        // Convert dp to pixels and enforce a minimum touch-safe size.
        val sizePx = (resolvedResolutionDp * _density.density)
            .toInt()

        // Create the base untinted bitmap from the action.
        val baseBitmap = createUntintedBitmap(
            action = point.action,
            ctx = ctx,
            icons = _drawerIconsCache,
            width = sizePx,
            height = sizePx
        )

        // Resolve custom icon configuration:
        // - If the point defines a shape, use it as-is.
        // - Otherwise apply the current global icon shape.
        val effectiveCustomIcon =
            if (point.customIcon?.shape != null) {
                point.customIcon!!
            } else {
                (point.customIcon ?: CustomIconSerializable()).copy(
                    shape = _iconShape.value
                )
            }

        // Render and return the final styled bitmap.
        return renderCustomIcon(
            orig = baseBitmap,
            customIcon = effectiveCustomIcon,
            sizePx = sizePx
        )
    }


    // DO a single function to load icons instead of 2 separated and shitty
    // No, in fact they are working and well now, no need to change

    private fun loadSingleIcon(
        app: AppModel,
        customIcon: CustomIconSerializable?,
        sizePx: Int
    ): ImageBitmap {
        val packageName = app.packageName
        val userId = app.userId
        val isPrivateProfile = app.isPrivateProfile
        val cacheKey = app.iconCacheKey

        var isIconPack = false
        val packIconName = getCachedIconMapping(packageName)
        val selectedPack = selectedIconPack.value

        val drawable =
            if (selectedPack != null) {
                packIconName?.let { packName ->
                    isIconPack = true
                    loadIconFromPack(
                        packPkg = selectedPack.packageName,
                        iconName = packName,
                        targetPkg = packageName
                    )
                }
            } else {
                null
            } ?: run {
                isIconPack = false
                pmCompat.getAppIcon(packageName, userId ?: 0, isPrivateProfile)
            }


        val orig = loadDrawableAsBitmap(
            drawable = drawable,
            width = sizePx,
            height = sizePx,
            tint = _packTint.value.takeIf { isIconPack }
        )

        logD(ICONS_TAG) { "Icon loaded for $cacheKey: $orig" }

        val customIcon = customIcon ?: _workspacesState.value.appOverrides[cacheKey]?.customIcon

        if (customIcon != null) {
            return renderCustomIcon(
                orig = orig,
                customIcon = customIcon,
                sizePx = sizePx
            ).also {
                logD(ICONS_TAG) { "Custom Icon rendered for $cacheKey: $it" }
            }
        }

        return orig
    }


    /* ───────────── Reload Functions ───────────── */

    /**
     * Reload a single point icon to the icons list, override if already existing
     *
     * @param point which point's icon to load
     */
    fun reloadPointIcon(point: SwipePointSerializable) {
        _pointsIconsCache.compute(point.id) {
            loadPointIcon(point)
        }
    }

    /**
     * Update single icon (for app)
     * Basically the same thing as [reloadPointIcon] but for an AppModel instead of the [SwipePointSerializable] you input an [AppModel]
     *
     * @param app
     */
    fun reloadAppIcon(
        app: AppModel,
        customIcon: CustomIconSerializable? = null,
        sizePx: Int = 128
    ) {
        _drawerIconsCache.compute(app.iconCacheKey) {
            loadSingleIcon(
                app = app,
                customIcon = customIcon,
                sizePx = sizePx
            )
        }
    }


    /* ───────────── Multiple Load Functions ───────────── */


    /**
     * Preload a given list of point icons asynchronously and per icon updates the icons list
     *
     * @param points which points to load
     */
    fun preloadPointIcons(points: List<SwipePointSerializable>) {
        logI(ICONS_TAG) { "Loading ${points.size} points icons" }

        viewModelScope.launch(Dispatchers.IO) {
            points.forEach { p ->
                iconSemaphore.withPermit {
                    reloadPointIcon(p)
                }
            }
        }
    }

    /**
     * Load app icons from a list of [AppModel]
     *
     * @param apps list of app icons to load
     * @param sizePx size of the loaded [ImageBitmap]
     */
    private suspend fun preloadAppIcons(
        apps: List<AppModel>,
        sizePx: Int
    ) = withContext(Dispatchers.IO) {
        logI(ICONS_TAG) { "Loading ${apps.size} app icons..." }

        apps.forEach { app ->
            iconSemaphore.withPermit {
                reloadAppIcon(
                    app = app,
                    customIcon = null,
                    sizePx = sizePx
                )
            }
        }
    }


    /**
     * Loads a drawable from the specified icon pack using a resolved drawable name.
     *
     * The function attempts to resolve the provided [iconName] as a `drawable`
     * resource within the icon pack identified by [packPkg]. If a matching
     * resource is found, it is returned as a [Drawable].
     *
     * This method assumes that the correct drawable name has already been
     * determined (e.g., via app-filter mapping or manual naming strategy).
     * No additional fallback logic is performed here.
     *
     * @param packPkg Package name of the icon pack. If `null`, the function
     *                returns `null` immediately.
     * @param iconName Name of the drawable resource inside the icon pack.
     * @param targetPkg Package name of the target application (used for logging/debugging).
     *
     * @return The resolved [Drawable] if found, or `null` if the drawable
     *         resource does not exist in the icon pack.
     */
    @SuppressLint("DiscouragedApi")
    fun loadIconFromPack(
        packPkg: String?,
        iconName: String,
        targetPkg: String
    ): Drawable? {

        logD(ICONS_TAG) { "Resolving icon → app=$targetPkg pack=$packPkg resolvedName=$iconName" }

        if (packPkg == null) return null

        val packResources = try {
            ctx.packageManager.getResourcesForApplication(packPkg)
        } catch (e: Exception) {
            logE(ICONS_TAG, e) { "Error fetching pack ressources: app is likely not installed" }
            null
        }

        packResources?.let {
            try {
                val drawableId = packResources.getIdentifier(iconName, "drawable", packPkg)

                logD(ICONS_TAG) { "Trying drawable: name=$iconName id=$drawableId" }
                if (drawableId != 0) {
                    return ResourcesCompat.getDrawable(packResources, drawableId, null)
                }
                logW(ICONS_TAG) { "drawableId is 0, wtf?" }

            } catch (e: Exception) {
                logE(ICONS_TAG, e) { "Error fetching pack drawable ressources" }
            }
        }

        return null
    }


    /**
     * Load all icons mappings from pack, used to display the picker list when user picks
     * a certain icon from the pack
     *
     * Doesn't load the actual icons, but their names which is cheaper and faster
     * the rendering is handled by the UI level IconPickerListDialog  (not accessible in this viewModelScope)
     *
     * @param pack the icon pack from where to load
     */
    fun loadAllIconsMappingsFromPack(pack: IconPackInfo) {

        viewModelScope.launch(Dispatchers.IO) {
            val cache = iconPackCache.getOrPut(pack.packageName) {
                loadIconPackMappings(pack.packageName)
            }

            if (cache.pkgToDrawables.isEmpty()) {
                _packIcons.value = emptyList()
                return@launch
            }

            _packIcons.value = cache.pkgToDrawables.values.flatten().distinct()
        }
    }

    /**
     * Retrieves a cached icon mapping for the given application package.
     *
     * This method checks the currently selected icon pack for a drawable
     * mapping corresponding to [pkgName]. It first attempts an exact
     * component-level match using the app's launch intent. If no exact match
     * is found, it falls back to a package-level match.
     *
     * The result is cached in [IconPackCache] to avoid repeatedly parsing
     * icon pack resources.
     *
     * @param pkgName The package name of the target application.
     *
     * @return The drawable name from the icon pack if a mapping exists,
     *         or `null` if no mapping is found.
     */
    private fun getCachedIconMapping(pkgName: String): String? {
        val pack = selectedIconPack.value ?: return null
        val cache = getCache(pack.packageName)

        logD(ICONS_TAG) { "getCachedIconMapping → app=$pkgName pack=${pack.packageName}" }

        val launchIntent = runCatching {
            pm.getLaunchIntentForPackage(pkgName)
        }.getOrNull()

        val component = launchIntent?.component?.let {
            normalizeComponent("${it.packageName}/${it.className}")
        }

        // Exact component match (best case)
        component?.let {
            cache.componentToDrawable[it]?.let { drawable ->
                return drawable
            }
        }

        // Package-level match
        cache.pkgToDrawables[pkgName]?.firstOrNull()?.let {
            return it
        }

        logD(ICONS_TAG) { "No mapping found for $pkgName" }
        return null
    }


    fun selectIconPack(pack: IconPackInfo) {
        _selectedIconPack.value = pack
        viewModelScope.launch(Dispatchers.IO) {
            UiSettingsStore.selectedIconPack.set(ctx, pack.packageName)
            reloadApps()
        }
    }

    private fun getCache(packPkg: String): IconPackCache {
        return iconPackCache[packPkg]
            ?: loadIconPackMappings(packPkg).also {
                iconPackCache[packPkg] = it
            }
    }


    fun clearIconPack() {
        _selectedIconPack.value = null
        viewModelScope.launch(Dispatchers.IO) {
            UiSettingsStore.selectedIconPack.reset(ctx)
            reloadApps()
        }
    }


    fun loadIconPacks() {
        val packs = mutableListOf<IconPackInfo>()
        val allPackages = pmCompat.getInstalledPackages()


        allPackages.forEach { pkgInfo ->
            if (pkgInfo.packageName == ctx.packageName) return@forEach

            try {
                val packResources = pmCompat.getResourcesForApplication(pkgInfo.packageName)
                val hasAppfilter = hasStandardAppFilter(packResources)

                if (hasAppfilter) {
                    val name = pkgInfo.applicationInfo?.loadLabel(pm).toString()
                    logD(ICONS_TAG) {
                        "FOUND icon pack: $name (${pkgInfo.packageName}"
                    }

                    packs.add(
                        IconPackInfo(
                            packageName = pkgInfo.packageName,
                            name = name
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val uniquePacks = packs.distinctBy { it.packageName }
        logD(ICONS_TAG) { "Total icon packs found: ${uniquePacks.size}" }
        _iconPacksList.value = uniquePacks
    }

    fun loadIconPackMappings(packPkg: String): IconPackCache {
        return try {
            val entries = parseAppFilterXml(ctx, packPkg) ?: emptyList()

            val componentToDrawable = mutableMapOf<String, String>()
            val pkgToDrawables = mutableMapOf<String, MutableList<String>>()

            entries.forEach { mapping ->
                val normalized = normalizeComponent(mapping.component)
                val pkg = normalized.substringBefore('/')

                componentToDrawable[normalized] = mapping.drawable

                val list = pkgToDrawables.getOrPut(pkg) { mutableListOf() }
                if (!list.contains(mapping.drawable)) {
                    list.add(mapping.drawable)
                }
            }

            IconPackCache(
                pkgToDrawables = pkgToDrawables,
                componentToDrawable = componentToDrawable
            )
        } catch (e: Exception) {
            logE(ICONS_TAG, e) { "Failed to load mappings for $packPkg" }
            IconPackCache(emptyMap(), emptyMap())
        }
    }


    private fun normalizeComponent(raw: String): String {
        var comp = raw

        if (comp.contains('{')) comp = comp.substringAfter('{')
        if (comp.contains('}')) comp = comp.substringBefore('}')
        comp = comp.trim()

        if (!comp.contains('/')) return comp

        val pkg = comp.substringBefore('/')
        var cls = comp.substringAfter('/')

        if (cls.startsWith(".")) {
            cls = pkg + cls
        }

        return "$pkg/$cls"
    }


    /** Load the user's workspaces into the _state var, enforced safety due to some crash at start */
    private suspend fun loadWorkspaces() = withContext(Dispatchers.IO) {
        try {
            val jsonString = WorkspaceSettingsStore.getAll(ctx).toString()
            if (jsonString.isBlank()) return@withContext

            val loadedState = json.decodeFromString<WorkspaceState>(jsonString)
            _workspacesState.value = loadedState

            // Async reload of icons to not block the workspace state emission
            launch {
                loadedState.appOverrides.forEach { (iconCacheKey, override) ->
                    val (packageName, userId) = iconCacheKey.splitCacheKey()
                    override.customIcon?.let { customIcon ->
                        reloadPointIcon(
                            point = SwipePointSerializable.dummySwipePoint(
                                SwipeActionSerializable.LaunchApp(packageName, false, userId)
                            ).copy(customIcon = customIcon, id = packageName)
                        )
                    }
                }
            }
        } catch (e: Exception) {
            logE(WORKSPACES_TAG, e) { "Error while loading the workspaces state" }
            _workspacesState.value = WorkspaceState()
        }
    }


    private fun persistWorkspaces() = viewModelScope.launch(Dispatchers.IO) {

        logW(WORKSPACES_TAG) { "Persisting the state: ${_workspacesState.value}" }

        val json = json.encodeToString(_workspacesState.value)

        logW(WORKSPACES_TAG) { "Encoded json workspace: $json" }
        WorkspaceSettingsStore.setAll(ctx, JSONObject(json))
    }

    fun selectWorkspace(id: String) {
        _selectedWorkspaceId.value = id

        viewModelScope.launch {
            DrawerSettingsStore.lastWorkspaceUsed.set(ctx, id)
        }
    }

    /* ───────────── Recently Used Apps ───────────── */

    private suspend fun loadRecentlyUsedApps() {
        val json = DrawerSettingsStore.recentlyUsedPackages.get(ctx)
        if (json.isNotEmpty()) {
            try {
                _recentlyUsedPackages.value = json.toList()
            } catch (_: Exception) {
                _recentlyUsedPackages.value = emptyList()
            }
        }
    }

    /**
     * Record a package as recently used.
     * Moves it to the front if already present, trims the list to a reasonable max.
     */
    fun addRecentlyUsedApp(packageName: String) {
        val maxStored = 30 // store more than display, user can raise the count later
        val current = _recentlyUsedPackages.value.toMutableList()
        current.remove(packageName)
        current.add(0, packageName)
        val trimmed = current.take(maxStored)
        _recentlyUsedPackages.value = trimmed
        viewModelScope.launch {
            DrawerSettingsStore.recentlyUsedPackages.set(ctx, trimmed.toSet())
        }
    }

    /**
     * Returns the recently used [AppModel]s, resolved from the current app list.
     * Uses combine to reactively update when either apps or recent packages change.
     * @param count max number of recent apps to return
     */
    fun getRecentApps(count: Int): StateFlow<List<AppModel>> {
        return _recentlyUsedPackages.combine(_apps) { packages, apps ->
            val allApps = apps.associateBy { it.packageName }
            packages
                .take(count)
                .mapNotNull { pkg -> allApps[pkg] }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    }


    /* ───────────── Workspace System───────────── */


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
        persistWorkspaces()
    }

    fun createWorkspace(name: String, type: WorkspaceType) {
        _workspacesState.value = _workspacesState.value.copy(
            workspaces = _workspacesState.value.workspaces +
                    Workspace(
                        id = System.currentTimeMillis().toString(),
                        name = name,
                        type = type,
                        enabled = true,
                        removedAppIds = emptySet(),
                        appIds = emptySet()
                    )
        )
        persistWorkspaces()
    }

    fun editWorkspace(id: String, name: String, type: WorkspaceType) {
        _workspacesState.value = _workspacesState.value.copy(
            workspaces = _workspacesState.value.workspaces.map {
                if (it.id == id) it.copy(name = name, type = type) else it
            }
        )
        persistWorkspaces()
    }

    fun deleteWorkspace(id: String) {
        _workspacesState.value = _workspacesState.value.copy(
            workspaces = _workspacesState.value.workspaces.filterNot { it.id == id }
        )
        persistWorkspaces()
    }

    fun setWorkspaceOrder(newOrder: List<Workspace>) {
        _workspacesState.value = _workspacesState.value.copy(workspaces = newOrder)
        persistWorkspaces()
    }


    fun resetWorkspace(id: String) {
        _workspacesState.value = _workspacesState.value.copy(
            workspaces = _workspacesState.value.workspaces.map {
                if (it.id == id) it.copy(removedAppIds = emptySet(), appIds = emptySet()) else it
            }
        )
        persistWorkspaces()
    }


    // Apps operations
    fun addAppToWorkspace(workspaceId: String, cacheKey: CacheKey) {

        val target = _workspacesState.value.workspaces.find { it.id == workspaceId } ?: return
        if (target.type == WorkspaceType.PRIVATE) return

        _workspacesState.value = _workspacesState.value.copy(
            workspaces = _workspacesState.value.workspaces.map { ws ->
                if (ws.id != workspaceId) return@map ws

                val removed = ws.removedAppIds ?: emptySet()

                ws.copy(
                    appIds = ws.appIds + cacheKey,
                    removedAppIds = if (cacheKey in removed)
                        removed - cacheKey
                    else
                        ws.removedAppIds
                )
            }
        )
        persistWorkspaces()
    }


    fun removeAppFromWorkspace(workspaceId: String, cacheKey: CacheKey) {

        val target = _workspacesState.value.workspaces.find { it.id == workspaceId } ?: return
        if (target.type == WorkspaceType.PRIVATE) return

        _workspacesState.value = _workspacesState.value.copy(
            workspaces = _workspacesState.value.workspaces.map { ws ->
                if (ws.id != workspaceId) return@map ws

                // remove the app cacheKey from appsIds, and add it to removedAppIDs
                ws.copy(
                    appIds = ws.appIds - cacheKey,
                    removedAppIds = (ws.removedAppIds ?: emptySet()) + cacheKey
                )
            }
        )
        persistWorkspaces()
    }

    fun addAliasToApp(alias: String, cacheKey: CacheKey) {
        _workspacesState.value = _workspacesState.value.copy(
            appAliases = _workspacesState.value.appAliases +
                    (cacheKey to (_workspacesState.value.appAliases[cacheKey]
                        ?: emptySet()) + alias)
        )
        persistWorkspaces()
    }

//    fun resetAliasesForApp(packageName: String) {
//        _workspacesState.value = _workspacesState.value.copy(
//            appAliases = _workspacesState.value.appAliases.filter { it.key != packageName }
//        )
//        persist()
//    }

    fun removeAliasFromWorkspace(aliasToRemove: String, cacheKey: CacheKey) {
        val current = _workspacesState.value.appAliases

        val updated = current[cacheKey]
            ?.minus(aliasToRemove)
            ?.takeIf { it.isNotEmpty() }

        _workspacesState.value = _workspacesState.value.copy(
            appAliases = if (updated == null)
                current - cacheKey
            else
                current + (cacheKey to updated)
        )
        persistWorkspaces()
    }

    fun renameApp(cacheKey: CacheKey, customName: String) {
        _workspacesState.value = _workspacesState.value.copy(
            appOverrides = _workspacesState.value.appOverrides +
                    (cacheKey to AppOverride(customName))
        )
        persistWorkspaces()
    }

    fun setAppIcon(cacheKey: CacheKey, customIcon: CustomIconSerializable?) {
        logD(WORKSPACES_TAG) { "CacheKey: $cacheKey, customIcon: $customIcon" }

        val prev = _workspacesState.value.appOverrides[cacheKey]

        logD(WORKSPACES_TAG) { "prev: $prev" }
        logD(WORKSPACES_TAG) { _workspacesState.value.toString() }

        _workspacesState.value = _workspacesState.value.copy(
            appOverrides = _workspacesState.value.appOverrides +
                    (cacheKey to (prev?.copy(customIcon = customIcon)
                        ?: AppOverride(customIcon = customIcon)))
        )
        persistWorkspaces()
    }

    /**
     * Mainly debug funny thing, it's like customizing all app icons at once
     * for each app installed, it applies to it the custom icon
     *
     * @param icon
     */
    fun applyIconToApps(
        icon: CustomIconSerializable?
    ) {
        viewModelScope.launch {
            iconSemaphore.withPermit {

                // Store icon ONCE
                val sharedIcon = icon?.copy()

                _workspacesState.value = _workspacesState.value.copy(
                    appOverrides = _apps.value.associate {
                        (it.iconCacheKey to AppOverride(customIcon = sharedIcon))
                    }
                )
            }
        }
        persistWorkspaces()
    }


    fun resetAppName(cacheKey: CacheKey) {
        val prev = _workspacesState.value.appOverrides[cacheKey] ?: return

        val updated = prev.copy(customName = null)

        _workspacesState.value = _workspacesState.value.copy(
            appOverrides =
                if (updated.customIcon == null)
                    _workspacesState.value.appOverrides - cacheKey
                else
                    _workspacesState.value.appOverrides + (cacheKey to updated)
        )
        viewModelScope.launch {
            reloadApps()
        }
        persistWorkspaces()
    }

    fun resetAppIcon(cacheKey: CacheKey) {
        val prev = _workspacesState.value.appOverrides[cacheKey] ?: return

        val updated = prev.copy(customIcon = null)

        _workspacesState.value = _workspacesState.value.copy(
            appOverrides =
                if (updated.customName == null)
                    _workspacesState.value.appOverrides - cacheKey
                else
                    _workspacesState.value.appOverrides + (cacheKey to updated)
        )

        viewModelScope.launch {
            reloadApps()
        }
        persistWorkspaces()
    }


    fun resetWorkspacesAndOverrides() {
        _workspacesState.value = WorkspaceState(
            workspaces = defaultWorkspaces
        )

        viewModelScope.launch {
            WorkspaceSettingsStore.resetAll(ctx)
        }
    }

    suspend fun setIconPackTint(tint: Color?) {
        UiSettingsStore.iconPackTint.set(ctx, tint)
        _packTint.value = tint?.toArgb()
        reloadApps()
    }
}

/**
 * Checks whether the given [Resources] instance contains an `appfilter.xml`
 * inside the `assets/` directory.
 *
 * This is used as a lightweight heuristic to detect traditional icon packs
 * that ship a standard `appfilter.xml` file in their assets folder.
 *
 * Note:
 * - Some icon packs place `appfilter.xml` under `res/xml/` instead of `assets/`.
 * - A `false` result does not guarantee that no app filter exists, only that
 *   it was not found in the `assets` directory.
 *
 * @param res Resources of the icon pack application.
 * @return `true` if `assets/appfilter.xml` can be opened successfully,
 *         `false` otherwise.
 */
private fun hasStandardAppFilter(res: Resources): Boolean {
    return try {
        res.assets.open("appfilter.xml").use { true }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

/**
 * Attempts to parse icon mappings from an icon pack's `appfilter.xml`.
 *
 * The function tries both common locations used by icon packs:
 *
 * 1. `assets/appfilter.xml`
 * 2. `res/xml/appfilter.xml`
 *
 * If mappings are successfully parsed from the first location, the second
 * is not attempted. If neither location yields valid mappings, `null`
 * is returned.
 *
 * This supports both traditional icon packs and variations that place
 * the filter file in different locations.
 *
 * @param ctx Context used to obtain the target application's resources.
 * @param packPkg Package name of the icon pack.
 * @return A list of [IconMapping] entries if parsing succeeds,
 *         or `null` if no valid `appfilter.xml` could be found or parsed.
 */
@SuppressLint("DiscouragedApi")
private fun parseAppFilterXml(ctx: Context, packPkg: String): List<IconMapping>? {
    val packResources = ctx.packageManager.getResourcesForApplication(packPkg)
    var mappings: List<IconMapping>? = null

    // 1. Try assets/appfilter.xml first
    try {
        packResources.assets.open("appfilter.xml").use { input ->
            val parser = Xml.newPullParser()
            parser.setInput(input.reader())
            mappings = parseXml(parser)
        }
        if (mappings?.isNotEmpty() == true) {
            logD(ICONS_TAG) { "Loaded ${mappings.size} mappings from assets/appfilter.xml" }
            return mappings
        }
    } catch (_: Exception) {
        logD(ICONS_TAG) { "Assets appfilter.xml failed" }
    }

    // 2. Fallback to res/xml/appfilter.xml
    val resId = packResources.getIdentifier("appfilter", "xml", packPkg)
    if (resId == 0) return null

    try {
        val parser: XmlResourceParser = packResources.getXml(resId)
        mappings = parseXml(parser)
        logD(ICONS_TAG) { "Loaded ${mappings.size} mappings from res/xml/appfilter.xml" }
    } catch (e: Exception) {
        logE(ICONS_TAG, e) { "res/xml/appfilter.xml parse failed" }
    }

    return mappings
}

/**
 * Parses an `appfilter.xml` document and extracts icon mapping entries.
 *
 * The parser scans for `<item>` tags and reads:
 * - `component` or `activity` attribute (component name)
 * - `drawable` attribute (icon resource name)
 *
 * Each valid pair is converted into an [IconMapping] and added to the result list.
 * Entries missing required attributes are ignored.
 *
 * @param parser An initialized [XmlPullParser] positioned at the start of
 *               an `appfilter.xml` document.
 * @return A list of parsed [IconMapping] objects. The list may be empty
 *         if no valid `<item>` entries are found.
 */
private fun parseXml(parser: XmlPullParser): List<IconMapping> {
    val mappings = mutableListOf<IconMapping>()
    var eventType = parser.eventType
    while (eventType != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
            val component = parser.getAttributeValue(null, "component") ?: parser.getAttributeValue(
                null,
                "activity"
            )
            val drawable = parser.getAttributeValue(null, "drawable")
            if (!component.isNullOrEmpty() && !drawable.isNullOrEmpty()) {
                mappings.add(IconMapping(component, drawable))
            }
        }
        eventType = parser.next()
    }
    return mappings
}

/**
 * Icon pack cache with normalized component mapping and package -> drawables list
 */
data class IconPackCache(
    val pkgToDrawables: Map<String, List<String>>,
    val componentToDrawable: Map<String, String>
)
