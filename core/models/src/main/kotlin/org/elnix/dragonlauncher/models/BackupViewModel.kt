package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.elnix.dragonlauncher.backup.BackupService
import javax.inject.Inject

@Stable
@HiltViewModel
public class BackupViewModel
	@Inject
	constructor(
		application: Application,
		public val backupService: BackupService
	) : AndroidViewModel(application)
