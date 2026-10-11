package org.elnix.dragonlauncher.ui.settings.debug

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.elnix.dragonlauncher.permissions.PermissionGroup
import org.elnix.dragonlauncher.permissions.PermissionsManager
import javax.inject.Inject

@Stable
@HiltViewModel
class DebugTabViewModel
	@Inject
	constructor(
		private val permissionsManager: PermissionsManager
	) : ViewModel() {
		val notificationsPermissions = permissionsManager.hasPermission(PermissionGroup.Notifications)

		fun requestNotificationsPermission() {
			permissionsManager.requestPermission(PermissionGroup.Notifications)
		}

		var packageResult = mutableStateOf<String?>(null)
		var showOverlayPermissionDialog = mutableStateOf(false)

		var packageQuery = mutableStateOf("")

		var customSystemPackage = mutableStateOf("")
	}
