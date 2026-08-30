package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.runtime.Stable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Stable
@Serializable
@SerialName("Workspace")
public data class Workspace(
    val id: String,
    val type: WorkspaceType,
    val appIds: Set<CacheKey>? = null,
    val removedAppIds: Set<CacheKey>? = setOf(CacheKey("org.elnix.dragonlauncher", 0)),
    val enabled: Boolean = true
) {
    public companion object {
        // I disable non-user workspaces by default, enable it if you need it (only used for nerds) (those who download my app are btw :) )
        public val defaultWorkspaces: List<Workspace> =
            listOf(
                Workspace(
                    id = "User",
                    type = WorkspaceType.User
                ),
                Workspace(
                    id = "System",
                    type = WorkspaceType.System,
                    enabled = false
                ),
                Workspace(
                    id = "All",
                    type = WorkspaceType.All,
                    enabled = false
                ),
                Workspace(
                    id = "Work",
                    type = WorkspaceType.Work,
                    enabled = false
                ),
                Workspace(
                    id = "Private Space",
                    type = WorkspaceType.Private,
                    enabled = false
                ) // Android 15+ only
            )
    }
}

public typealias WorkspaceState = List<Workspace>
