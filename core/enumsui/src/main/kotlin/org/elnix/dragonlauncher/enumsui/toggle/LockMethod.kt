package org.elnix.dragonlauncher.enumsui.toggle

import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.enumsui.ToggleButtonOption

/**
 * Available methods for locking the settings screen.
 */
enum class LockMethod(
    override val resId: Int,
    override val iconEnabled: Int? = null,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    /** No lock — settings are freely accessible */
    NONE(R.string.lock_none),

    /** Require a user-defined PIN code */
    PIN(R.string.lock_pin),

    /** Use native Android device unlock (biometric + device credentials fallback) */
    DEVICE_UNLOCK(R.string.lock_device_unlock)
}
