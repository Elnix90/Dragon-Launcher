package org.elnix.dragonlauncher.base.model.serializables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Workspace")
public data class Workspace(
    val id: String,
    val name: String,
    val type: WorkspaceType,
    val appIds: Set<CacheKey>? = null,
    val removedAppIds: Set<CacheKey>? = null,
    val enabled: Boolean = true
) {
    public companion object {
        // I disable non-user workspaces by default, enable it if you need it (only used for nerds) (those who download my app are btw :) )
        public val defaultWorkspaces: List<Workspace> = listOf(
            Workspace(
                id = "user",
                name = "User",
                type = WorkspaceType.User,
                appIds = setOf(CacheKey("org.elnix.dragonlauncher", 0)),
            ),
            Workspace(
                id = "system",
                name = "System",
                type = WorkspaceType.System,
                enabled = false
            ),
            Workspace(
                id = "all",
                name = "All",
                type = WorkspaceType.All,
                enabled = false
            ),
            Workspace(
                id = "work",
                name = "Work",
                type = WorkspaceType.Work,
                enabled = false
            ),
            Workspace(
                id = "private",
                name = "Private Space",
                type = WorkspaceType.Private,
                enabled = false
            ) // Android 15+ only
        )
    }
}


public typealias WorkspaceState = List<Workspace>
