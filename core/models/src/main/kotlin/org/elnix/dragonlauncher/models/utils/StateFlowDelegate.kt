package org.elnix.dragonlauncher.models.utils

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import kotlin.reflect.KProperty

data class StateFlowWrapper<T>(
    val flow: StateFlow<T>,
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

    private var initialized = false

    operator fun getValue(thisRef: Any?, property: KProperty<*>): StateFlowWrapper<T> {
        if (!initialized) {
            initialized = true
            loadValue()
        }
        return StateFlowWrapper(flow) { value ->
            setValue(value ?: default)
        }
    }

//    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = setValue(value)
}

class SettingObjectDelegate<T>(
    private val viewModelLazy: Lazy<AndroidViewModel>,
    private val settingObject: BaseSettingObject<T, *>,
) : StateFlowDelegate<T>(settingObject.default) {

    private val viewModel: AndroidViewModel get() = viewModelLazy.value

    override fun loadValue() {
        viewModel.viewModelScope.launch {
            mutableFlow.value = settingObject.get(viewModel.getApplication<Application>().applicationContext)
        }
    }

    override fun setValue(value: T) {
        super.setValue(value)
        viewModel.viewModelScope.launch {
            settingObject.set(viewModel.getApplication<Application>().applicationContext, value)
        }
    }
}

class BasicObjectDelegate<T>(
    default: T
) : StateFlowDelegate<T>(default) {
    override fun loadValue() { /* no-op */
    }
}


fun <T> AndroidViewModel.stateFlowDelegate(settingObject: BaseSettingObject<T, *>) =
    SettingObjectDelegate(
        viewModelLazy = lazy { this },
        settingObject = settingObject
    )

fun <T> stateFlowDelegate(default: T) = BasicObjectDelegate(default)
