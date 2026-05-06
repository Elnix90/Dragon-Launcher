package org.elnix.dragonlauncher.ui.dragon.generic

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.enumsui.ToggleButtonOption
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.dragon.components.DragonTooltip
import org.elnix.dragonlauncher.ui.dragon.internals.connectedBottomButtonShapes
import org.elnix.dragonlauncher.ui.dragon.internals.connectedTopButtonShapes

/**
 * A horizontally connected multi-select toggle button group built on Material3 Expressive's
 * [ButtonGroupDefaults] connected shape system.
 *
 * Each button independently toggles without affecting the others, making this suitable
 * for multi-select filter rows, editor toolbars, or any set of orthogonal on/off options.
 *
 * Connected shapes are applied automatically based on position:
 * - First entry → [ButtonGroupDefaults.connectedLeadingButtonShapes]
 * - Last entry  → [ButtonGroupDefaults.connectedTrailingButtonShapes]
 * - Middle entries → [ButtonGroupDefaults.connectedMiddleButtonShapes]
 *
 * @param T Any type implementing [ToggleButtonOption], typically an enum.
 * @param entries The ordered list of options to display as toggle buttons.
 * @param isChecked Predicate returning the current checked state for a given entry.
 * @param onCheck Called when the user taps a button, both on check and uncheck.
 * @param showLabel Whether to show the text label alongside the icon. Defaults to `true`.
 * @param hapticFeedback Whether to emit a [HapticFeedbackType.KeyboardTap] on every tap,
 *   regardless of the resulting checked state. Defaults to `true`.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T : ToggleButtonOption> MultiSelectConnectedButtonColumn(
    entries: List<T>,

    // Optional parameters
    showLabel: Boolean = true,
    hapticFeedback: Boolean = true,

    isEnabled: (T) -> Boolean = { true },
    isChecked: (T) -> Boolean,
    onCheck: (T) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column(
        Modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        entries.forEachIndexed { index, entry ->

            // No idea why, but using a not here feels more natural for the displayed entries
            val checked = !isChecked(entry)

            DragonTooltip(entry.resId ?: -1) {
                ToggleButton(
                    checked = checked,
                    enabled = isEnabled(entry),
                    onCheckedChange = {
                        onCheck(entry)
                        if (hapticFeedback) {
                            haptic.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                        }
                    },
                    colors = AppObjectsColors.toggleButtonColors(),
                    // Custom shapes
                    shapes = when (index) {
                        0 -> connectedTopButtonShapes()
                        entries.lastIndex -> connectedBottomButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    }
                ) {
                    entry.iconEnabled?.let { iconEnabled ->
                        Crossfade(!checked) { notChecked ->
                            Icon(
                                painter = painterResource(entry.iconDisabled.takeIf { notChecked && it != null } ?: iconEnabled),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}