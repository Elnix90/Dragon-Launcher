package org.elnix.dragonlauncher.models

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.elnix.dragonlauncher.colors.ColorService
import javax.inject.Inject

@HiltViewModel
class ColorsViewModel @Inject constructor(
    val colorService: ColorService
) : ViewModel() {
}