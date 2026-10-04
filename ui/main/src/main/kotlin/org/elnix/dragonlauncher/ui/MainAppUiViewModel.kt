package org.elnix.dragonlauncher.ui

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@Stable
@HiltViewModel
class MainAppUiViewModel
	@Inject
	constructor(
		application: Application
	) : AndroidViewModel(application)
