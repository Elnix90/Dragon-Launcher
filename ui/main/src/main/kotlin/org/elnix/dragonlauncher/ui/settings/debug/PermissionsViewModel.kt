package org.elnix.dragonlauncher.ui.settings.debug

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import org.elnix.dragonlauncher.permissions.PermissionGroup
import org.elnix.dragonlauncher.permissions.PermissionsManager
import javax.inject.Inject

@Stable
@HiltViewModel
class PermissionsViewModel
	@Inject
	constructor(
		private val permissionsManager: PermissionsManager
	) : ViewModel() {
		/**
		 * Granted state of every group, so the tab can render the whole list from
		 * the single source of truth.
		 */
		val states: Map<PermissionGroup, StateFlow<Boolean>> =
			PermissionGroup.entries.associateWith { group ->
				permissionsManager.hasPermission(group)
			}

		/**
		 * Opens the system screen where the group is granted.
		 *
		 * The manager tracks the foreground activity itself, so a view model does
		 * not need one to make a request.
		 */
		fun requestPermission(permissionGroup: PermissionGroup) {
			permissionsManager.requestPermission(permissionGroup)
		}
	}
