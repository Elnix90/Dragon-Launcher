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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults.elevatedCardElevation
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.elnix.dragonlauncher.base.ColorUtils.alphaMultiplier
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.HAPTIC_TAG
import org.elnix.dragonlauncher.common.messyfolder.showToast
import org.elnix.dragonlauncher.common.serializables.CustomHapticFeedbackSerializable
import org.elnix.dragonlauncher.common.serializables.hapticFeedbackSerializablePresets
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.pasteClipboard
import org.elnix.dragonlauncher.common.utils.HapticUtils.performCustomHaptic
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.dragon.text.TextDivider
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private data class HapticEntry(
    val id: Long, // stable key for reorderable list
    val isVibration: Boolean,
    val durationMs: Int
)

@Composable
fun HapticFeedbackEditor(
    initial: CustomHapticFeedbackSerializable? = null,
    onDismiss: () -> Unit,
    onPicked: (CustomHapticFeedbackSerializable?) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val initialEntries = remember(initial) {
        initial?.haptics
            ?.mapIndexed { i, (isVibration, duration) ->
                HapticEntry(
                    id = System.nanoTime() + i,
                    isVibration = isVibration,
                    durationMs = duration
                )
            }
            ?: emptyList()
    }

    val entries = remember { mutableStateListOf<HapticEntry>().also { it.addAll(initialEntries) } }

    val lazyListState = rememberLazyListState()

    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            entries.add(to.index, entries.removeAt(from.index))
        }
    )


    fun currentEditingSnapshot(): CustomHapticFeedbackSerializable? {
        val snapshot = if (entries.isEmpty()) null
        else CustomHapticFeedbackSerializable(
            haptics = entries.map { Pair(it.isVibration, it.durationMs) }
        )
        logD(HAPTIC_TAG) { "Got snapshot: $snapshot" }
        return snapshot
    }

    fun playTest() {
        scope.launch {
            performCustomHaptic(ctx, currentEditingSnapshot())
        }
    }

    fun selectPreset(customFeedback: CustomHapticFeedbackSerializable) {
        entries.clear()
        customFeedback.haptics.mapIndexed { i, (isVibration, duration) ->
            entries.add(
                HapticEntry(
                    id = System.nanoTime() + i,
                    isVibration = isVibration,
                    durationMs = duration
                )
            )
        }
        scope.launch {
            delay(50)
            playTest()
        }
    }


    fun copyToClipboard() {
        val encoded = Json.encodeToString(currentEditingSnapshot())

        ctx.copyToClipboard(encoded)
    }

    fun importFromClipboard() {
        val clipboardContent = ctx.pasteClipboard() ?: ""

        try {
            val decoded = Json.decodeFromString<CustomHapticFeedbackSerializable>(clipboardContent)

            selectPreset(decoded)
            ctx.showToast("✅ Successfully imported!")

        } catch (e: Exception) {
            logE(HAPTIC_TAG, e) { "Failed to decode '$clipboardContent' from clipboard" }
            ctx.showToast("❌ Failed to decode '$clipboardContent' from clipboard: $e")
        }
    }

    CustomAlertDialog(
        modifier = Modifier
            .padding(24.dp)
            .heightIn(max = 700.dp),
        onDismissRequest = onDismiss,
        imePadding = false,
        scroll = false,
        alignment = Alignment.Center,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.haptic_feedback_editor),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge
                    )

                    DragonIconButton(
                        onClick = ::importFromClipboard,
                        icon = R.drawable.paste,
                        contentDescription = stringResource(R.string.paste)
                    )

                    DragonIconButton(
                        onClick = ::copyToClipboard,
                        icon = R.drawable.copy,
                        contentDescription = stringResource(R.string.copy)
                    )
                }
                Spacer(Modifier.height(5.dp))

                TextDivider(stringResource(R.string.presets))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(DragonShape)
                        .horizontalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    hapticFeedbackSerializablePresets.forEach { (name, preset) ->
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
        },
        confirmButton = {
            ValidateCancelButtons(
                onCancel = onDismiss
            ) {
                onPicked(
                    currentEditingSnapshot()
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {


                TextDivider(stringResource(R.string.steps))


                // ── Add buttons ──────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                id = System.nanoTime(),
                                isVibration = true,
                                durationMs = 50
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
                                id = System.nanoTime(),
                                isVibration = false,
                                durationMs = 100
                            )
                        )
                    }

                    RotatingPlayIcon(enabled = entries.isNotEmpty(), onClick = ::playTest)
                }

                // ── Reorderable list ─────────────────────────────────────────
                if (entries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(DragonShape)
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
                        state = lazyListState
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
                                        .fillMaxWidth()
                                        .scale(scale)
                                        .longPressDraggableHandle(),
                                    elevation = elevatedCardElevation(elevation),
                                    colors = AppObjectsColors.cardColors(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // ── Row: checkbox · label · delete · drag ──
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

                                            Spacer(Modifier.width(8.dp))

                                            Text(
                                                text = stringResource(if (entry.isVibration) R.string.vibration else R.string.delay),
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier.weight(1f)
                                            )

                                            DragonIconButton(
                                                onClick = {
                                                    entries.add(index + 1, entries[index].copy(id = System.nanoTime()))
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

                                        // ── Duration slider ──────────────────
                                        SliderWithLabel(
                                            label = stringResource(R.string.duration_ms),
                                            value = entry.durationMs,
                                            valueRange = 0..1000,
                                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
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
    )
}

@Composable
private fun RotatingPlayIcon(
    enabled: Boolean,
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
        enabled = { enabled },
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
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}


@Composable
fun HapticFeedBackEditorButtonWithPlayTest(
    customHapticFeedbackSerializable: CustomHapticFeedbackSerializable,
    titleExt: String = "",
    onClick: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            Spacer(Modifier.width(5.dp))
            Text("${stringResource(R.string.haptic_feedback_editor)}$titleExt")
        }

        RotatingPlayIcon(
            enabled = true
        ) {
            scope.launch {
                performCustomHaptic(ctx, customHapticFeedbackSerializable)
            }
        }
    }
}