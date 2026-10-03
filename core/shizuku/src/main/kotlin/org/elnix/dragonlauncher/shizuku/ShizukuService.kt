package org.elnix.dragonlauncher.shizuku

import android.content.Context
import io.github.elnix90.logging.logD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.SHIZUKU_TAG
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.ktx.showToast
import rikka.shizuku.Shizuku

public interface ShizukuService {
	public val outputValue: SettingFlow<OutputLine?>

	public val showUnavailable: StateFlow<Boolean>

	public fun clearOutput()

	public fun shizukuPermissionState(): StateFlow<Boolean>

	public fun requestShizukuPermission()

	public fun executeShizukuCommand(command: String)

	public fun setUnavailable()

	public fun dismissUnavailableDialog()

	public fun runShisukuCommandNotEmpty(command: Action.RunAdbCommand)
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
