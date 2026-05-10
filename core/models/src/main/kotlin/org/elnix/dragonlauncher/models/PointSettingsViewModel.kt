package org.elnix.dragonlauncher.models

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.settings.stores.SwipeMapSettingsStore
import javax.inject.Inject

/**
 * Point settings view model, responsible for holding different values related to the point settings screen
 * @param application
 */
@HiltViewModel
class PointSettingsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext


    private val _showSubNestSlider = MutableStateFlow(false)
    val showSubNestSlider = _showSubNestSlider.asStateFlow()
    suspend fun loadShowSubNestSlider() {
        _showSubNestSlider.value = SwipeMapSettingsStore.showSubNestsSlider.get(ctx)
    }
    fun toggleShowSubNestSlider() {
        val newValue = _showAdvancedPointTools.updateAndGet { !it }
        viewModelScope.launch {
            SwipeMapSettingsStore.showSubNestsSlider.set(ctx, newValue)
        }
    }




    private val _showAdvancedPointTools = MutableStateFlow(false)
    val showAdvancedPointTools = _showAdvancedPointTools.asStateFlow()
    suspend fun loadAdvancedPointsTools() {
        _showAdvancedPointTools.value = SwipeMapSettingsStore.showAdvancedPointTools.get(ctx)
    }
    fun toggleAdvancedPointsTools() {
        val newValue = _showAdvancedPointTools.updateAndGet { !it }
        viewModelScope.launch {
            SwipeMapSettingsStore.showAdvancedPointTools.set(ctx, newValue)
        }
    }


    private val _isInDragAroundMode = MutableStateFlow(false)
    val isInDragAroundMode = _isInDragAroundMode.asStateFlow()
    suspend fun loadIsInDragAroundMode() {
        _isInDragAroundMode.value = SwipeMapSettingsStore.isInDragAroundMode.get(ctx)
    }
    fun toggleIsInDragAroundMode() {
        val newValue = _isInDragAroundMode.updateAndGet { !it }
        viewModelScope.launch {
            SwipeMapSettingsStore.isInDragAroundMode.set(ctx, newValue)
        }
    }

    init {
        viewModelScope.launch {
            loadAdvancedPointsTools()
            loadShowSubNestSlider()
            loadIsInDragAroundMode()
        }
    }
}
