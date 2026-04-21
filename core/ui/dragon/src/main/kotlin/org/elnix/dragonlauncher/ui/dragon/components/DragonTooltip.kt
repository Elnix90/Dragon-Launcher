package org.elnix.dragonlauncher.ui.dragon.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape


@Composable
fun DragonTooltipInternal(
    text: String,
    enabled: Boolean,
    content: @Composable (() -> Unit)
) {
    val tooltipState = rememberTooltipState(isPersistent = true)
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(tooltipState.isVisible) {
        if (tooltipState.isVisible) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Above
        ),
        tooltip = {
            PlainTooltip(
                shape = DragonShape,
                contentColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 5.dp,
                shadowElevation = 3.dp
            ) {
                Text(text)
            }
        },
        enableUserInput = enabled,
        state = tooltipState,
        content = content
    )
}

@Composable
fun DragonTooltip(
    resId: Int,
    enabled: Boolean = true,
    content: @Composable (() -> Unit)
) {
    val text = resId.takeIf { it != -1 }?.let { stringResource(resId) } ?: "Unknown ressource"

    DragonTooltipInternal(
        text = text,
        enabled = enabled,
        content = content
    )
}


@Composable
fun DragonTooltip(
    description: String,
    enabled: Boolean = true,
    content: @Composable (() -> Unit)
) {
    DragonTooltipInternal(
        text = description,
        enabled = enabled,
        content = content
    )
}
