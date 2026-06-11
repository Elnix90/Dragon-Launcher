package org.elnix.dragonlauncher.base.model.serializables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("WorkspaceType")
enum class WorkspaceType {
    All,
    User,
    System,
    Work,
    Private,
    Custom;

    companion object {
        val WorkspaceType.isPrivate: Boolean
            get() = this == Private
    }
}
