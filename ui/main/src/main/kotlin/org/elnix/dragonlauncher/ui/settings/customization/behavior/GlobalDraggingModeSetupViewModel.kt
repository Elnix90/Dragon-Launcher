package org.elnix.dragonlauncher.ui.settings.customization.behavior

import android.app.Application
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.model.serializables.GlobalDraggingMode
import org.elnix.dragonlauncher.swipe.SwipeService

@HiltViewModel
@Stable
class GlobalDraggingModeSetupViewModel
	@Inject
	constructor(
		application: Application,
		private val swipeService: SwipeService
	) : AndroidViewModel(application) {
		val offset = SettingFlow(swipeService.fixedOffset.value ?: Offset.Zero)

		fun save() {
			swipeService.globalDraggingMode.update { mode ->
				when (mode) {
					is GlobalDraggingMode.Fixed -> {
						val screenWidth = application.resources.displayMetrics.widthPixels
						val screenHeight = application.resources.displayMetrics.heightPixels

						val offset = offset.value
						val xRatio = offset.x / screenWidth
						val yRatio = offset.y / screenHeight

						val newMode = GlobalDraggingMode.Fixed(
							xRatio = xRatio,
							yRatio = yRatio
						)

						swipeService.setFixedOffset(newMode)

						newMode
					}

					GlobalDraggingMode.Normal -> {
						mode
					}
				}
			}
			swipeService.saveGlobalDraggingMode()
		}

		suspend fun PointerInputScope.draggingGesture() {
			detectDragGestures(
				onDragStart = { startPos ->
					offset.value = startPos
				},
				onDragEnd = {},
				onDragCancel = {},
				onDrag = { change, _ ->
					offset.value = change.position
				}
			)
		}
	}
