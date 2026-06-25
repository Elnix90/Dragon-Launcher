package org.elnix.dragonlauncher.base

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.properties.ReadOnlyProperty

public class SettingFlow<T>(default: T) {
    private val mutableFlow = MutableStateFlow(default)
    public val flow: StateFlow<T> = mutableFlow.asStateFlow()

    public var value: T
        get() = mutableFlow.value
        set(newValue) { mutableFlow.value = newValue }
}


public fun <T> settingDelegate(default: T): ReadOnlyProperty<Any?, SettingFlow<T>> {
    val setting = SettingFlow(default)
    return ReadOnlyProperty { _, _ -> setting }
}
