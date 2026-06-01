package org.elnix.dragonlauncher.base.model.serializables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("WorkspaceType")
enum class WorkspaceType {
    ALL,
    USER,
    SYSTEM,
    WORK,
    PRIVATE,
    CUSTOM;

    companion object {
        val WorkspaceType.isPrivate: Boolean
            get() = this == PRIVATE
    }
}
