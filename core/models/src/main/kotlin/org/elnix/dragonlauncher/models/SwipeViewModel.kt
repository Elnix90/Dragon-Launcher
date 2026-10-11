package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.swipe.SwipeService
import javax.inject.Inject

@Stable
@HiltViewModel
class SwipeViewModel
	@Inject
	constructor(
		application: Application,
		val swipeService: SwipeService
	) : AndroidViewModel(application) {
		init {
			viewModelInitialized()
		}
	}
