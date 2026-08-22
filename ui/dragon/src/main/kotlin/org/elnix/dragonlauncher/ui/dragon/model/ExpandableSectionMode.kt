package org.elnix.dragonlauncher.ui.dragon.model

import androidx.compose.runtime.Stable

sealed class ExpandableSectionMode {
    data class ModalSheet(
        val skipPartiallyExpanded: Boolean = false
    ) : ExpandableSectionMode()

    data object Expandable : ExpandableSectionMode()
}

@Stable
data class ExpandableSectionState(
    val isExpanded: () -> Boolean,
    val enabled: () -> Boolean,
    val title: String,
    val description: String?,
    val mode: ExpandableSectionMode,
    val toggle: () -> Unit,
)