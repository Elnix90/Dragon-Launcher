package org.elnix.dragonlauncher.shizuku

import android.content.Context
import io.github.elnix90.logging.logD
import io.github.elnix90.shared.base.SettingFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.SHIZUKU_TAG
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.ktx.showToast
import rikka.shizuku.Shizuku

interface ShizukuService {
	val outputValue: SettingFlow<OutputLine?>

	val showUnavailable: StateFlow<Boolean>

	fun clearOutput()

	fun shizukuPermissionState(): StateFlow<Boolean>

	fun requestShizukuPermission()

	fun executeShizukuCommand(command: String)

	fun setUnavailable()

	fun dismissUnavailableDialog()

	fun runShisukuCommandNotEmpty(command: Action.RunAdbCommand)
}

internal class ShizukuServiceImpl(
	private val ctx: Context
) : ShizukuService {
	private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	private val shellCommandExecutor = ShellCommandExecutor()
	private val shizukuPermissionHandler = ShizukuPermissionHandler()

	override val outputValue: SettingFlow<OutputLine?> = SettingFlow(null)

	private val _showUnavailable = MutableStateFlow(false)
	override val showUnavailable: StateFlow<Boolean> = _showUnavailable.asStateFlow()

	override fun clearOutput() {
		outputValue.value = null
	}

	override fun shizukuPermissionState(): StateFlow<Boolean> = shizukuPermissionHandler.permissionGranted

	override fun requestShizukuPermission(): Unit = shizukuPermissionHandler.requestPermission()

	override fun executeShizukuCommand(command: String) {
		scope.launch {
			shellCommandExecutor
				.runShizuku(command)
				.collect { outputLine ->
					outputValue.value = outputLine
				}
		}
	}

	override fun setUnavailable() {
		_showUnavailable.value = true
	}

	override fun dismissUnavailableDialog() {
		_showUnavailable.value = false
	}

	override fun runShisukuCommandNotEmpty(command: Action.RunAdbCommand) {
		if (!Shizuku.pingBinder()) {
			logD(SHIZUKU_TAG) { "Shizuku is not running, opening it..." }
			setUnavailable()
			return
		}

		if (!shizukuPermissionHandler.permissionGranted.value) {
			logD(SHIZUKU_TAG) { "Shizuku his not allowed" }

			requestShizukuPermission()
		} else {
			logD(SHIZUKU_TAG) { "Shizuku tries to run the command: $command" }
			if (command.toast == true) {
				ctx.showToast("Running: $command")
			}
			executeShizukuCommand(command.command)
		}
	}
}
