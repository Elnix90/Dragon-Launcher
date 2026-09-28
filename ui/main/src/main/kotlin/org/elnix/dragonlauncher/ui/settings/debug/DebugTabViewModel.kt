package org.elnix.dragonlauncher.ui.settings.debug

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@Stable
@HiltViewModel
class DebugTabViewModel
	@Inject
	constructor() : ViewModel() {
		var packageResult = mutableStateOf<String?>(null)
		var showOverlayPermissionDialog = mutableStateOf(false)

		var packageQuery = mutableStateOf("")

		var customSystemPackage = mutableStateOf("")
	}
