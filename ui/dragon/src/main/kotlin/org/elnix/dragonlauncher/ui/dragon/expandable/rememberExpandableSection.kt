package org.elnix.dragonlauncher.ui.dragon.expandable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.elnix.dragonlauncher.ui.dragon.model.ExpandableSectionMode
import org.elnix.dragonlauncher.ui.dragon.model.ExpandableSectionState


@Composable
fun rememberExpandableSection(
    title: Int,
    description: Int?,
    icon: Int?,
    mode: ExpandableSectionMode = ExpandableSectionMode.ModalSheet(),
    enabled: Boolean = true
): ExpandableSectionState {
    var isExpanded by remember { mutableStateOf(false) }

    return remember(title, description, enabled) {
        ExpandableSectionState(
            isExpanded = { isExpanded },
            enabled = enabled,
            title = title,
            description = description,
            icon = icon,
            mode = mode,
            toggle = { isExpanded = !isExpanded }
        )
    }
}
