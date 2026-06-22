package org.elnix.dragonlauncher.models

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
class DragonLogViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext
    private var fileTree: FileLoggingTree? = null


    private val recentLogs = ConcurrentLinkedQueue<LogAlert>()
    private val _alertFlow = MutableStateFlow<LogAlert?>(null)
    val alertFlow: StateFlow<LogAlert?> = _alertFlow


    val enableLogging = DebugSettingsStore.enableLogging

    private val maxRecentLogs = 50


    init {
        viewModelScope.launch {
            fileTree = FileLoggingTree(ctx, ::onHighPriorityLog)
            fileTree?.snackBarLogLevel = DebugSettingsStore.snackBarLogLevel.get(ctx)
            fileTree?.filesLogsLevel = DebugSettingsStore.filesLogLevel.get(ctx)
            fileTree?.filterTag = DebugSettingsStore.filterTag.get(ctx)

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

    fun updateEnableLogging(enable: Boolean) {
        viewModelScope.launch {

            if (enableLogging.get(application) == enable) {
                return@launch
            }

            DebugSettingsStore.enableLogging.set(ctx, enable)
            updateLoggingState()
        }
    }

    fun updateSnackBarLogLevel(newLevel: Int) {
        fileTree?.snackBarLogLevel = newLevel
        viewModelScope.launch {
            DebugSettingsStore.snackBarLogLevel.set(ctx, newLevel)
        }
    }

    fun updateFilesLogLevel(newLevel: Int) {
        fileTree?.filesLogsLevel = newLevel
        viewModelScope.launch {
            DebugSettingsStore.filesLogLevel.set(ctx, newLevel)
        }
    }

    fun updateFilterTag(newTag: String) {
        fileTree?.filterTag = newTag
        viewModelScope.launch {
            DebugSettingsStore.filterTag.set(ctx, newTag)
        }
    }

    private suspend fun updateLoggingState() {
        val tree = fileTree ?: return
        val plantedTrees = Timber.forest()
        if (enableLogging.get(application.applicationContext)) {
            if (tree !in plantedTrees) {

                Log.e("TEST", "Planting $tree")
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
            logE(LOGS_TAG, e) { "Failed to read log file: ${file.absolutePath}" }
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