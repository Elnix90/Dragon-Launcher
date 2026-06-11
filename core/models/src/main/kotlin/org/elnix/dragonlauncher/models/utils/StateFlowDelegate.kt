package org.elnix.dragonlauncher.models.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.reflect.KProperty

data class StateFlowWrapper<T>(
    val flow: StateFlow<T>,
    val value: T,
    val set: (T?) -> Unit
)

abstract class StateFlowDelegate<T>(
    private val default: T
) {
    protected val mutableFlow = MutableStateFlow(default)
    val flow: StateFlow<T> = mutableFlow.asStateFlow()

    abstract fun loadValue()

    open fun setValue(value: T) {
        mutableFlow.value = value
    }

    private fun get() = mutableFlow.value

    private var initialized = false

    operator fun getValue(thisRef: Any?, property: KProperty<*>): StateFlowWrapper<T> {
        if (!initialized) {
            initialized = true
            loadValue()
        }
        return StateFlowWrapper(flow, get()) { value ->
            setValue(value ?: default)
        }
    }

//    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = setValue(value)
}



class BasicObjectDelegate<T>(
    default: T
) : StateFlowDelegate<T>(default) {
    override fun loadValue() { /* no-op */
    }
}

fun <T> stateFlowDelegate(default: T) = BasicObjectDelegate(default)
