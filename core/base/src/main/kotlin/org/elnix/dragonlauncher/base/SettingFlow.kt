package org.elnix.dragonlauncher.base

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingFlow<T>(
	private val default: T
) {
	private val mutableFlow = MutableStateFlow(default)
	val flow: StateFlow<T> = mutableFlow.asStateFlow()

	var value: T
		get() = mutableFlow.value
		set(newValue) {
			mutableFlow.value = newValue
		}

	fun update(newValue: (T) -> T?) {
		val newValue = newValue(mutableFlow.value)
		if (newValue == null) {
			reset()
		} else {
			mutableFlow.value = newValue
		}
	}

	fun reset() {
		mutableFlow.value = default
	}
}
