package org.elnix.dragonlauncher.common.serializables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.common.serializables.Workspace.Companion.defaultWorkspaces

@Serializable
@SerialName("Workspace")
data class Workspace(
    val id: String,
    val name: String,
    val type: WorkspaceType,
    val appIds: Set<CacheKey>? = null,
    val removedAppIds: Set<CacheKey>? = null,
    val enabled: Boolean = true
) {
    companion object {
        // I disable non-user workspaces by default, enable it if you need it (only used for nerds) (those who download my app are btw :) )
        val defaultWorkspaces = listOf(
            Workspace(
                id = "user",
                name = "User",
                type = WorkspaceType.USER,
                appIds = setOf(CacheKey("org.elnix.dragonlauncher", 0)),
            ),
            Workspace(
                id = "system",
                name = "System",
                type = WorkspaceType.SYSTEM,
                enabled = false
            ),
            Workspace(
                id = "all",
                name = "All",
                type = WorkspaceType.ALL,
                enabled = false
            ),
            Workspace(
                id = "work",
                name = "Work",
                type = WorkspaceType.WORK,
                enabled = false
            ),
            Workspace(
                id = "private",
                name = "Private Space",
                type = WorkspaceType.PRIVATE,
                enabled = false
            ) // Android 15+ only
        )
    }
}


@Serializable
data class WorkspaceState(
    val workspaces: List<Workspace> = defaultWorkspaces,
)
