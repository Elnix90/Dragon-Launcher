package org.elnix.dragonlauncher.models

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.elnix.dragonlauncher.lifecycle.LifecycleService
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import javax.inject.Inject

@Stable
@HiltViewModel
class AppLifecycleViewModel
	@Inject
	constructor(
		val lifecycleService: LifecycleService
	) : ViewModel() {
		init {
			viewModelInitialized()
		}
	}
