package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.widgets.WidgetsService
import javax.inject.Inject

@Stable
@HiltViewModel
class WidgetsViewModel
	@Inject
	constructor(
		application: Application,
		val widgetsService: WidgetsService,
		private val appsRepository: AppRepository
	) : AndroidViewModel(application) {
		init {
			viewModelInitialized()
		}

		fun findOne(action: Action.LaunchApp): Flow<org.elnix.dragonlauncher.base.model.models.Application?> =
			appsRepository.findOne(
				action
			)
	}
