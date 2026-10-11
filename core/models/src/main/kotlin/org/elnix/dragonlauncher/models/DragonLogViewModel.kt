package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.elnix.dragonlauncher.logs.LogsService
import javax.inject.Inject

@Stable
@HiltViewModel
class DragonLogViewModel
	@Inject
	constructor(
		application: Application,
		val logsService: LogsService
	) : AndroidViewModel(application)
