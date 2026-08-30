package org.elnix.dragonlauncher.models

import androidx.compose.material3.Typography
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.elnix.dragonlauncher.base.theme.Typography
import org.elnix.dragonlauncher.fonts.FontService
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import javax.inject.Inject

@Stable
@HiltViewModel
public class FontViewModel
    @Inject
    constructor(
        fontService: FontService
    ) : ViewModel() {
        public val fontFamily: StateFlow<FontFamily> =
            fontService.fontFamily
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = FontFamily.Default
                )

        public val typography: StateFlow<Typography> =
            fontFamily
                .map { fontFamily ->
                    Typography.copy(
                        displayLarge = Typography.displayLarge.copy(fontFamily = fontFamily),
                        displayMedium = Typography.displayMedium.copy(fontFamily = fontFamily),
                        displaySmall = Typography.displaySmall.copy(fontFamily = fontFamily),
                        headlineLarge = Typography.headlineLarge.copy(fontFamily = fontFamily),
                        headlineMedium = Typography.headlineMedium.copy(fontFamily = fontFamily),
                        headlineSmall = Typography.headlineSmall.copy(fontFamily = fontFamily),
                        titleLarge = Typography.titleLarge.copy(fontFamily = fontFamily),
                        titleMedium = Typography.titleMedium.copy(fontFamily = fontFamily),
                        titleSmall = Typography.titleSmall.copy(fontFamily = fontFamily),
                        bodyLarge = Typography.bodyLarge.copy(fontFamily = fontFamily),
                        bodyMedium = Typography.bodyMedium.copy(fontFamily = fontFamily),
                        bodySmall = Typography.bodySmall.copy(fontFamily = fontFamily),
                        labelLarge = Typography.labelLarge.copy(fontFamily = fontFamily),
                        labelMedium = Typography.labelMedium.copy(fontFamily = fontFamily),
                        labelSmall = Typography.labelSmall.copy(fontFamily = fontFamily),
                        displayLargeEmphasized = Typography.displayLargeEmphasized.copy(fontFamily = fontFamily),
                        displayMediumEmphasized = Typography.displayMediumEmphasized.copy(fontFamily = fontFamily),
                        displaySmallEmphasized = Typography.displaySmallEmphasized.copy(fontFamily = fontFamily),
                        headlineLargeEmphasized = Typography.headlineLargeEmphasized.copy(fontFamily = fontFamily),
                        headlineMediumEmphasized = Typography.headlineMediumEmphasized.copy(fontFamily = fontFamily),
                        headlineSmallEmphasized = Typography.headlineSmallEmphasized.copy(fontFamily = fontFamily),
                        titleLargeEmphasized = Typography.titleLargeEmphasized.copy(fontFamily = fontFamily),
                        titleMediumEmphasized = Typography.titleMediumEmphasized.copy(fontFamily = fontFamily),
                        titleSmallEmphasized = Typography.titleSmallEmphasized.copy(fontFamily = fontFamily),
                        bodyLargeEmphasized = Typography.bodyLargeEmphasized.copy(fontFamily = fontFamily),
                        bodyMediumEmphasized = Typography.bodyMediumEmphasized.copy(fontFamily = fontFamily),
                        bodySmallEmphasized = Typography.bodySmallEmphasized.copy(fontFamily = fontFamily),
                        labelLargeEmphasized = Typography.labelLargeEmphasized.copy(fontFamily = fontFamily),
                        labelMediumEmphasized = Typography.labelMediumEmphasized.copy(fontFamily = fontFamily),
                        labelSmallEmphasized = Typography.labelSmallEmphasized.copy(fontFamily = fontFamily)
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = Typography
                )

        init {
            viewModelInitialized()
        }
    }
