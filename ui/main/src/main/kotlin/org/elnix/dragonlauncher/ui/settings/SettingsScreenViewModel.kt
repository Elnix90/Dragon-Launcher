package org.elnix.dragonlauncher.ui.settings

import android.app.Application
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.elnix.dragonlauncher.base.utils.VersionsUtils.isBetaVersion
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel
	@Inject
	constructor(
		application: Application
	) : AndroidViewModel(application) {
		var showLanguageSheet = mutableStateOf(false)

		var showBottomSearch = mutableStateOf(false)
		val textFieldState = TextFieldState()

		private val hideBetaVersionWarning = PrivateSettingsStore.hideBetaVersionWarning.stateFlow(application, viewModelScope)
		val showBetaVersionWarning = hideBetaVersionWarning
			.map {
				it && application.isBetaVersion()
			}.stateIn(
				scope = viewModelScope,
				started = SharingStarted.Eagerly,
				initialValue = false
			)
	}
