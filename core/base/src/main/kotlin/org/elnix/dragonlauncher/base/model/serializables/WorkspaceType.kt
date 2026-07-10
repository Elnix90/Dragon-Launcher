package org.elnix.dragonlauncher.base.model.serializables

import androidx.annotation.DrawableRes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.i18n.R


@Serializable
@SerialName("WorkspaceType")
public enum class WorkspaceType(
    @param:DrawableRes public val icon: Int
){
    All(R.drawable.select_all),
    User(R.drawable.account_tree),
    System(R.drawable.account_tree),
    Work(R.drawable.enterprise),
    Private(R.drawable.encrypted),
    Custom(R.drawable.instant_mix);

    public companion object {
        public val WorkspaceType.isPrivate: Boolean
            get() = this == Private
    }
}
