package org.elnix.dragonlauncher.models

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.elnix.dragonlauncher.base.theme.AmoledDragonColorScheme
import org.elnix.dragonlauncher.base.theme.DefaultExtraColors
import org.elnix.dragonlauncher.base.theme.ExtraColors
import org.elnix.dragonlauncher.colors.ColorService
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import javax.inject.Inject

@Stable
@HiltViewModel
public class ColorsViewModel @Inject constructor(colorService: ColorService) : ViewModel() {

    init {
        viewModelInitialized()
    }

    public val colorscheme: StateFlow<ColorScheme> = colorService.colors.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AmoledDragonColorScheme
    )

    public val extraColors: StateFlow<ExtraColors> = colorService.extraColors.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = DefaultExtraColors
    )
}