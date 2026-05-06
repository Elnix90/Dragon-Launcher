package org.elnix.dragonlauncher.ui.statusbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.areStatusBarsVisible
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.serializables.MainScreenLayer
import org.elnix.dragonlauncher.common.serializables.StatusBarJson
import org.elnix.dragonlauncher.common.serializables.StatusBarSerializable
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.serializables.allStatusBarSerializable
import org.elnix.dragonlauncher.common.messyfolder.Constants
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.STATUS_BAR_TAG
import org.elnix.dragonlauncher.common.utils.DateUtils.isValidDateFormat
import org.elnix.dragonlauncher.common.utils.DateUtils.isValidTimeFormat
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.settings.stores.StatusBarJsonSettingsStore
import org.elnix.dragonlauncher.settings.stores.StatusBarSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.composition.LocalMainScreenLayers
import org.elnix.dragonlauncher.ui.composition.LocalStatusBarElements
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonColumnGroup
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonTooltip
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.helpers.CustomActionSelector
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

enum class DateFormat(val pattern: String, val displayName: String) {
    SHORT("MMM dd", "Short (Dec 25)"),
    MEDIUM("MMM dd, yyyy", "Medium (Dec 25, 2023)"),
    LONG("EEEE, MMMM dd, yyyy", "Long (Monday, December 25, 2023)"),
    ISO("yyyy-MM-dd", "ISO (2023-12-25)"),
    US("MM/dd/yyyy", "US (12/25/2023)"),
    EUROPEAN("dd/MM/yyyy", "European (25/12/2023)"),
    CUSTOM("", "Custom")
}

enum class TimeFormat(val pattern: String, val displayName: String) {
    H12("hh:mm a", "12-hour (02:30 PM)"),
    H24("HH:mm", "24-hour (14:30)"),
    H12_SECONDS("hh:mm:ss a", "12-hour with seconds (02:30:45 PM)"),
    H24_SECONDS("HH:mm:ss", "24-hour with seconds (14:30:45)"),
    H12_SHORT("h:mm a", "12-hour short (2:30 PM)"),
    H24_SHORT("H:mm", "24-hour short (14:30)"),
    CUSTOM("", "Custom")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatusBar(
    launchAction: ((SwipeActionSerializable) -> Unit)?,
) {
    val view = LocalView.current
    val density = LocalDensity.current

    /**
     * Don't show the status bar if not in full screen.
     * For instance, when the system status bar is displayed
     */
    if (WindowInsets.areStatusBarsVisible) return

    val showStatusBar = showStatusBar()


    val statusBarBackground by StatusBarSettingsStore.barBackgroundColor.asState()
    val statusBarText by StatusBarSettingsStore.barTextColor.asState()

    val leftStatusBarPadding by StatusBarSettingsStore.leftPadding.asState()
    val rightStatusBarPadding by StatusBarSettingsStore.rightPadding.asState()
    val topStatusBarPadding by StatusBarSettingsStore.topPadding.asState()
    val bottomStatusBarPadding by StatusBarSettingsStore.bottomPadding.asState()

    val elements = LocalStatusBarElements.current

    // Detect exact cutout bounding rects (geometric notch detection)
    val totalCutoutWidth = remember(view) {
        val insets = ViewCompat.getRootWindowInsets(view)
        val rects = insets?.displayCutout?.boundingRects ?: emptyList()
        // We focus on the top cutout for the status bar
        val topCutout = rects.find { it.top == 0 }
        topCutout?.width() ?: 0
    }

    AnimatedVisibility(showStatusBar) {
        CompositionLocalProvider(
            LocalContentColor provides statusBarText
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(statusBarBackground)
                    .padding(
                        start = leftStatusBarPadding.dp,
                        top = topStatusBarPadding.dp,
                        end = rightStatusBarPadding.dp,
                        bottom = bottomStatusBarPadding.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                elements.forEach { element ->
                    if (element !is StatusBarSerializable.Spacer) {
                        StatusBarItem(element, launchAction)
                    } else {
                        val modifier = Modifier.conditional(
                            condition = element.width == -1,
                            block = { Modifier.weight(1f) },
                            fallback = {
                                // If this is the "Auto" spacer, and we have a cutout, we use cutout width
                                // Otherwise use the defined width
                                width(element.width.dp)
                            }
                        )

                        if (element.width == -2) { // Special ID for Notch Spacer
                            Spacer(Modifier.width(with(density) { totalCutoutWidth.toDp() }))
                        } else {
                            Spacer(modifier)
                        }
                    }
                }
            }
        }
    }
}


private data class StatusBarElement(
    val id: String,
    val item: StatusBarSerializable
)

@Composable
fun EditStatusBar() {
    val ctx = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val scope = rememberCoroutineScope()

    val statusBarBackground by StatusBarSettingsStore.barBackgroundColor.asState()
    val statusBarText by StatusBarSettingsStore.barTextColor.asState()

    val leftStatusBarPadding by StatusBarSettingsStore.leftPadding.asState()
    val rightStatusBarPadding by StatusBarSettingsStore.rightPadding.asState()
    val topStatusBarPadding by StatusBarSettingsStore.topPadding.asState()
    val bottomStatusBarPadding by StatusBarSettingsStore.bottomPadding.asState()

    val elements: SnapshotStateList<StatusBarElement> = remember { mutableStateListOf() }
    var selectedElementId by remember { mutableStateOf<String?>(null) }


    suspend fun load() {
        elements.clear()

        val loadedElements = StatusBarJsonSettingsStore.jsonSetting.get(ctx)

        val elementsJson = StatusBarJson.decodeStatusBarElements(loadedElements)

        elementsJson.forEach { item ->
            elements.add(
                StatusBarElement(
                    id = java.util.UUID.randomUUID().toString(),
                    item = item
                )
            )
        }
    }

    // Load the elements of the status bar on first composition
    LaunchedEffect(Unit) {
        load()
    }

    fun save() {
        val elementsJson = StatusBarJson.encodeStatusBarElements(elements.map { it.item })
        scope.launch {
            StatusBarJsonSettingsStore.jsonSetting.set(ctx, elementsJson)
        }
    }

    fun addElement(element: StatusBarSerializable) {
        elements.add(
            StatusBarElement(
                id = java.util.UUID.randomUUID().toString(),
                item = element
            )
        )
        save()
    }

    fun duplicateElement(element: StatusBarElement) {
        val index = elements.indexOfFirst { it.id == element.id }
        if (index == -1) return

        val copiedItem = when (val item = element.item) {
            is StatusBarSerializable.Time -> item.copy()
            is StatusBarSerializable.Date -> item.copy()
            is StatusBarSerializable.Bandwidth -> item.copy()
            is StatusBarSerializable.Notifications -> item.copy()
            is StatusBarSerializable.Connectivity -> item.copy()
            is StatusBarSerializable.Spacer -> item.copy()
            is StatusBarSerializable.Battery -> item.copy()
            is StatusBarSerializable.NextAlarm -> item.copy()
        }

        elements.add(
            index + 1,
            StatusBarElement(
                id = java.util.UUID.randomUUID().toString(),
                item = copiedItem
            )
        )

        save()
    }

    fun removeElement(element: StatusBarElement) {
        elements -= element
        selectedElementId = null
        save()
    }

    fun updateElement(updated: StatusBarSerializable) {
        val index = elements.indexOfFirst { it.id == selectedElementId }
        if (index == -1) return

        if (elements[index].item != updated) {
            elements[index] = elements[index].copy(item = updated)
            save()
        }
    }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            try {
                if (from.key == to.key) return@rememberReorderableLazyListState

                val fromIdx = elements.indexOfFirst { it.id == from.key }
                val toIdx = elements.indexOfFirst { it.id == to.key }

                if (fromIdx != -1 && toIdx != -1 && fromIdx != toIdx) {
                    val item = elements.removeAt(fromIdx)
                    elements.add(toIdx, item)
                }
            } catch (e: Exception) {
                logE(STATUS_BAR_TAG, e) { "Crash avoided during reorder" }
            }
        }
    )

    CompositionLocalProvider(
        LocalContentColor provides statusBarText
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(MaterialTheme.colorScheme.surface, DragonShape)
                .background(statusBarBackground)
                .padding(
                    start = leftStatusBarPadding.dp,
                    top = topStatusBarPadding.dp,
                    end = rightStatusBarPadding.dp,
                    bottom = bottomStatusBarPadding.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            state = lazyListState
        ) {

            items(elements, key = { it.id }) { statusBarElement ->
                ReorderableItem(
                    state = reorderState,
                    key = statusBarElement.id
                ) { isDragging ->

                    val element = statusBarElement.item
                    val selected = statusBarElement.id == selectedElementId

                    val scale by animateFloatAsState(
                        targetValue = when {
                            isDragging && selected -> 1.2f
                            isDragging -> 1.3f
                            selected -> 0.9f
                            else -> 1f
                        },
                        label = "reorderScale"
                    )
                    val backgroundColor by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        label = "reorderBackground"
                    )


                    val borderColor by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        label = "reorderBorder"
                    )

                    LaunchedEffect(isDragging) {
                        if (isDragging) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }

                    Box(
                        modifier = Modifier
                            .scale(scale)
                            .longPressDraggableHandle(onDragStopped = ::save)
                            .sizeIn(minWidth = 50.dp, minHeight = 50.dp)
                            .border(1.dp, borderColor, DragonShape)
                            .clip(DragonShape)
                            .background(backgroundColor)
                            .clickable {
                                selectedElementId =
                                    if (selectedElementId == statusBarElement.id) null
                                    else statusBarElement.id
                            }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        StatusBarItem(element, previewMode = true)
                    }
                }
            }
        }

        AnimatedVisibility(selectedElementId != null) {
            elements.firstOrNull { it.id == selectedElementId }?.let { element ->
                DragonColumnGroup(
                    Modifier.fillMaxWidth()
                ) {

                    when (val item = element.item) {

                        is StatusBarSerializable.Bandwidth -> {
                            SwitchRow(
                                title = stringResource(R.string.merge_bandwidth),
                                description = "",
                                state = item.merge,
                            ) {
                                updateElement(item.copy(merge = it))
                            }
                        }

                        is StatusBarSerializable.Connectivity -> {
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                SwitchRow(
                                    title = stringResource(R.string.show_airplane_mode),
                                    description = "",
                                    state = item.showAirplaneMode,
                                ) {
                                    updateElement(item.copy(showAirplaneMode = it))
                                }
                                SwitchRow(
                                    title = stringResource(R.string.show_wifi),
                                    description = "",
                                    state = item.showWifi,
                                ) {
                                    updateElement(item.copy(showWifi = it))
                                }
                                SwitchRow(
                                    title = stringResource(R.string.show_bluetooth),
                                    description = "",
                                    state = item.showBluetooth,
                                ) {
                                    updateElement(item.copy(showBluetooth = it))
                                }
                                SwitchRow(
                                    title = stringResource(R.string.show_vpn),
                                    description = "",
                                    state = item.showVpn,
                                ) {
                                    updateElement(item.copy(showVpn = it))
                                }
                                SwitchRow(
                                    title = stringResource(R.string.show_mobile_data),
                                    description = "",
                                    state = item.showMobileData,
                                ) {
                                    updateElement(item.copy(showMobileData = it))
                                }
                                SwitchRow(
                                    title = stringResource(R.string.show_hotspot),
                                    description = "",
                                    state = item.showHotspot,
                                ) {
                                    updateElement(item.copy(showHotspot = it))
                                }
                                SliderWithLabel(
                                    label = stringResource(R.string.connectivity_update_frequency),
                                    value = item.updateFrequency,
                                    valueRange = 1..60,
                                    onReset = { updateElement(item.copy(updateFrequency = 5)) }
                                ) {
                                    updateElement(item.copy(updateFrequency = it))
                                }
                            }
                        }

                        is StatusBarSerializable.Date -> {

                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    DateFormat.entries.filter { it != DateFormat.CUSTOM }.forEach { format ->
                                        DragonButton(
                                            onClick = { updateElement(item.copy(formatter = format.pattern)) }
                                        ) {
                                            Text(format.displayName)
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    label = {
                                        Text(stringResource(R.string.date_format_title))
                                    },
                                    value = item.formatter,
                                    onValueChange = { newValue ->
                                        updateElement(item.copy(formatter = newValue))
                                    },
                                    singleLine = true,
                                    isError = !isValidDateFormat(item.formatter),
                                    supportingText = if (!isValidDateFormat(item.formatter)) {
                                        { Text(stringResource(R.string.invalid_format)) }
                                    } else null,
                                    placeholder = { Text("MMM dd") },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Restore,
                                            contentDescription = stringResource(R.string.reset),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable {
                                                updateElement(item.copy(formatter = "MMM dd"))
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = AppObjectsColors.outlinedTextFieldColors()
                                )

                                CustomActionSelector(
                                    currentAction = item.action,
                                    label = stringResource(R.string.clock_action),
                                    nullText = stringResource(R.string.opens_alarm_clock_app),
                                    onToggle = { updateElement(item.copy(action = null)) }
                                ) { updateElement(item.copy(action = it)) }
                            }
                        }

                        is StatusBarSerializable.Time -> {
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TimeFormat.entries.filter { it != TimeFormat.CUSTOM }.forEach { format ->
                                        DragonButton(
                                            onClick = { updateElement(item.copy(formatter = format.pattern)) }
                                        ) {
                                            Text(format.displayName)
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    label = { Text(stringResource(R.string.time_format_title)) },
                                    value = item.formatter,
                                    onValueChange = { newValue ->
                                        updateElement(item.copy(formatter = newValue))
                                    },
                                    singleLine = true,
                                    isError = !isValidTimeFormat(item.formatter),
                                    supportingText = if (!isValidTimeFormat(item.formatter)) {
                                        { Text(stringResource(R.string.invalid_format)) }
                                    } else null,
                                    placeholder = { Text("HH:mm:ss") },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Restore,
                                            contentDescription = stringResource(R.string.reset),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable {
                                                updateElement(item.copy(formatter = "HH:mm:ss"))
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = AppObjectsColors.outlinedTextFieldColors()
                                )
                            }
                        }

                        is StatusBarSerializable.Notifications -> {
                            SliderWithLabel(
                                label = stringResource(R.string.max_notification_icons),
                                value = item.maxIcons,
                                valueRange = 1..15,
                                onReset = { updateElement(item.copy(maxIcons = 5)) }
                            ) {
                                updateElement(item.copy(maxIcons = it))
                            }
                        }

                        is StatusBarSerializable.Spacer -> {
                            SliderWithLabel(
                                label = stringResource(R.string.width),
                                value = item.width,
                                valueRange = -2..30,
                                onReset = { updateElement(item.copy(width = -1)) }
                            ) {
                                updateElement(item.copy(width = it))
                            }

                            if (item.width == -2) {
                                Text(
                                    text = stringResource(R.string.notch_mode),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        is StatusBarSerializable.Battery -> {
                            SwitchRow(
                                title = stringResource(R.string.show_percentage),
                                description = stringResource(R.string.show_percentage_desc),
                                state = item.showPercentage,
                            ) {
                                updateElement(item.copy(showPercentage = it))
                            }
                        }

                        is StatusBarSerializable.NextAlarm -> {
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(
                                    text = stringResource(R.string.time_format_examples),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                OutlinedTextField(
                                    label = { Text(stringResource(R.string.time_format_title)) },
                                    value = item.formatter,
                                    onValueChange = { newValue ->
                                        updateElement(item.copy(formatter = newValue))
                                    },
                                    singleLine = true,
                                    isError = !isValidTimeFormat(item.formatter),
                                    supportingText = if (!isValidTimeFormat(item.formatter)) {
                                        { Text(stringResource(R.string.invalid_format)) }
                                    } else null,
                                    placeholder = { Text("HH:mm") },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Restore,
                                            contentDescription = stringResource(R.string.reset),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable {
                                                updateElement(item.copy(formatter = "HH:mm"))
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = AppObjectsColors.outlinedTextFieldColors()
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { removeElement(element) },
                            colors = AppObjectsColors.cancelButtonColors()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.remove)
                            )
                            Text(stringResource(R.string.remove))
                        }

                        DragonIconButton(
                            onClick = {
                                duplicateElement(element)
                            },
                            icon = R.drawable.copy,
                            contentDescription = stringResource(R.string.copy)
                        )
                    }
                }
            }
        }



        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            allStatusBarSerializable.forEach { item ->

                val itemName = remember(item) { item::class.simpleName.toString() }

                DragonTooltip(itemName) {
                    Box(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.primary, DragonShape)
                            .clip(DragonShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .sizeIn(minWidth = 50.dp, minHeight = 50.dp)
                            .clickable { addElement(item) }
                            .padding(15.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        StatusBarItem(item, previewMode = true)
                    }
                }
            }
        }
    }

    DragonButton(
        onClick = {
            scope.launch {
                StatusBarJsonSettingsStore.jsonSetting.set(ctx, Constants.Settings.STATUS_BAR_TEMPLATE)
                load()
            }
        }
    ) {
        Text(stringResource(R.string.set_status_bar_template))
    }
}


@Composable
fun StatusBarItem(
    element: StatusBarSerializable,
    launchAction: ((SwipeActionSerializable) -> Unit)? = null,
    previewMode: Boolean = false
) {
    when (element) {
        is StatusBarSerializable.Bandwidth -> {
            StatusBarBandwidth(element)
        }

        is StatusBarSerializable.Connectivity -> {
            StatusBarConnectivity(
                element = element,
                previewMode = previewMode
            )
        }

        is StatusBarSerializable.Date -> {
            StatusBarDate(
                element = element,
                onAction = launchAction,
            )
        }

        is StatusBarSerializable.Time -> {
            StatusBarTime(
                element = element,
                onAction = launchAction,
            )
        }

        is StatusBarSerializable.Notifications -> {
            StatusBarNotifications(element)
        }

        is StatusBarSerializable.Spacer -> {
            Text(stringResource(R.string.spacer))
        }

        is StatusBarSerializable.Battery -> {
            StatusBarBattery(element)
        }

        is StatusBarSerializable.NextAlarm -> {
            StatusBarNextAlarm(element, forceShowIcon = previewMode)
        }
    }
}


@Composable
fun showStatusBar(): Boolean {
    val mainScreensLayers = LocalMainScreenLayers.current

    return ((mainScreensLayers.find { it is MainScreenLayer.StatusBar }
        ?: error("No status bar provided in the list")) as MainScreenLayer.StatusBar).enabled
}


@Composable
fun showChargingAnimation(): Boolean {
    val mainScreensLayers = LocalMainScreenLayers.current

    return (
            (mainScreensLayers.find { it is MainScreenLayer.ChargingAnimation } ?: error("No charging animation provided in the list"))
                    as MainScreenLayer.ChargingAnimation
            ).enabled
}
