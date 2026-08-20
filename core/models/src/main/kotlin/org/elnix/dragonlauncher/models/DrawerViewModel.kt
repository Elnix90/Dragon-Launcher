@file:OptIn(ExperimentalCoroutinesApi::class)

package org.elnix.dragonlauncher.models

import android.content.pm.ShortcutInfo
import android.os.UserHandle
import android.service.notification.StatusBarNotification
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.appoverrides.AppOverridesManager
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.Workspace
import org.elnix.dragonlauncher.enumsui.select.WorkspaceViewMode
import org.elnix.dragonlauncher.icons.IconPack
import org.elnix.dragonlauncher.icons.IconPackManager
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.notifications.NotificationService
import org.elnix.dragonlauncher.permissions.PermissionGroup
import org.elnix.dragonlauncher.permissions.PermissionsManager
import org.elnix.dragonlauncher.recents.RecentsService
import org.elnix.dragonlauncher.workspaces.WorkspacesManager
import javax.inject.Inject


@Stable
@HiltViewModel
public class DrawerViewModel @Inject constructor(
    private val appsRepository: AppRepository,
    private val recentsService: RecentsService,
    private val permissionsManager: PermissionsManager,
    private val iconPackManager: IconPackManager,
    public val appOverrideManager: AppOverridesManager,
    public val workspaceManager: WorkspacesManager
) : ViewModel() {

    public val allApps: StateFlow<List<Application>> = appsRepository.getAllApps().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    // Only used for preview, the real user apps getter are using the appsForWorkspace function
    public val userApps: StateFlow<List<Application>> = allApps.map { list ->
        list.filter { it.isLaunchable && !it.isWork && !it.isSystem && !it.isPrivate }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


    public val notifications: Array<out StatusBarNotification?>?
        get() = NotificationService.getInstance()?.activeNotifications

    public fun isAppInstalled(packageName: String): StateFlow<Boolean> = allApps.map { apps ->
        apps.any { it.packageName == packageName }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        false
    )

    public val searchQuery: MutableState<String> = mutableStateOf("")

    public fun findOne(packageName: String, userHandle: UserHandle): Flow<Application?> = appsRepository.findOne(packageName, userHandle)

    public fun search(
        workspace: Workspace,
        workspaceViewMode: WorkspaceViewMode = WorkspaceViewMode.Default
    ): StateFlow<List<Application>> = appsRepository.search(
        searchQuery.value.trim(),
        workspace = workspace,
        workspaceViewMode = workspaceViewMode
    ).stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    public fun reloadApps(): Job = viewModelScope.launch {
        appsRepository.refreshApps()
    }

    public fun getRecentApps(count: Int): StateFlow<List<Application>> = recentsService.getRecentApps(count)

    public val selectedWorkspaceId: StateFlow<String> = workspaceManager.selectedWorkspaceId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = "User"
    )


    /**
     * Returns a flow containing only the active workspaces.
     * It shares the flow using [SharingStarted.Eagerly] and its really important,
     * so that the flow always receives the correct workspaces, and not an emptyList when it starts to collect it
     */
    public val activeWorkspaces: StateFlow<List<Workspace>> = workspaceManager.workspaces.flow.map { workspaces ->
        workspaces.filter { it.enabled }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    public fun queryAppShortcuts(packageName: String): List<ShortcutInfo> = appsRepository.queryAppShortcuts(packageName)

    public fun hasPermission(permission: PermissionGroup): Flow<Boolean> = permissionsManager.hasPermission(permission)

    public fun getInstalledIconPacks(): Flow<List<IconPack>> = iconPackManager.getInstalledIconPacks()

    init {
        viewModelInitialized()
    }
}