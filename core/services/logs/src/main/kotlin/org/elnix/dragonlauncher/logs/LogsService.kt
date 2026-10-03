package org.elnix.dragonlauncher.logs

import android.content.Context
import io.github.elnix90.core.objects.BooleanSettingObject
import io.github.elnix90.logging.FileLoggingTree
import io.github.elnix90.logging.LogAlert
import io.github.elnix90.logging.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.LOGS_TAG
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import timber.log.Timber
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue

public interface LogsService {
	public val alertFlow: StateFlow<LogAlert?>

	public fun updateEnableLogging(enable: Boolean)

	public fun getAllLogFiles(): List<File>

	public fun clearLogs()

	public fun readLogFile(file: File): String

	public fun deleteLogFile(file: File)
}

internal class LogsServiceIml(
	private val ctx: Context
) : LogsService {
	private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	private var fileTree: FileLoggingTree? = null

	private val recentLogs = ConcurrentLinkedQueue<LogAlert>()
	private val _alertFlow = MutableStateFlow<LogAlert?>(null)
	override val alertFlow: StateFlow<LogAlert?> = _alertFlow

	private val enableLogging: BooleanSettingObject = DebugSettingsStore.enableLogging
	private val maxRecentLogs = 50

	init {
		scope.launch {
			fileTree = FileLoggingTree(ctx, ::onHighPriorityLog)

			updateLoggingState()

			launch {
				DebugSettingsStore.snackBarLogLevel.flow(ctx).collect {
					fileTree?.snackBarLogLevel = it
				}
			}

			launch {
				DebugSettingsStore.filesLogLevel.flow(ctx).collect {
					fileTree?.filesLogsLevel = it
				}
			}

			launch {
				DebugSettingsStore.filterTag.flow(ctx).collect {
					fileTree?.filterTag = it
				}
			}
		}
	}

	private fun onHighPriorityLog(level: Int, message: String) {
		val alert = LogAlert(level, message)
		recentLogs.add(alert)
		if (recentLogs.size > maxRecentLogs) {
			recentLogs.poll()
		}
		_alertFlow.value = alert
	}

	override fun updateEnableLogging(enable: Boolean) {
		scope.launch {
			if (enableLogging.get(ctx) == enable) {
				return@launch
			}

			DebugSettingsStore.enableLogging.set(ctx, enable)
			updateLoggingState()
		}
	}

	private suspend fun updateLoggingState() {
		val tree = fileTree ?: return
		val plantedTrees = Timber.forest()
		if (enableLogging.get(ctx)) {
			if (tree !in plantedTrees) {
				Timber.plant(tree)
			}
		} else {
			if (tree in plantedTrees) {
				Timber.uproot(tree)
			}
		}
	}

	override fun getAllLogFiles(): List<File> = fileTree?.getAllLogFiles() ?: emptyList()

	override fun clearLogs() {
		fileTree?.clearAllLogs()
		recentLogs.clear()
		_alertFlow.value = null
	}

	override fun readLogFile(file: File): String =
		try {
			file.readText()
		} catch (e: Exception) {
			logE(LOGS_TAG, e) { "Failed to read log file: ${file.absolutePath}" }
			"Failed to read log file: $e"
		}

	override fun deleteLogFile(file: File) {
		try {
			file.delete()
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}
