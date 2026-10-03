package org.elnix.dragonlauncher.models

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.elnix.dragonlauncher.colors.ColorService
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import javax.inject.Inject

@Stable
@HiltViewModel
public class ColorsViewModel
	@Inject
	constructor(
		public val colorService: ColorService
	) : ViewModel() {
		init {
			viewModelInitialized()
		}
	}
