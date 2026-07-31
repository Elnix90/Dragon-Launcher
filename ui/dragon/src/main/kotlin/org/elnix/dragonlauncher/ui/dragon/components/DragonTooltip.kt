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
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DragonTooltipInternal(
    text: String,
    enabled: Boolean,
    modifier: Modifier,
    content: @Composable ((Modifier) -> Unit)
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
                shape = MaterialTheme.shapes.large,
                contentColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 5.dp,
                shadowElevation = 3.dp
            ) {
                Text(text)
            }
        },
        enableUserInput = enabled,
        state = tooltipState
    ) {
        content(modifier)
    }
}

@Composable
fun DragonTooltip(
    resId: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable ((Modifier) -> Unit)
) {
    val text = resId.takeIf { it != -1 }?.let { stringResource(resId) } ?: "Unknown ressource"

    DragonTooltipInternal(
        text = text,
        modifier = modifier,
        enabled = enabled,
        content = content
    )
}


@Composable
fun DragonTooltip(
    description: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable ((Modifier) -> Unit)
) {
    DragonTooltipInternal(
        text = description,
        modifier = modifier,
        enabled = enabled,
        content = content
    )
}
