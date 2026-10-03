package org.elnix.dragonlauncher.models

import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.elnix.dragonlauncher.applaunch.AppLaunchService
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import javax.inject.Inject

@Stable
@HiltViewModel
public class AppLaunchViewModel
	@Inject
	constructor(
		application: android.app.Application,
		public val appLaunchService: AppLaunchService
	) : AndroidViewModel(application) {
		init {
			viewModelInitialized()
		}
	}
