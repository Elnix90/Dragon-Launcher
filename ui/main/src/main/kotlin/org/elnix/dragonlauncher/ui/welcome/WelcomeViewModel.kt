package org.elnix.dragonlauncher.ui.welcome

import android.app.Application
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

const val PAGES_NUMBER = 4

@HiltViewModel
@Stable
class WelcomeViewModel
	@Inject
	constructor(
		application: Application
	) : AndroidViewModel(application) {
		val pagerState = PagerState { PAGES_NUMBER }

		fun setAsSeen() {
			viewModelScope.launch {
				PrivateSettingsStore.hasSeenWelcomeScreen.set(application, true)

				// Do not reset the pager state instantly because otherwise the animation would be ugly
				delay(3.seconds)
				pagerState.scrollToPage(0)
			}
		}
	}
