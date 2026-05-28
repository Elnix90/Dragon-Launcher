//package org.elnix.dragonlauncher.models.viewModelValue

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ViewModelValue<T>(
    initialValue: T,
    private val setData: suspend (T) -> Unit,
    private val loadData: suspend () -> T
) {
    private val _value = MutableStateFlow(initialValue)
    val value = _value.asStateFlow()

    fun update(newValue: T) {
        _value.update { newValue }
        set(newValue)
    }

    suspend fun load(): T = loadData()
    suspend fun set(v: T) = setData(v)
}
