package org.elnix.dragonlauncher.ui.drawer

import org.elnix.dragonlauncher.data.SwipeActionSerializable

data class AppModel(
    val name: String,
    val packageName: String,
    val isEnabled: Boolean,
    val isSystem: Boolean,
    val isWorkProfile: Boolean,
    val settings: Map<String, Any> = emptyMap()
) {
    val action = SwipeActionSerializable.LaunchApp(packageName)
}


enum class WorkspaceType {
    ALL,
    USER,
    SYSTEM,
    WORK,
    CUSTOM
}

data class Workspace(
    val id: String,
    val name: String,
    val type: WorkspaceType,
    val appIds: List<String>,
    val enabled: Boolean
)


data class AppOverride(
    val packageName: String,
    val customLabel: String? = null,
    val customIconUri: String? = null
)



data class WorkspaceState(
    val workspaces: List<Workspace>,
    val appOverrides: Map<String, AppOverride>
)

fun resolveApp(
    app: AppModel,
    overrides: Map<String, AppOverride>
): AppModel {
    val o = overrides[app.packageName] ?: return app
    return app.copy(name = o.customLabel ?: app.name)
}


val defaultWorkspaces = listOf(
    Workspace("all", "All", WorkspaceType.ALL, emptyList(), true),
    Workspace("user", "User", WorkspaceType.USER, emptyList(), true),
    Workspace("system", "System", WorkspaceType.SYSTEM, emptyList(), true),
    // I set work profile disabled by default, enable it if you need it
    Workspace("work", "Work", WorkspaceType.WORK, emptyList(), false)
)
