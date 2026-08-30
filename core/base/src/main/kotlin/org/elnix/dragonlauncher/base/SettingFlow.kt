package org.elnix.dragonlauncher.base

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

public class SettingFlow<T>(
    default: T
) {
    private val mutableFlow = MutableStateFlow(default)
    public val flow: StateFlow<T> = mutableFlow.asStateFlow()

    public var value: T
        get() = mutableFlow.value
        set(newValue) {
            mutableFlow.value = newValue
        }

    public fun update(newValue: (T) -> T) {
        mutableFlow.value = newValue(mutableFlow.value)
    }
}
