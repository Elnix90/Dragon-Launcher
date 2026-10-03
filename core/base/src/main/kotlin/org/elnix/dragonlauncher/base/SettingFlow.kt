package org.elnix.dragonlauncher.base

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

public class SettingFlow<T>(
	private val default: T
) {
	private val mutableFlow = MutableStateFlow(default)
	public val flow: StateFlow<T> = mutableFlow.asStateFlow()

	public var value: T
		get() = mutableFlow.value
		set(newValue) {
			mutableFlow.value = newValue
		}

	public fun update(newValue: (T) -> T?) {
		val newValue = newValue(mutableFlow.value)
		if (newValue == null) {
			reset()
		} else {
			mutableFlow.value = newValue
		}
	}

	public fun reset() {
		mutableFlow.value = default
	}
}
