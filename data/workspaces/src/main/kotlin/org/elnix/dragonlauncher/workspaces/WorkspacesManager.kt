@file:Suppress("unused")

package org.elnix.dragonlauncher.workspaces

import android.content.Context
import io.github.elnix90.logging.WORKSPACES_TAG
import io.github.elnix90.logging.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.model.serializables.CacheKey
import org.elnix.dragonlauncher.base.model.serializables.Workspace
import org.elnix.dragonlauncher.base.model.serializables.Workspace.Companion.defaultWorkspaces
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceState
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType
import org.elnix.dragonlauncher.settings.stores.array.WorkspaceSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore

private object WorkspaceJson : DragonJson<List<Workspace>>()

public class WorkspacesManager(
    private val ctx: Context
) {

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    public val workspaces: SettingFlow<List<Workspace>> = SettingFlow(defaultWorkspaces)
    public val selectedWorkspaceId: Flow<String> = DrawerSettingsStore.lastWorkspaceUsed.flow(ctx)

//    public val selectedWorkspace: Flow<Workspace?> = combine(workspaces.flow, selectedWorkspaceId) { workspaces, selected ->
//        if (workspaces.isEmpty()) return@combine null
//        workspaces.firstOrNull { it.id == selected } ?: workspaces.first()
//    }

    init {
        scope.launch {
            loadWorkspaces()
        }
    }

    /** Load the user's workspaces into the _state var, enforced safety due to some crash at start */
    private suspend fun loadWorkspaces() = withContext(Dispatchers.IO) {
        try {
            val jsonString = WorkspaceSettingsStore.jsonSetting.get(ctx)
            if (jsonString.isBlank()) return@withContext

            val loadedState = WorkspaceJson.decode(jsonString, defaultWorkspaces)
            workspaces.value = loadedState

        } catch (e: Exception) {
            logE(WORKSPACES_TAG, e) { "Error while loading the workspaces state" }
            workspaces.value = defaultWorkspaces
        }
    }

    public fun persistWorkspaces() {
        scope.launch(Dispatchers.IO) {
            if (workspaces.value == defaultWorkspaces) return@launch
            val json = WorkspaceJson.encode(workspaces.value)
            WorkspaceSettingsStore.jsonSetting.set(ctx, json)
        }
    }


    private inline fun update(newWorkSpaceState: (WorkspaceState) -> WorkspaceState) {
        workspaces.value = newWorkSpaceState(workspaces.value)
        persistWorkspaces()
    }

    private inline fun updateWs(id: String, newWs: (Workspace) -> Workspace) {
        update { old ->
            old.map {
                if (it.id == id) newWs(it) else it
            }
        }
    }

//    public fun selectWorkspace(id: String) {
//        selectedWorkspaceId.value = id
//
//        scope.launch {
//            DrawerSettingsStore.lastWorkspaceUsed.set(ctx, id)
//        }
//    }


    /** Enable/disable a workspace */
    public fun setWorkspaceEnabled(id: String, enabled: Boolean) {
        updateWs(id) { old ->
            old.copy(enabled = enabled)
        }
    }

    public fun createWorkspace(name: String, type: WorkspaceType) {
        update { old ->
            old + Workspace(
                id = name,
                type = type,
                enabled = true,
                removedAppIds = emptySet(),
                appIds = emptySet()
            )
        }
    }

    public fun editWorkspace(oldId: String, newId: String, type: WorkspaceType) {
        updateWs(oldId) { old ->
            old.copy(id = newId, type = type)
        }
    }

    public fun deleteWorkspace(id: String) {
        update { old ->
            old.filterNot { it.id == id }
        }
    }

    public fun setWorkspaceOrder(newOrder: List<Workspace>) {
        update { newOrder }
    }

    public fun resetWorkspace(id: String) {
        updateWs(id) { old ->
            old.copy(removedAppIds = emptySet(), appIds = emptySet())
        }
    }

    public fun addAppToWorkspace(id: String, cacheKey: CacheKey) {
        updateWs(id) { old ->
            old.copy(
                appIds = old.appIds?.plus(cacheKey) ?: setOf(cacheKey),
                removedAppIds = old.removedAppIds?.minus(cacheKey)
            )
        }
    }

    public fun removeAppFromWorkspace(id: String, cacheKey: CacheKey) {
        updateWs(id) { old ->
            old.copy(
                appIds = old.appIds?.minus(cacheKey),
                removedAppIds = old.removedAppIds?.plus(cacheKey)
            )
        }
    }

    public fun resetWorkspaces() {
        workspaces.value = defaultWorkspaces

        scope.launch {
            WorkspaceSettingsStore.resetAll(ctx)
        }
    }
}