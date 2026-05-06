package org.elnix.dragonlauncher.ui.dragon.generic

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import org.elnix.dragonlauncher.enumsui.ToggleButtonOption
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.UiConstants
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.ui.base.withHaptic
import org.elnix.dragonlauncher.ui.dragon.components.DragonTooltip

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
 *   regardless of the resulting checked state. Defaults to `true`.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T : ToggleButtonOption> MultiSelectConnectedButtonRow(
    entries: List<T>,
    isEnabled: (T) -> Boolean = { true },
    isChecked: (T) -> Boolean = { true },
    onCheck: (T) -> Unit
) {
    val interactionSources = List(entries.size) { rememberInteractionSource() }

    @Suppress("DEPRECATION")
    ButtonGroup(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
        entries.forEachIndexed { idx, entry ->

            val checked = isChecked(entry)

            IconButton(
                onClick = withHaptic { onCheck(entry) },
                interactionSource = interactionSources[idx],
                modifier = Modifier
                    .size(IconButtonDefaults.smallContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide))
                    .animateWidth(interactionSources[idx]),
                enabled = isEnabled(entry),
                shapes = UiConstants.dragonIconButtonShapes(),
                colors = AppObjectsColors.iconButtonColors()
//                shapes = when (idx) {
//                    0 -> ButtonGroupDefaults.
//                    entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
//                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
//                }
            ) {

                entry.iconEnabled?.let { iconEnabled ->
                    DragonTooltip(entry.resId ?: -1) {
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