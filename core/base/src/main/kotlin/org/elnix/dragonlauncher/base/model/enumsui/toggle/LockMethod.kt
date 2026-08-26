package org.elnix.dragonlauncher.base.model.enumsui.toggle

import org.elnix.dragonlauncher.base.model.enumsui.ToggleButtonOption
import org.elnix.dragonlauncher.i18n.R

/**
 * Available methods for locking things
 */
public enum class LockMethod(
    override val resId: Int,
    override val iconEnabled: Int? = null,
    override val iconDisabled: Int? = null
) : ToggleButtonOption {
    /** No lock */
    None(R.string.lock_none),

    /** Require a user-defined PIN code */
    Pin(R.string.lock_pin),

    /** A pattern unlock, size is configurable **/
    Pattern(R.string.pattern),

    /** Use native Android device unlock (biometric + device credentials fallback) */
    Device(R.string.lock_device_unlock)
}
