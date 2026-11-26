package org.elnix.dragonlauncher.ui.welcome

import android.app.Application
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

const val PAGES_NUMBER = 3

@HiltViewModel
@Stable
class WelcomeViewModel
	@Inject
	constructor(
		application: Application
	) : AndroidViewModel(application) {
		val pagerState = PagerState { PAGES_NUMBER }

		var pullTarget by mutableFloatStateOf(0f)
		var canceling by mutableStateOf(false)
		var launching by mutableStateOf(false)
		var showScrollIndicator by mutableStateOf(false)
		var showScrollIndicatorTwo by mutableStateOf(false)
		var showShapesScrollBar by mutableStateOf(false)
		val exitAnim = Animatable(0f)

		fun setAsSeen() {
			viewModelScope.launch {
				PrivateSettingsStore.hasSeenWelcomeScreen.set(application, true)

				// Do not reset the pager state instantly because otherwise the animation would be ugly
				delay(3.seconds)
				pagerState.scrollToPage(0)
			}
		}
	}
