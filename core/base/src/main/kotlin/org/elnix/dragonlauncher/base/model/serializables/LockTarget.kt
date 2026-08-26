package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.enumsui.toggle.LockMethod
import org.elnix.dragonlauncher.base.navigation.NavigationRoute


/**
 * Lock target for when the user sets a new lock screen.
 * Used by the [NavigationRoute.LockScreenSetup] to know what hash to save and where
 **/
@Immutable
@Serializable
@SerialName("LockTarget")
public sealed class LockTarget {

    public abstract val method: LockMethod

    @SerialName("Settings")
    public data class Settings(
        override val method: LockMethod
    ) : LockTarget()

    @SerialName("Action")
    public data class Action(
        override val method: LockMethod,
        val useSameLockAndHashAsSettings: Boolean
    ) : LockTarget()
}

/**
 * Lock result to allow [NavigationRoute.LockScreen] to handle an action upon successful unlock
 */
@Immutable
@Serializable
@SerialName("LockResult")
public sealed class LockResult {

    @SerialName("Settings")
    public data class Settings(
        val screenToGo: NavigationRoute
    ) : LockResult()

    @SerialName("Action")
    public data class Action(
        val actionToLaunch: org.elnix.dragonlauncher.base.model.serializables.Action
    ) : LockResult()
}
