package org.elnix.dragonlauncher.ui

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Point
import javax.inject.Inject

@Stable
@HiltViewModel
class MainAppUiViewModel
	@Inject
	constructor(
		application: Application
	) : AndroidViewModel(application)
