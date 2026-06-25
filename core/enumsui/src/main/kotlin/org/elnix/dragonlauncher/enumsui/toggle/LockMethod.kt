package org.elnix.dragonlauncher.enumsui.toggle

import org.elnix.dragonlauncher.enumsui.ToggleButtonOption
import org.elnix.dragonlauncher.i18n.R

/**
 * Available methods for locking the settings screen.
 */
public enum class LockMethod(
    override val resId: Int,
    override val iconEnabled: Int? = null,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    /** No lock — settings are freely accessible */
    None(R.string.lock_none),

    /** Require a user-defined PIN code */
    Pin(R.string.lock_pin),

    /** Use native Android device unlock (biometric + device credentials fallback) */
    Device(R.string.lock_device_unlock)
}
