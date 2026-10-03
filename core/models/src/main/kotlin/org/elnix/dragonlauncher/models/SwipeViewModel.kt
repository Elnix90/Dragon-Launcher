package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.points.NestsNavigationService
import org.elnix.dragonlauncher.swipe.SwipeService
import org.elnix.dragonlauncher.widgets.WidgetsService
import javax.inject.Inject

@Stable
@HiltViewModel
public class SwipeViewModel
	@Inject
	constructor(
		application: Application,
		private val widgetsService: WidgetsService,
		public val swipeService: SwipeService,
		private val nestsNavigationService: NestsNavigationService
	) : AndroidViewModel(application) {
		init {
			viewModelInitialized()
		}
	}
