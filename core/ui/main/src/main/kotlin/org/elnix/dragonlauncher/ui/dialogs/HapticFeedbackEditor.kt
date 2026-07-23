package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults.elevatedCardElevation
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.logging.HAPTIC_TAG
import io.github.elnix90.logging.logE
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.json
import org.elnix.dragonlauncher.base.model.serializables.CustomHapticFeedback
import org.elnix.dragonlauncher.base.model.serializables.HapticEntry
import org.elnix.dragonlauncher.base.model.serializables.HapticEntry.Companion.defaultHapticDuration
import org.elnix.dragonlauncher.base.model.serializables.HapticEntry.Companion.defaultVibrationDuration
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.pasteClipboard
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.alphaMultiplier
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonDropDownMenu
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun HapticFeedbackEditor(
    initial: CustomHapticFeedback?,
    default: CustomHapticFeedback?,
    onDismiss: (CustomHapticFeedback?) -> Unit,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val entries = remember(initial) {
        mutableStateListOf<HapticEntry>().apply { addAll(initial?.haptics ?: default?.haptics ?: emptyList()) }
    }

    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            entries.add(to.index, entries.removeAt(from.index))
        }
    )

    fun currentEditingSnapshot(): CustomHapticFeedback? {
        val snapshot = if (entries.isEmpty()) null
        else CustomHapticFeedback(entries.toList())
        return snapshot
    }

    fun playTest() {
        scope.launch {
            currentEditingSnapshot()?.perform(ctx)
        }
    }

    fun selectPreset(customFeedback: CustomHapticFeedback) {
        scope.launch {
            entries.clear()
            customFeedback.haptics.forEach { (isVibration, duration) ->
                entries.add(
                    HapticEntry(
                        isVibration = isVibration,
                        durationMs = duration
                    )
                )
            }
            playTest()
        }
    }

    fun copyToClipboard() {
        val current = currentEditingSnapshot() ?: return
        val encoded = json.encodeToString(current)
        ctx.copyToClipboard(encoded)
    }

    fun importFromClipboard() {
        val clipboardContent = ctx.pasteClipboard()
        try {
            clipboardContent ?: throw IllegalStateException("<empty>")
            val decoded = json.decodeFromString<CustomHapticFeedback>(clipboardContent)

            selectPreset(decoded)
            ctx.showToast("✅ Successfully imported!")

        } catch (e: IllegalStateException) {
            logE(HAPTIC_TAG, e) { "Clipboard if empty" }
            ctx.showToast("❌  Clipboard if empty")
        } catch (e: Exception) {
            logE(HAPTIC_TAG, e) { "Failed to decode '$clipboardContent' from clipboard" }
            ctx.showToast("❌ Failed to decode '$clipboardContent' from clipboard: $e")
        }
    }

    DragonModalBottomSheet(
        onDismissRequest = { onDismiss(currentEditingSnapshot()) }
    ) {
        DialogTitle(
            text = stringResource(R.string.haptic_feedback_editor),
            trailingIcon = {
                var showPopup by remember { mutableStateOf(false) }
                Box {
                    DragonIconButton(
                        icon = R.drawable.more_vert,
                        contentDescription = R.string.more
                    ) { showPopup = true }

                    DragonDropDownMenu(showPopup, { showPopup = false }) {
                        DropdownMenuGroup(
                            shapes = MenuDefaults.groupShapes()
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.copy_to_clipboard))
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.copy),
                                        contentDescription = null
                                    )
                                },
                                onClick = ::copyToClipboard
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.import_from_clipboard))
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.paste),
                                        contentDescription = null
                                    )
                                },
                                onClick = ::importFromClipboard
                            )
                        }
                    }
                }
            },
            resetEnabled = entries.toList() != default?.haptics?.toList(),
            onReset = {
                entries.clear()
                default?.haptics?.let { entries.addAll(it) }
            }
        )

        Spacer(5.dp)


        DragonSettingsGroup(R.string.presets) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                CustomHapticFeedback.allPresets.forEach { (name, preset) ->
                    DragonButton(
                        onClick = { selectPreset(preset) }
                    ) {
                        Text(
                            text = stringResource(name),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }


        DragonSettingsGroup(R.string.steps) {
            Row(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AddStepButton(
                    label = stringResource(R.string.vibration),
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.haptic),
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    entries.add(
                        HapticEntry(
                            isVibration = true,
                            durationMs = defaultHapticDuration
                        )
                    )
                }

                AddStepButton(
                    label = stringResource(R.string.delay),
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.timer),
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    entries.add(
                        HapticEntry(
                            isVibration = false,
                            durationMs = defaultVibrationDuration
                        )
                    )
                }

                RotatingPlayIcon(enabled = entries.isNotEmpty(), onClick = ::playTest)
            }

            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surface.alphaMultiplier(0.7f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_steps_yet_add_a_vibration_or_delay),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    state = lazyListState,
                    modifier = Modifier.heightIn(max = 600.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        val index = entries.indexOf(entry)

                        ReorderableItem(
                            state = reorderState,
                            key = entry.id
                        ) { isDragging ->

                            val scale by animateFloatAsState(
                                if (isDragging) 1.03f else 1f
                            )
                            val elevation by animateDpAsState(
                                if (isDragging) 16.dp else 0.dp
                            )

                            ElevatedCard(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth()
                                    .scale(scale)
                                    .longPressDraggableHandle(),
                                elevation = elevatedCardElevation(elevation),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = entry.isVibration,
                                            onCheckedChange = { checked ->
                                                entries[index] = entry.copy(isVibration = checked)
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = MaterialTheme.colorScheme.primary
                                            )
                                        )

                                        Icon(
                                            painter = painterResource(
                                                if (entry.isVibration)
                                                    R.drawable.haptic
                                                else
                                                    R.drawable.timer
                                            ),
                                            contentDescription = null,
                                            tint = if (entry.isVibration)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.secondary
                                        )

                                        Spacer(8.dp)

                                        Text(
                                            text = stringResource(if (entry.isVibration) R.string.vibration else R.string.delay),
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.weight(1f)
                                        )

                                        DragonIconButton(
                                            onClick = {
                                                entries.add(index + 1, entries[index].copy())
                                            },
                                            colors = AppObjectsColors.iconButtonColors(),
                                            icon = R.drawable.copy,
                                            contentDescription = stringResource(R.string.copy)
                                        )

                                        DragonIconButton(
                                            colors = AppObjectsColors.cancelIconButtonColors(),
                                            icon = R.drawable.delete_forever,
                                            contentDescription = stringResource(R.string.remove)
                                        ) { entries.removeAt(index) }

                                        Icon(
                                            painter = painterResource(R.drawable.drag_handle),
                                            contentDescription = stringResource(R.string.drag_handle),
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.draggableHandle()
                                        )
                                    }

                                    SliderWithLabel(
                                        label = stringResource(R.string.duration_ms),
                                        value = entry.durationMs,
                                        valueRange = 0..1000,
                                        resetEnabled = entry.resetEnabled,
                                        onReset = {
                                            entries[index] = entry.copy(
                                                durationMs = if (entry.isVibration) 50 else 100
                                            )
                                        }
                                    ) { newValue ->
                                        entries[index] = entry.copy(durationMs = newValue)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RotatingPlayIcon(
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val playIconRotation = remember {
        Animatable(
            initialValue = 0f
        )
    }

    DragonIconButton(
        modifier = Modifier.rotate(playIconRotation.value),
        enabled = enabled,
        icon = R.drawable.play_arrow,
        contentDescription = stringResource(R.string.play),
    ) {
        scope.launch {
            playIconRotation.animateTo(
                targetValue = playIconRotation.value + 360f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
            playIconRotation.snapTo(0f)
        }
        onClick()
    }
}

@Composable
private fun AddStepButton(
    label: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    DragonButton(
        onClick = onClick,
        modifier = modifier
    ) {
        icon()
        Spacer(5.dp)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}


@Composable
public fun HapticFeedBackEditorButtonWithPlayTest(
    customHapticFeedback: CustomHapticFeedback,
    titleExt: String = "",
    onClick: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        DragonButton(
            onClick = onClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = painterResource(R.drawable.haptic),
                contentDescription = stringResource(R.string.haptic_feedback_editor)
            )
            Spacer(5.dp)
            Text("${stringResource(R.string.haptic_feedback_editor)}$titleExt")
        }

        RotatingPlayIcon {
            scope.launch {
                customHapticFeedback.perform(ctx)
            }
        }
    }
}