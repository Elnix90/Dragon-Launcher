package org.elnix.dragonlauncher.models

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor() : ViewModel() {
    private val _result = MutableStateFlow<BackupResult?>(null)
    val result = _result.asStateFlow()

    fun setResult(result: BackupResult?) {
        _result.value = result
    }

    init {
        viewModelInitialized()
    }
}

data class BackupResult(
    val export: Boolean,
    val error: Boolean,
    val title: String,
    val message: String = ""
)
