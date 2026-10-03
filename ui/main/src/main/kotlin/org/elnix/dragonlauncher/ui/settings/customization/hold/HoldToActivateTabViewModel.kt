package org.elnix.dragonlauncher.ui.settings.customization.hold

import android.app.Application
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
@Stable
class HoldToActivateTabViewModel
	@Inject
	constructor(
		application: Application
	) : AndroidViewModel(application) {
		var showHoldSettingsOrderDialog = mutableStateOf(false)
		var playAnimation = mutableStateOf(true)
		var manualMode = mutableStateOf(false)

		val progress = Animatable(0f)

		var height = mutableIntStateOf(0)
		var isFirstPositioning = mutableStateOf(true)
	}
