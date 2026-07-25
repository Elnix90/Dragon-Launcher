package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.undoredo.UndoRedoManager
import org.elnix.dragonlauncher.enumsui.toggle.UndRedoEditTools
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.ui.base.withHapticParam
import org.elnix.dragonlauncher.ui.dragon.components.DragonTooltip
import org.elnix.dragonlauncher.ui.dragon.generic.connectedLeadingButtonShapes
import org.elnix.dragonlauncher.ui.dragon.generic.connectedMiddleButtonShapes
import org.elnix.dragonlauncher.ui.dragon.generic.connectedTrailingButtonShapes

@Composable
fun UndoRedoBlock(undoRedo: UndoRedoManager) {

    val undoButtonEnabled by undoRedo.canUndo.collectAsState()
    val redoButtonEnabled by undoRedo.canRedo.collectAsState()

    val interactionSources = List(4) { rememberInteractionSource() }

    val debugInfos by DebugSettingsStore.settingsDebugInfo.asState()

    ButtonGroup(
        overflowIndicator = { ButtonGroupDefaults.OverflowIndicator(it) },
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        UndRedoEditTools.entries.forEachIndexed { idx, entry ->

            val checked = when (entry) {
                UndRedoEditTools.UndoAll -> undoButtonEnabled
                UndRedoEditTools.Undo -> undoButtonEnabled
                UndRedoEditTools.Redo -> redoButtonEnabled
                UndRedoEditTools.RedoAll -> redoButtonEnabled
            }

            customItem(
                buttonGroupContent = {
                    IconToggleButton(
                        checked = checked,
                        onCheckedChange = withHapticParam {
                            when (entry) {
                                UndRedoEditTools.UndoAll -> undoRedo.undoAll()
                                UndRedoEditTools.Undo -> undoRedo.undo()
                                UndRedoEditTools.Redo -> undoRedo.redo()
                                UndRedoEditTools.RedoAll -> undoRedo.redoAll()
                            }
                        },
                        interactionSource = interactionSources[idx],
                        modifier = Modifier
                            .size(IconButtonDefaults.smallContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide))
                            .animateWidth(interactionSources[idx]),
                        enabled = when (entry) {
                            UndRedoEditTools.UndoAll -> undoButtonEnabled
                            UndRedoEditTools.Undo -> undoButtonEnabled
                            UndRedoEditTools.Redo -> redoButtonEnabled
                            UndRedoEditTools.RedoAll -> redoButtonEnabled
                        },
                        colors = AppObjectsColors.iconToggleButtonColors(),
                        shapes = when (idx) {
                            0 -> connectedLeadingButtonShapes()
                            3 -> connectedTrailingButtonShapes()
                            else -> connectedMiddleButtonShapes()
                        }
                    ) {
                        entry.iconEnabled.let { iconEnabled ->
                            DragonTooltip(entry.resId ?: -1) {
                                Crossfade(!checked) { notChecked ->
                                    BadgedBox(
                                        badge = {

                                            if (debugInfos) {
                                                val undoSize by undoRedo.undoSize.collectAsState()
                                                val redoSize by undoRedo.redoSize.collectAsState()

                                                val text = when (entry) {
                                                    UndRedoEditTools.UndoAll -> undoSize.takeIf { it > 0 }
                                                    UndRedoEditTools.Undo -> null
                                                    UndRedoEditTools.Redo -> null
                                                    UndRedoEditTools.RedoAll -> redoSize.takeIf { it > 0 }
                                                }

                                                text?.let {
                                                    Text(
                                                        text = it.toString(),
                                                        fontSize = 6.sp,
                                                        modifier = Modifier.align(Alignment.BottomEnd)
                                                    )
                                                }
                                            }
                                        },
                                    ) {
                                        Icon(
                                            painter = painterResource(entry.iconDisabled.takeIf { notChecked && it != null } ?: iconEnabled),
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                menuContent = { }
            )
        }
    }
}