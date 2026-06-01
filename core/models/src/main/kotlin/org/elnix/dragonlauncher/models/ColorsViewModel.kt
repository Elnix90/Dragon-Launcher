package org.elnix.dragonlauncher.models

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.elnix.dragonlauncher.colors.ColorService
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import javax.inject.Inject

@HiltViewModel
class ColorsViewModel @Inject constructor(
    val colorService: ColorService
) : ViewModel() {
    init {
        viewModelInitialized()
    }
}