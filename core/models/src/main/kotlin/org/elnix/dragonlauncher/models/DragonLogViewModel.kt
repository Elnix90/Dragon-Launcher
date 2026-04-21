package org.elnix.dragonlauncher.models

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.utils.Constants.Logging.LOGS_TAG
import org.elnix.dragonlauncher.logging.FileLoggingTree
import org.elnix.dragonlauncher.logging.LogAlert
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.settings.stores.DebugSettingsStore
import timber.log.Timber
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue

class DragonLogViewModel(
    application: Application
) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext
    private val _isLoggingEnabled = MutableStateFlow(true)
    val isLoggingEnabled = _isLoggingEnabled.asStateFlow()

    private val _snackBarLogLevel = MutableStateFlow(7) // No Logging
    val snackBarLogLevel = _snackBarLogLevel.asStateFlow()
    private val _filesLogsLevel = MutableStateFlow(Log.DEBUG)
    val filesLogsLevel = _filesLogsLevel.asStateFlow()

    private val _filterTag = MutableStateFlow("")
    val filterTag = _filterTag.asStateFlow()

    private var fileTree: FileLoggingTree? = null


    private val recentLogs = ConcurrentLinkedQueue<LogAlert>()
    private val _alertFlow = MutableStateFlow<LogAlert?>(null)
    val alertFlow: StateFlow<LogAlert?> = _alertFlow


    private val maxRecentLogs = 50

    init {
        Timber.plant(Timber.DebugTree())

        viewModelScope.launch {
            fileTree = FileLoggingTree(ctx, ::onHighPriorityLog)

            _isLoggingEnabled.value = DebugSettingsStore.enableLogging.get(ctx)

            _snackBarLogLevel.value = DebugSettingsStore.snackBarLogLevel.get(ctx)
            fileTree?.snackBarLogLevel = _snackBarLogLevel.value

            _filesLogsLevel.value = DebugSettingsStore.filesLogLevel.get(ctx)
            fileTree?.filesLogsLevel = _filesLogsLevel.value

            _filterTag.value = DebugSettingsStore.filterTag.get(ctx)
            fileTree?.filterTag = filterTag.value
        }

        updateLoggingState()
    }

    private fun onHighPriorityLog(level: Int, message: String) {
        val alert = LogAlert(level, message)
        recentLogs.add(alert)
        if (recentLogs.size > maxRecentLogs) {
            recentLogs.poll()
        }
        _alertFlow.value = alert
    }

    fun updateEnableLogging(enable: Boolean) {
        if (_isLoggingEnabled.value == enable) return

        _isLoggingEnabled.value = enable
        viewModelScope.launch {
            DebugSettingsStore.enableLogging.set(ctx, enable)
        }

        updateLoggingState()
    }


    fun updateSnackBarLogLevel(newLevel: Int) {
        _snackBarLogLevel.value = newLevel
        fileTree?.snackBarLogLevel = newLevel
        viewModelScope.launch {
            DebugSettingsStore.snackBarLogLevel.set(ctx, newLevel)
        }
    }

    fun updateFilesLogLevel(newLevel: Int) {
        _filesLogsLevel.value = newLevel
        fileTree?.filesLogsLevel = newLevel
        viewModelScope.launch {
            DebugSettingsStore.filesLogLevel.set(ctx, newLevel)
        }
    }

    fun updateFilterTag(newTag: String) {
        _filterTag.value = newTag
        fileTree?.filterTag = newTag
        viewModelScope.launch {
            DebugSettingsStore.filterTag.set(ctx, newTag)
        }
    }

    private fun updateLoggingState() {
        val tree = fileTree ?: return
        val plantedTrees = Timber.forest()
        if (_isLoggingEnabled.value) {
            if (tree !in plantedTrees) {
                Timber.plant(tree)
            }
        } else {
            if (tree in plantedTrees) {
                Timber.uproot(tree)
            }
        }
    }

    fun getAllLogFiles(): List<File> {
        return fileTree?.getAllLogFiles() ?: emptyList()
    }

    fun clearLogs() {
        fileTree?.clearAllLogs()
        recentLogs.clear()
        _alertFlow.value = null
    }

    fun readLogFile(file: File): String {
        return try {
            file.readText()
        } catch (e: Exception) {
            logE(LOGS_TAG, e) { "Failed to read log file: ${file.absolutePath}"}
            "Failed to read log file: $e"
        }
    }

    fun deleteLogFile(file: File) {
        try {
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}