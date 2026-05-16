package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.TAG
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.shizuku.OutputLine
import org.elnix.dragonlauncher.shizuku.ShellCommandExecutor
import org.elnix.dragonlauncher.shizuku.ShizukuPermissionHandler
import javax.inject.Inject


@HiltViewModel
class ShizukuViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    private val shellCommandExecutor = ShellCommandExecutor()
    private val shizukuPermissionHandler = ShizukuPermissionHandler()

    private val _output = MutableStateFlow<OutputLine?>(null)
    val outputValue = _output.asStateFlow()

    private val _showUnavailable = MutableStateFlow(false)
    val showUnavailable = _showUnavailable.asStateFlow()


    fun clearOutput() {
        _output.value = null
    }

    fun shizukuPermissionState(): StateFlow<Boolean> {
        return shizukuPermissionHandler.permissionGranted
    }

    fun requestShizukuPermission() {
        return shizukuPermissionHandler.requestPermission()
    }

    fun executeShizukuCommand(command: String) {
        viewModelScope.launch {
            shellCommandExecutor.runShizuku(command)
                .collect { outputLine ->
                    _output.value = outputLine
                }
        }
    }

    fun setUnavailable() {
        _showUnavailable.value = true
    }

    fun dismissUnavailableDialog() {
        _showUnavailable.value = false
    }

    init {
        logD(TAG) { "created ShizukuVM ${System.identityHashCode(this)}" }
    }
}