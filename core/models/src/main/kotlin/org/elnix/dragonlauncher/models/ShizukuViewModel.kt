package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.shizuku.OutputLine
import org.elnix.dragonlauncher.shizuku.ShellCommandExecutor
import org.elnix.dragonlauncher.shizuku.ShizukuPermissionHandler
import javax.inject.Inject


@HiltViewModel
public class ShizukuViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    private val shellCommandExecutor = ShellCommandExecutor()
    private val shizukuPermissionHandler = ShizukuPermissionHandler()

    public val outputValue: SettingFlow<OutputLine?> = SettingFlow(null)

    private val _showUnavailable = MutableStateFlow(false)
    public val showUnavailable: StateFlow<Boolean> = _showUnavailable.asStateFlow()


    public fun clearOutput() {
        outputValue.value = null
    }

    public fun shizukuPermissionState(): StateFlow<Boolean> {
        return shizukuPermissionHandler.permissionGranted
    }

    public fun requestShizukuPermission() {
        return shizukuPermissionHandler.requestPermission()
    }

    public fun executeShizukuCommand(command: String) {
        viewModelScope.launch {
            shellCommandExecutor.runShizuku(command)
                .collect { outputLine ->
                    outputValue.value = outputLine
                }
        }
    }

    public fun setUnavailable() {
        _showUnavailable.value = true
    }

    public fun dismissUnavailableDialog() {
        _showUnavailable.value = false
    }

    init {
        viewModelInitialized()
    }
}