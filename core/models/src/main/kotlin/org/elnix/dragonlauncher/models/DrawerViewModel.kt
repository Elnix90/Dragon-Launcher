@file:OptIn(ExperimentalCoroutinesApi::class)

package org.elnix.dragonlauncher.models

import android.os.UserHandle
import android.service.notification.StatusBarNotification
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.appoverrides.AppOverridesManager
import org.elnix.dragonlauncher.badges.Badge
import org.elnix.dragonlauncher.badges.BadgeService
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.Workspace
import org.elnix.dragonlauncher.icons.IconPackManager
import org.elnix.dragonlauncher.icons.IconService
import org.elnix.dragonlauncher.icons.IconSettings
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.notifications.NotificationService
import org.elnix.dragonlauncher.permissions.PermissionsManager
import org.elnix.dragonlauncher.recents.RecentsService
import org.elnix.dragonlauncher.workspaces.WorkspacesManager
import javax.inject.Inject


@HiltViewModel
class DrawerViewModel @Inject constructor(
    application: android.app.Application,
    val iconPackManager: IconPackManager,
    val appsRepository: AppRepository,
    private val recentsService: RecentsService,
    val iconsService: IconService,
    val badgeService: BadgeService,
    val workspaceManager: WorkspacesManager,
    val appOverrideManager: AppOverridesManager,
    val permissionsManager: PermissionsManager,
    notificationService: NotificationService
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


    val notifications: Array<out StatusBarNotification?>? = notificationService.activeNotifications

    fun isAppInstalled(packageName: String) = allApps.map { apps ->
        apps.any { it.packageName == packageName }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        false
    )


    val iconPackList = iconPackManager.getInstalledIconPacks()

    val packTint = iconsService.packTint
//    /**
//     * The list of icons available in the selected pack
//     */
//    private val _packIcons = MutableStateFlow<List<String>>(emptyList())
//    val packIcons: StateFlow<List<String>> = _packIcons.asStateFlow()


    val searchQuery = mutableStateOf("")

    fun findOne(packageName: String, userHandle: UserHandle) = appsRepository.findOne(packageName, userHandle)

    fun search(
        workspace: Workspace?,
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

    fun reloadApps() = viewModelScope.launch {
        appsRepository.refreshApps()
    }

    val iconSettings = iconsService.iconSettings.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        IconSettings()
    )

    fun getRecentApps(count: Int): StateFlow<List<Application>> {
        return recentsService.getRecentApps(count)
    }


    fun selectWorkspace(workspaceId: String) = workspaceManager.selectWorkspace(workspaceId)
    val selectedWorkspaceId = workspaceManager.selectedWorkspaceId.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        "user"
    )


    fun getIconPickerVM(application: Application): IconPickerVM =
        IconPickerVM(application, iconsService)


    fun getIcon(application: Application): StateFlow<LauncherIcon?> = iconsService.getAppIcon(application).stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )


    fun getBadge(application: Application): StateFlow<Badge?> = badgeService.getBadge(application).stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    init {
        viewModelInitialized()
    }
}