package org.elnix.dragonlauncher.models

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.shizuku.ShizukuService
import javax.inject.Inject

@Stable
@HiltViewModel
public class ShizukuViewModel
	@Inject
	constructor(
		public val shizukuService: ShizukuService
	) : ViewModel() {
		init {
			viewModelInitialized()
		}
	}
