@file:Suppress("unused")

package org.elnix.dragonlauncher.workspaces

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.model.serializables.CacheKey
import org.elnix.dragonlauncher.base.model.serializables.Workspace
import org.elnix.dragonlauncher.base.model.serializables.Workspace.Companion.defaultWorkspaces
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceState
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType
import org.elnix.dragonlauncher.logging.WORKSPACES_TAG
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.settings.stores.array.WorkspaceSettingsStore

object WorkspaceJson : DragonJson<List<Workspace>>()

class WorkspacesManager(
    private val ctx: Context
) {

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    private val _workspacesState = MutableStateFlow(defaultWorkspaces)
    val workspacesState = _workspacesState.asStateFlow()

    private val _selectedWorkspaceId = MutableStateFlow("user")
    val selectedWorkspaceId: StateFlow<String> = _selectedWorkspaceId.asStateFlow()

    val selectedWorkspace by mutableStateOf(_workspacesState.value.firstOrNull())

    init {
        scope.launch {
            loadWorkspaces()
            _selectedWorkspaceId.value = DrawerSettingsStore.lastWorkspaceUsed.get(ctx)
        }
    }

    /** Load the user's workspaces into the _state var, enforced safety due to some crash at start */
    private suspend fun loadWorkspaces() = withContext(Dispatchers.IO) {
        try {
            val jsonString = WorkspaceSettingsStore.jsonSetting.get(ctx)
            if (jsonString.isBlank()) return@withContext

            val loadedState = WorkspaceJson.decode(jsonString, defaultWorkspaces)
            _workspacesState.value = loadedState

        } catch (e: Exception) {
            logE(WORKSPACES_TAG, e) { "Error while loading the workspaces state" }
            _workspacesState.value = defaultWorkspaces
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
            old.map {
                if (it.id == id) newWs(it) else it
            }
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
            old + Workspace(
                id = System.currentTimeMillis().toString(),
                name = name,
                type = type,
                enabled = true,
                removedAppIds = emptySet(),
                appIds = emptySet()
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
            old.filterNot { it.id == id }
        }
    }

    fun setWorkspaceOrder(newOrder: List<Workspace>) {
        update { newOrder }
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
        _workspacesState.value = defaultWorkspaces

        scope.launch {
            WorkspaceSettingsStore.resetAll(ctx)
        }
    }
}