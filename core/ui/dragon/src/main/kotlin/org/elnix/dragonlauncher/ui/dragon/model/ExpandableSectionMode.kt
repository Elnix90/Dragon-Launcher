package org.elnix.dragonlauncher.ui.dragon.model

public sealed class ExpandableSectionMode {
    public data class ModalSheet(
        val skipPartiallyExpanded: Boolean = false
    ) : ExpandableSectionMode()

    public data object Expandable : ExpandableSectionMode()
}


public data class ExpandableSectionState(
    val isExpanded: () -> Boolean,
    val enabled: () -> Boolean,
    val title: String,
    val description: String?,
    val mode: ExpandableSectionMode,
    val toggle: () -> Unit,
)