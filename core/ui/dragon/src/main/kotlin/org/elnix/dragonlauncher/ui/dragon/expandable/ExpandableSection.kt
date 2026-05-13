package org.elnix.dragonlauncher.ui.dragon.expandable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.ui.base.animation.bouncySpec
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.base.modifiers.settingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.text.AutoResizeableText


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableSection(
    state: ExpandableSectionState,
    content: @Composable ColumnScope.() -> Unit
) {
    val enabled = state.enabled()
    val expanded = state.isExpanded() && enabled

    val rotationDegrees = animateFloatAsState(
        targetValue = if (expanded) 0f else -90f,
        animationSpec = bouncySpec()
    )

    val backgroundColor by animateColorAsState(
        if (expanded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
    )

    val contentColor = contentColorFor(backgroundColor)

    Column(
        modifier = Modifier.settingsGroup(
            clickModifier = Modifier.conditional(!expanded && enabled) {
                clickable {
                    state.toggle()
                }
            },
            backgroundColor = backgroundColor,
            border = false,
            enabled = enabled
        )
    ) {
        Row(
            modifier = Modifier
                .settingsGroup(
                    clickModifier = Modifier.conditional(expanded) {
                        clickable {
                            state.toggle()
                        }
                    },
                    backgroundColor = Color.Transparent,
                    border = false,
                    enabled = enabled
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AutoResizeableText(
                state.title,
                modifier = Modifier.weight(1f),
                color = contentColor
            )

            Icon(
                painter = painterResource(R.drawable.arrow_drop_down),
                contentDescription = stringResource(R.string.expanded_chevron_indicator),
                tint = contentColor,
                modifier = Modifier
                    .rotate(rotationDegrees.value)
            )
        }

        if (state.mode == ExpandableSectionMode.Expandable) {
            AnimatedVisibility(
                visible = expanded
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    content()
                }
            }
        }
    }
    if (state.mode is ExpandableSectionMode.ModalSheet && expanded) {
        DragonModalBottomSheet(
            onDismissRequest = { state.toggle() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = state.mode.skipPartiallyExpanded)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                content()
            }
        }
    }
}
