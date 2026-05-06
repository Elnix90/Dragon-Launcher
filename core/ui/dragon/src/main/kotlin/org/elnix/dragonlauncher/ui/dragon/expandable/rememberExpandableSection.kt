package org.elnix.dragonlauncher.ui.dragon.expandable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


sealed class ExpandableSectionMode {
    data class ModalSheet(
        val skipPartiallyExpanded: Boolean = false
    ) : ExpandableSectionMode()

    data object Expandable : ExpandableSectionMode()
}


data class ExpandableSectionState(
    val isExpanded: () -> Boolean,
    val enabled: () -> Boolean,
    val title: String,
    val mode: ExpandableSectionMode,
    val toggle: () -> Unit,
)

@Composable
fun rememberExpandableSection(
    title: String,
    mode: ExpandableSectionMode = ExpandableSectionMode.ModalSheet(),
    enabled: () -> Boolean = { true }
): ExpandableSectionState {
    var isExpanded by remember { mutableStateOf(false) }

    return remember(title, enabled) {
        ExpandableSectionState(
            isExpanded = { isExpanded },
            enabled = enabled,
            title = title,
            mode = mode,
            toggle = { isExpanded = !isExpanded }
        )
    }
}
