package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.elnix90.core.objects.BooleanSettingObject
import io.github.elnix90.logging.FileLoggingTree
import io.github.elnix90.logging.LOGS_TAG
import io.github.elnix90.logging.LogAlert
import io.github.elnix90.logging.logE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import timber.log.Timber
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

@HiltViewModel
public class DragonLogViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    private var fileTree: FileLoggingTree? = null

    private val recentLogs = ConcurrentLinkedQueue<LogAlert>()
    private val _alertFlow = MutableStateFlow<LogAlert?>(null)
    public val alertFlow: StateFlow<LogAlert?> = _alertFlow


    private val enableLogging: BooleanSettingObject = DebugSettingsStore.enableLogging

    private val maxRecentLogs = 50


    init {
        viewModelScope.launch {
            fileTree = FileLoggingTree(application, ::onHighPriorityLog)

            DebugSettingsStore.snackBarLogLevel.flow(application).collect {
                fileTree?.snackBarLogLevel = it
            }

            DebugSettingsStore.filesLogLevel.flow(application).collect {
                fileTree?.filesLogsLevel = it
            }

            DebugSettingsStore.filterTag.flow(application).collect {
                fileTree?.filterTag = it
            }

            updateLoggingState()
        }
        viewModelInitialized()
    }

    private fun onHighPriorityLog(level: Int, message: String) {
        val alert = LogAlert(level, message)
        recentLogs.add(alert)
        if (recentLogs.size > maxRecentLogs) {
            recentLogs.poll()
        }
        _alertFlow.value = alert
    }

    public fun updateEnableLogging(enable: Boolean) {
        viewModelScope.launch {

            if (enableLogging.get(application) == enable) {
                return@launch
            }

            DebugSettingsStore.enableLogging.set(application, enable)
            updateLoggingState()
        }
    }

//    public fun updateSnackBarLogLevel(newLevel: Int) {
//        fileTree?.snackBarLogLevel = newLevel
//        viewModelScope.launch {
//            DebugSettingsStore.snackBarLogLevel.set(application, newLevel)
//        }
//    }
//
//    public fun updateFilesLogLevel(newLevel: Int) {
//        fileTree?.filesLogsLevel = newLevel
//        viewModelScope.launch {
//            DebugSettingsStore.filesLogLevel.set(application, newLevel)
//        }
//    }
//
//    public fun updateFilterTag(newTag: String) {
//        fileTree?.filterTag = newTag
//        viewModelScope.launch {
//            DebugSettingsStore.filterTag.set(application, newTag)
//        }
//    }

    private suspend fun updateLoggingState() {
        val tree = fileTree ?: return
        val plantedTrees = Timber.forest()
        if (enableLogging.get(application)) {
            if (tree !in plantedTrees) {
                Timber.plant(tree)
            }
        } else {
            if (tree in plantedTrees) {
                Timber.uproot(tree)
            }
        }
    }

    public fun getAllLogFiles(): List<File> {
        return fileTree?.getAllLogFiles() ?: emptyList()
    }

    public fun clearLogs() {
        fileTree?.clearAllLogs()
        recentLogs.clear()
        _alertFlow.value = null
    }

    public fun readLogFile(file: File): String {
        return try {
            file.readText()
        } catch (e: Exception) {
            logE(LOGS_TAG, e) { "Failed to read log file: ${file.absolutePath}" }
            "Failed to read log file: $e"
        }
    }

    public fun deleteLogFile(file: File) {
        try {
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}