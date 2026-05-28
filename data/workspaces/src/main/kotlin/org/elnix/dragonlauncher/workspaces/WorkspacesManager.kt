@file:Suppress("unused")

package org.elnix.dragonlauncher.workspaces

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.WORKSPACES_TAG
import org.elnix.dragonlauncher.common.serializables.CacheKey
import org.elnix.dragonlauncher.common.serializables.DragonJson
import org.elnix.dragonlauncher.common.serializables.Workspace
import org.elnix.dragonlauncher.common.serializables.WorkspaceState
import org.elnix.dragonlauncher.common.serializables.WorkspaceType
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.settings.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.settings.stores.WorkspaceSettingsStore

object WorkspaceJson : DragonJson<WorkspaceState>()

class WorkspacesManager(
    private val ctx: Context
) {

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    private val _workspacesState = MutableStateFlow(WorkspaceState())
    val workspacesState = _workspacesState.asStateFlow()

    private val _selectedWorkspaceId = MutableStateFlow("user")
    val selectedWorkspaceId: StateFlow<String> = _selectedWorkspaceId.asStateFlow()

    init {
        scope.launch { loadWorkspaces() }

        scope.launch {
            _selectedWorkspaceId.value = DrawerSettingsStore.lastWorkspaceUsed.get(ctx)
        }
    }

    /** Load the user's workspaces into the _state var, enforced safety due to some crash at start */
    private suspend fun loadWorkspaces() = withContext(Dispatchers.IO) {
        try {
            val jsonString = WorkspaceSettingsStore.jsonSetting.get(ctx)
            if (jsonString.isBlank()) return@withContext

            val loadedState = WorkspaceJson.decode<WorkspaceState>(jsonString) ?: WorkspaceState()
            _workspacesState.value = loadedState

        } catch (e: Exception) {
            logE(WORKSPACES_TAG, e) { "Error while loading the workspaces state" }
            _workspacesState.value = WorkspaceState()
        }
    }

    private fun persistWorkspaces() = scope.launch(Dispatchers.IO) {
        val json = WorkspaceJson.encode(_workspacesState.value)
        WorkspaceSettingsStore.jsonSetting.set(ctx, json)
    }


    private inline fun update(newWorkSpaceState: (WorkspaceState) -> WorkspaceState?) {
        newWorkSpaceState(_workspacesState.value)?.let {
            _workspacesState.value = it
            persistWorkspaces()
        }
    }

    private inline fun updateWs(id: String, newWs: (Workspace) -> Workspace) {
        update { old ->
            old.copy(
                workspaces = old.workspaces.map {
                    if (it.id == id) newWs(it) else it
                }
            )
        }
    }

    fun selectWorkspace(id: String) {
        _selectedWorkspaceId.value = id

        scope.launch {
            DrawerSettingsStore.lastWorkspaceUsed.set(ctx, id)
        }
    }


    /** Enable/disable a workspace */
    fun setWorkspaceEnabled(id: String, enabled: Boolean) {
        updateWs(id) { old ->
            old.copy(enabled = enabled)
        }
    }

    fun createWorkspace(name: String, type: WorkspaceType) {
        update { old ->
            old.copy(
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
        }
    }

    fun editWorkspace(id: String, name: String, type: WorkspaceType) {
        updateWs(id) { old ->
            old.copy(name = name, type = type)
        }
    }

    fun deleteWorkspace(id: String) {
        update { old ->
            old.copy(workspaces = _workspacesState.value.workspaces.filterNot { it.id == id }
            )
        }
    }

    fun setWorkspaceOrder(newOrder: List<Workspace>) {
        update { old ->
            old.copy(workspaces = newOrder)
        }
    }

    fun resetWorkspace(id: String) {
        updateWs(id) { old ->
            old.copy(removedAppIds = emptySet(), appIds = emptySet())
        }
    }

    fun addAppToWorkspace(id: String, cacheKey: CacheKey) {
        updateWs(id) { old ->
            old.copy(
                appIds = old.appIds?.plus(cacheKey) ?: setOf(cacheKey),
                removedAppIds = old.removedAppIds?.minus(cacheKey)
            )
        }
    }

    fun removeAppFromWorkspace(id: String, cacheKey: CacheKey) {
        updateWs(id) { old ->
            old.copy(
                appIds = old.appIds?.minus(cacheKey),
                removedAppIds = old.removedAppIds?.plus(cacheKey)
            )
        }
    }

    fun resetWorkspaces() {
        _workspacesState.value = WorkspaceState()

        scope.launch {
            WorkspaceSettingsStore.resetAll(ctx)
        }
    }
}