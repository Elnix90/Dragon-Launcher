@file:OptIn(ExperimentalCoroutinesApi::class)

package org.elnix.dragonlauncher.models

import android.os.UserHandle
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.appoverrides.AppOverridesManager
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.Workspace
import org.elnix.dragonlauncher.icons.IconPackManager
import org.elnix.dragonlauncher.icons.IconService
import org.elnix.dragonlauncher.models.utils.stateFlowDelegate
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.recents.RecentsService
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.workspaces.WorkspacesManager
import javax.inject.Inject


@HiltViewModel
class DrawerViewModel @Inject constructor(
    application: android.app.Application,
    val iconPackManager: IconPackManager,
    val appsRepository: AppRepository,
    private val recentsService: RecentsService,
    val iconsService: IconService,
    val workspaceManager: WorkspacesManager,
    val appOverrideManager: AppOverridesManager
) : AndroidViewModel(application) {

    val allApps: StateFlow<List<Application>> = appsRepository.getAllApps().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    // Only used for preview, the real user apps getter are using the appsForWorkspace function
    val userApps: StateFlow<List<Application>> = allApps.map { list ->
        list.filter { it.isLaunchable && !it.isWork && !it.isSystem && !it.isPrivate }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


    fun isAppInstalled(packageName: String) = allApps.map { apps ->
        apps.any { it.packageName == packageName }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        false
    )


    val iconPackList = iconPackManager.getInstalledIconPacks()
    val pointsIconsCache = iconsService.pointsIconsCache
    val drawerIconsCache = iconsService.drawerIconCache

    val packTint = iconsService.packTint
//    /**
//     * The list of icons available in the selected pack
//     */
//    private val _packIcons = MutableStateFlow<List<String>>(emptyList())
//    val packIcons: StateFlow<List<String>> = _packIcons.asStateFlow()


    val searchQuery = mutableStateOf("")

    fun findOne(packageName: String, userHandle: UserHandle) = appsRepository.findOne(packageName, userHandle)

    fun search(
        workspace: Workspace,
        getOnlyAdded: Boolean = false,
        getOnlyRemoved: Boolean = false
    ) = appsRepository.search(
        searchQuery.value,
        workspace = workspace,
        getOnlyAdded = getOnlyAdded,
        getOnlyRemoved = getOnlyRemoved,
    ).stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )


    val leftDrawerAction by stateFlowDelegate(DrawerSettingsStore.leftDrawerAction)
    val rightDrawerAction by stateFlowDelegate(DrawerSettingsStore.rightDrawerAction)
    val drawerEnterAction by stateFlowDelegate(DrawerSettingsStore.drawerEnterAction)
    val drawerHomeAction by stateFlowDelegate(DrawerSettingsStore.drawerHomeAction)
    val scrollUpDrawerAction by stateFlowDelegate(DrawerSettingsStore.scrollUpDrawerAction)
    val scrollDownDrawerAction by stateFlowDelegate(DrawerSettingsStore.scrollDownDrawerAction)
    val backDrawerAction by stateFlowDelegate(DrawerSettingsStore.backDrawerAction)

    val leftDrawerWidth by stateFlowDelegate(DrawerSettingsStore.leftDrawerWidth)
    val rightDrawerWidth by stateFlowDelegate(DrawerSettingsStore.rightDrawerWidth)
    val showSearchBar by stateFlowDelegate(DrawerSettingsStore.showSearchBar)
    val showRecentlyUsedApps by stateFlowDelegate(DrawerSettingsStore.showRecentlyUsedApps)
    val recentlyUsedAppsCount by stateFlowDelegate(DrawerSettingsStore.recentlyUsedAppsCount)
    val tapEmptySpaceAction by stateFlowDelegate(DrawerSettingsStore.tapEmptySpaceAction)
    val disableAutoLaunchOnSpaceFirstChar by stateFlowDelegate(DrawerSettingsStore.disableAutoLaunchOnSpaceFirstChar)
    val autoOpenSingleMatch by stateFlowDelegate(DrawerSettingsStore.autoOpenSingleMatch)
    val pullDownWallPaperDimFade by stateFlowDelegate(DrawerSettingsStore.pullDownWallPaperDimFade)

    val autoShowKeyboardOnDrawer by stateFlowDelegate(DrawerSettingsStore.autoShowKeyboardOnDrawer)
    val drawerToolbars by stateFlowDelegate(DrawerSettingsStore.toolbarsOrder)
    val iconShape by stateFlowDelegate(DrawerSettingsStore.iconsShape)

    fun getRecentApps(count: Int): StateFlow<List<Application>> {
        return recentsService.getRecentApps(count)
    }


    fun selectWorkspace(workspaceId: String) = workspaceManager.selectWorkspace(workspaceId)
    val selectedWorkspaceId = workspaceManager.selectedWorkspaceId.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        "user"
    )

    init {
        viewModelInitialized()
    }
}