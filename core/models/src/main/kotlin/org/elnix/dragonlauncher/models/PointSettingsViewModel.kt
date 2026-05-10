package org.elnix.dragonlauncher.models

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore
import javax.inject.Inject

/**
 * Point settings view model, responsible for holding different values related to the point settings screen
 * @param application
 */
@HiltViewModel
class PointSettingsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    private val _showAdvancedPointTools = MutableStateFlow(false)
    val showAdvancedPointTools = _showAdvancedPointTools.asStateFlow()


    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext



    init {
        viewModelScope.launch {
            loadAdvancedPointsTools()
        }
    }

    suspend fun loadAdvancedPointsTools() {
        _showAdvancedPointTools.value = UiSettingsStore.showAdvancedPointTools.get(ctx)
    }

    fun toggleAdvancedPointsTools(enabled: Boolean) {
        _showAdvancedPointTools.value = enabled
        viewModelScope.launch {
            UiSettingsStore.showAdvancedPointTools.set(ctx, enabled)
        }
    }
}
