package org.elnix.dragonlauncher.models.utils

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    init {
        loadValue()
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): StateFlowWrapper<T> =
        StateFlowWrapper(flow) { value ->
            setValue(value ?: default)
        }

//    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = setValue(value)
}

class SettingObjectDelegate<T>(
    private val viewModel: AndroidViewModel,
    private val settingObject: BaseSettingObject<T, *>,
) : StateFlowDelegate<T>(settingObject.default) {

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
        viewModel = this,
        settingObject = settingObject
    )

fun <T> stateFlowDelegate(default: T) = BasicObjectDelegate(default)


@Composable
fun <T> StateFlowWrapper<T>.asState(): State<T> = this.flow.collectAsStateWithLifecycle()

