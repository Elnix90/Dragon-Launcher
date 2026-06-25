@file:OptIn(ExperimentalCoroutinesApi::class)

package org.elnix.dragonlauncher.models

import android.content.pm.ShortcutInfo
import android.os.UserHandle
import android.service.notification.StatusBarNotification
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
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
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.Workspace
import org.elnix.dragonlauncher.icons.IconPackManager
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.notifications.NotificationService
import org.elnix.dragonlauncher.permissions.PermissionGroup
import org.elnix.dragonlauncher.permissions.PermissionsManager
import org.elnix.dragonlauncher.recents.RecentsService
import org.elnix.dragonlauncher.workspaces.WorkspacesManager
import javax.inject.Inject


@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val appsRepository: AppRepository,
    private val recentsService: RecentsService,
    private val permissionsManager: PermissionsManager,
    private val iconPackManager: IconPackManager,
    val appOverrideManager: AppOverridesManager,
    val workspaceManager: WorkspacesManager,
    notificationService: NotificationService
) : ViewModel() {

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

    fun getRecentApps(count: Int): StateFlow<List<Application>> {
        return recentsService.getRecentApps(count)
    }


    fun selectWorkspace(workspaceId: String) = workspaceManager.selectWorkspace(workspaceId)
    val selectedWorkspaceId = workspaceManager.selectedWorkspaceId.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        "user"
    )

    fun queryAppShortcuts(packageName: String): List<ShortcutInfo> = appsRepository.queryAppShortcuts(packageName)

    fun hasPermission(permission: PermissionGroup) = permissionsManager.hasPermission(permission)

    fun getInstalledIconPacks() = iconPackManager.getInstalledIconPacks()

    init {
        viewModelInitialized()
    }
}