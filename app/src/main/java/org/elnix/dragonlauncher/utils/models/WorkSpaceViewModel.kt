package org.elnix.dragonlauncher.utils.models

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.data.stores.WorkspaceSettingsStore
import org.elnix.dragonlauncher.ui.drawer.AppOverride
import org.elnix.dragonlauncher.ui.drawer.Workspace
import org.elnix.dragonlauncher.ui.drawer.WorkspaceState
import org.elnix.dragonlauncher.ui.drawer.WorkspaceType
import org.elnix.dragonlauncher.ui.drawer.defaultWorkspaces
import org.json.JSONObject
import java.io.ByteArrayOutputStream


class WorkspaceViewModel(application: Application) : AndroidViewModel(application) {

    private val gson = Gson()

    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext

    private val _state = MutableStateFlow(
        WorkspaceState(
            workspaces = defaultWorkspaces,
            appOverrides = emptyMap()
        )
    )
    val state: StateFlow<WorkspaceState> = _state.asStateFlow()

    /** Get enabled workspaces only */
    val enabledState: StateFlow<WorkspaceState> = _state
        .map { state ->
            state.copy(
                workspaces = state.workspaces.filter { it.enabled }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = WorkspaceState(
                workspaces = emptyList(),
                appOverrides = emptyMap()
            )
        )


    private val _selectedWorkspaceId = MutableStateFlow("user")
    val selectedWorkspaceId: StateFlow<String> = _selectedWorkspaceId.asStateFlow()

    init {
        load()
    }


    /** Load the user's workspaces into the _state var, enforced safety due to some crash at start */
    private fun load() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val json = WorkspaceSettingsStore.getAll(ctx).toString()

            // Correct generic type: WorkspaceState with List<Workspace>
            val type = object : TypeToken<WorkspaceState>() {}.type
            val loadedState: WorkspaceState? = gson.fromJson(json, type)

            _state.value = loadedState?.copy(
                workspaces = loadedState.workspaces,
                appOverrides = loadedState.appOverrides
            ) ?: WorkspaceState(
                workspaces = defaultWorkspaces,
                appOverrides = emptyMap()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            _state.value = WorkspaceState(
                workspaces = defaultWorkspaces,
                appOverrides = emptyMap()
            )
        }
    }



    private fun persist() = viewModelScope.launch(Dispatchers.IO) {
        WorkspaceSettingsStore.setAll(
            ctx,
            JSONObject(gson.toJson(_state.value))
        )
    }

    fun selectWorkspace(id: String) {
        _selectedWorkspaceId.value = id
    }


    /** Enable/disable a workspace */
    fun setWorkspaceEnabled(id: String, enabled: Boolean) {
        _state.value = _state.value.copy(
            workspaces = _state.value.workspaces.map { workspace ->
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
        _state.value = _state.value.copy(
            workspaces = _state.value.workspaces +
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
        _state.value = _state.value.copy(
            workspaces = _state.value.workspaces.map {
                if (it.id == id) it.copy(name = name, type = type) else it
            }
        )
        persist()
    }

    fun deleteWorkspace(id: String) {
        _state.value = _state.value.copy(
            workspaces = _state.value.workspaces.filterNot { it.id == id }
        )
        persist()
    }

    fun setWorkspaceOrder(newOrder: List<Workspace>) {
        _state.value = _state.value.copy(workspaces = newOrder)
        persist()
    }


    fun resetWorkspace(id: String) {
        _state.value = _state.value.copy(
            workspaces = _state.value.workspaces.map {
                if (it.id == id) it.copy(removedAppIds = emptyList(), appIds = emptyList()) else it
            }
        )
        persist()
    }


    // Apps operations
    fun addAppToWorkspace(workspaceId: String, packageName: String) {
        _state.value = _state.value.copy(
            workspaces = _state.value.workspaces.map { ws ->
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
        _state.value = _state.value.copy(
            workspaces = _state.value.workspaces.map { ws ->
                if (ws.id != workspaceId) return@map ws

                // remove the app packageName from appsIds, and add it to removedAppIDs
                ws.copy(
                    appIds = ws.appIds - packageName,
                    removedAppIds =  (ws.removedAppIds ?: emptyList()) + packageName
                )
            }
        )
        persist()
    }




    fun renameApp(packageName: String, name: String) {
        _state.value = _state.value.copy(
            appOverrides = _state.value.appOverrides +
                    (packageName to AppOverride(packageName, name))
        )
        persist()
    }

    fun setAppIcon(packageName: String, bitmap: Bitmap) {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val b64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

        val prev = _state.value.appOverrides[packageName]
        _state.value = _state.value.copy(
            appOverrides = _state.value.appOverrides +
                    (packageName to (prev?.copy(customIconBase64 = b64)
                        ?: AppOverride(packageName, customIconBase64 = b64)))
        )
        persist()
    }

    fun resetAppName(packageName: String) {
        val prev = _state.value.appOverrides[packageName] ?: return

        val updated = prev.copy(customLabel = null)

        _state.value = _state.value.copy(
            appOverrides =
                if (updated.customIconBase64 == null)
                    _state.value.appOverrides - packageName
                else
                    _state.value.appOverrides + (packageName to updated)
        )
        persist()
    }

    fun resetAppIcon(packageName: String) {
        val prev = _state.value.appOverrides[packageName] ?: return

        val updated = prev.copy(customIconBase64 = null)

        _state.value = _state.value.copy(
            appOverrides =
                if (updated.customLabel == null)
                    _state.value.appOverrides - packageName
                else
                    _state.value.appOverrides + (packageName to updated)
        )
        persist()
    }


    fun resetWorkspacesAndOverrides() {
        _state.value = WorkspaceState(
            workspaces = defaultWorkspaces,
            appOverrides = emptyMap()
        )
        persist()
    }
}
