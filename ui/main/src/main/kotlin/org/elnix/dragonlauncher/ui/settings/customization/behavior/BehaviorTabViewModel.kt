package org.elnix.dragonlauncher.ui.settings.customization.behavior

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.model.serializables.GlobalDraggingMode
import org.elnix.dragonlauncher.swipe.SwipeService
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
@Stable
class BehaviorTabViewModel
	@Inject
	constructor(
		private val swipeService: SwipeService
	) : ViewModel() {
		val showPaddingBox = SettingFlow(false)
		val showSecretButtonBox = SettingFlow(false)
		var showLockMethodPicker = mutableStateOf(false)

		val globalDraggingMode = swipeService.globalDraggingMode

		fun setDraggingMode(draggingMode: GlobalDraggingMode?) {
			globalDraggingMode.update { draggingMode }

			if (draggingMode is GlobalDraggingMode.Fixed) {
				swipeService.setFixedOffset(draggingMode)
			}
			swipeService.saveGlobalDraggingMode()
		}

		fun enableSecretButton() {
			viewModelScope.launch {
				showSecretButtonBox.value = true
				delay(1.seconds)
				showSecretButtonBox.value = false
			}
		}
	}
