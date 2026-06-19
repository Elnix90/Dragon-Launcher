package org.elnix.dragonlauncher.ui.dragon.generic

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonGroupDefaults.connectedButtonCheckedShape
import androidx.compose.material3.ButtonGroupDefaults.connectedLeadingButtonPressShape
import androidx.compose.material3.ButtonGroupDefaults.connectedLeadingButtonShape
import androidx.compose.material3.ButtonGroupDefaults.connectedMiddleButtonPressShape
import androidx.compose.material3.ButtonGroupDefaults.connectedTrailingButtonPressShape
import androidx.compose.material3.ButtonGroupDefaults.connectedTrailingButtonShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.IconToggleButtonShapes
import androidx.compose.material3.ShapeDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import org.elnix.dragonlauncher.enumsui.ToggleButtonOption
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.ui.base.withHapticParam
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
 * @param checked Predicate returning the current checked state for a given entry.
 * @param onCheck Called when the user taps a button, both on check and uncheck.
 *   regardless of the resulting checked state. Defaults to `true`.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T : ToggleButtonOption> MultiSelectConnectedButtonRow(
    entries: List<T>,
    enabled: (T) -> Boolean = { true },
    checked: (T) -> Boolean = { true },
    onCheck: (T) -> Unit
) {
    val interactionSources = List(entries.size) { rememberInteractionSource() }

    ButtonGroup(
        overflowIndicator = { ButtonGroupDefaults.OverflowIndicator(it) },
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        entries.forEachIndexed { idx, entry ->

            val checked = checked(entry)

            customItem(
                buttonGroupContent = {
                    IconToggleButton(
                        checked = checked,
                        onCheckedChange = withHapticParam { onCheck(entry) },
                        interactionSource = interactionSources[idx],
                        modifier = Modifier
                            .size(IconButtonDefaults.smallContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide))
                            .animateWidth(interactionSources[idx]),
                        enabled = enabled(entry),
                        colors = AppObjectsColors.iconToggleButtonColors(),
                        shapes = when (idx) {
                            0 -> connectedLeadingButtonShapes()
                            entries.lastIndex -> connectedTrailingButtonShapes()
                            else -> connectedMiddleButtonShapes()
                        }
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
                },
                menuContent = {  }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun connectedLeadingButtonShapes(
    shape: Shape = connectedLeadingButtonShape,
    pressedShape: Shape = connectedLeadingButtonPressShape,
    checkedShape: Shape = connectedButtonCheckedShape,
): IconToggleButtonShapes =
    IconToggleButtonShapes(shape = shape, pressedShape = pressedShape, checkedShape = checkedShape)


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun connectedMiddleButtonShapes(
    shape: Shape = ShapeDefaults.Small,
    pressedShape: Shape = connectedMiddleButtonPressShape,
    checkedShape: Shape = connectedButtonCheckedShape,
): IconToggleButtonShapes =
    IconToggleButtonShapes(shape = shape, pressedShape = pressedShape, checkedShape = checkedShape)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun connectedTrailingButtonShapes(
    shape: Shape = connectedTrailingButtonShape,
    pressedShape: Shape = connectedTrailingButtonPressShape,
    checkedShape: Shape = connectedButtonCheckedShape,
): IconToggleButtonShapes =
    IconToggleButtonShapes(shape = shape, pressedShape = pressedShape, checkedShape = checkedShape)
