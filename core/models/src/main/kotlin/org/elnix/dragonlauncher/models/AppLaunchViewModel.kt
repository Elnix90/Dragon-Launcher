package org.elnix.dragonlauncher.models

import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.elnix.dragonlauncher.applaunch.AppLaunchService
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import javax.inject.Inject

@Stable
@HiltViewModel
class AppLaunchViewModel
	@Inject
	constructor(
		application: android.app.Application,
		val appLaunchService: AppLaunchService
	) : AndroidViewModel(application) {
		init {
			viewModelInitialized()
		}
	}
