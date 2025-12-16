package org.elnix.dragonlauncher.utils.models

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BackupViewModel : ViewModel() {
    private val _result = MutableStateFlow<BackupResult?>(null)
    val result = _result.asStateFlow()

    fun setResult(result: BackupResult?) {
        _result.value = result
    }
}

data class BackupResult(
    val export: Boolean,
    val error: Boolean,
    val message: String = ""
)
