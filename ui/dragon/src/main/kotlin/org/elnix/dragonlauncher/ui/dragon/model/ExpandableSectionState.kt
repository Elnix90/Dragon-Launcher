package org.elnix.dragonlauncher.ui.dragon.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@ConsistentCopyVisibility
@Stable
data class ExpandableSectionState
    internal constructor(
        val isExpanded: () -> Boolean,
        val enabled: Boolean,
        val title: Int,
        val description: Int?,
        val icon: Int?,
        val customLeadingContent: (@Composable () -> Unit)?,
        val skipPartiallyExpanded: Boolean,
        val toggle: () -> Unit
    ) {
        init {
            require((icon != null) xor (customLeadingContent != null)) {
                "Must either provide an icon of a custom leading content"
            }
        }
    }
