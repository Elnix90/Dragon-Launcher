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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import io.github.elnix90.logging.STATUS_BAR_TAG
import io.github.elnix90.logging.logE
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.models.DateFormat
import org.elnix.dragonlauncher.base.model.models.TimeFormat
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer
import org.elnix.dragonlauncher.base.model.serializables.StatusBar
import org.elnix.dragonlauncher.base.model.serializables.StatusBarJson
import org.elnix.dragonlauncher.base.model.serializables.allStatusBars
import org.elnix.dragonlauncher.common.utils.DateUtils
import org.elnix.dragonlauncher.common.utils.DateUtils.isValidDateFormat
import org.elnix.dragonlauncher.common.utils.DateUtils.isValidTimeFormat
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.array.StatusBarJsonSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.StatusBarSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.components.Spacer
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


@OptIn(ExperimentalLayoutApi::class)
@Composable
public fun StatusBar(
    launchAction: ((Action) -> Unit)?,
) {
    val view = LocalView.current
    val density = LocalDensity.current

    /**
     * Don't show the status bar if not in full screen.
     * For instance, when the system status bar is displayed
     */
    if (WindowInsets.areStatusBarsVisible) return

    val showStatusBar by showStatusBar()


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
                    if (element !is StatusBar.Spacer) {
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
                            Spacer(with(density) { totalCutoutWidth.toDp() })
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
    val item: StatusBar
)

@Composable
public fun EditStatusBar() {
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

        val elementsJson = StatusBarJson.decode<List<StatusBar>>(loadedElements, emptyList())

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
        val elementsJson = StatusBarJson.encode(elements.map { it.item })
        scope.launch {
            StatusBarJsonSettingsStore.jsonSetting.set(ctx, elementsJson)
        }
    }

    fun addElement(element: StatusBar) {
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
            is StatusBar.Time -> item.copy()
            is StatusBar.Date -> item.copy()
            is StatusBar.Bandwidth -> item.copy()
            is StatusBar.Notifications -> item.copy()
            is StatusBar.Connectivity -> item.copy()
            is StatusBar.Spacer -> item.copy()
            is StatusBar.Battery -> item.copy()
            is StatusBar.NextAlarm -> item.copy()
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

    fun updateElement(updated: StatusBar) {
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
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
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
                            .border(1.dp, borderColor, MaterialTheme.shapes.large)
                            .clip(MaterialTheme.shapes.large)
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

                        is StatusBar.Bandwidth -> {
                            SwitchRow(
                                title = stringResource(R.string.merge_bandwidth),
                                description = "",
                                state = item.merge,
                            ) {
                                updateElement(item.copy(merge = it))
                            }
                        }

                        is StatusBar.Connectivity -> {
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

                        is StatusBar.Date -> {

                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    DateFormat.entries.filter { it != DateFormat.Custom }.forEach { format ->
                                        DragonButton(
                                            onClick = { updateElement(item.copy(formatter = format.pattern)) }
                                        ) {
                                            Text(DateUtils.nowFormattedDate(format.format))
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
                                            painter = painterResource(R.drawable.reset),
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

                        is StatusBar.Time -> {
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TimeFormat.entries.filter { it != TimeFormat.Custom }.forEach { format ->
                                        DragonButton(
                                            onClick = { updateElement(item.copy(formatter = format.pattern)) }
                                        ) {
                                            Text(DateUtils.nowFormattedTime(format.format))
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
                                            painter = painterResource(R.drawable.reset),
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

                        is StatusBar.Notifications -> {
                            SliderWithLabel(
                                label = stringResource(R.string.max_notification_icons),
                                value = item.maxIcons,
                                valueRange = 1..15,
                                onReset = { updateElement(item.copy(maxIcons = 5)) }
                            ) {
                                updateElement(item.copy(maxIcons = it))
                            }
                        }

                        is StatusBar.Spacer -> {
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

                        is StatusBar.Battery -> {
                            SwitchRow(
                                title = stringResource(R.string.show_percentage),
                                description = stringResource(R.string.show_percentage_desc),
                                state = item.showPercentage,
                            ) {
                                updateElement(item.copy(showPercentage = it))
                            }
                        }

                        is StatusBar.NextAlarm -> {
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
                                            painter = painterResource(R.drawable.reset),
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
                                painter = painterResource(R.drawable.close),
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
            allStatusBars.forEach { item ->

                val itemName = remember(item) { item::class.simpleName.toString() }

                DragonTooltip(itemName) {
                    Box(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.large)
                            .clip(MaterialTheme.shapes.large)
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
                TODO()
                load()
            }
        }
    ) {
        Text(stringResource(R.string.set_status_bar_template))
    }
}


@Composable
public fun StatusBarItem(
    element: StatusBar,
    launchAction: ((Action) -> Unit)? = null,
    previewMode: Boolean = false
): Unit = when (element) {
    is StatusBar.Bandwidth -> StatusBarBandwidth(element)

    is StatusBar.Connectivity -> StatusBarConnectivity(
        element = element,
        previewMode = previewMode
    )

    is StatusBar.Date -> StatusBarDate(
        element = element,
        onAction = launchAction,
    )

    is StatusBar.Time -> StatusBarTime(
        element = element,
        onAction = launchAction,
    )

    is StatusBar.Notifications -> StatusBarNotifications(element)

    is StatusBar.Spacer -> Text(stringResource(R.string.spacer))

    is StatusBar.Battery -> StatusBarBattery(element)

    is StatusBar.NextAlarm -> StatusBarNextAlarm(element, forceShowIcon = previewMode)
}


@Composable
public fun showStatusBar(): State<Boolean> {
    val mainScreensLayers = LocalMainScreenLayers.current

    return remember(mainScreensLayers) {
        derivedStateOf {
            val bar = mainScreensLayers
                .find { it is MainScreenLayer.StatusBar }
                ?: error("No status bar provided in the list")

            (bar as MainScreenLayer.StatusBar).enabled
        }
    }
}


@Composable
public fun showChargingAnimation(): State<Boolean> {
    val mainScreensLayers = LocalMainScreenLayers.current

    return remember(mainScreensLayers) {
        derivedStateOf {
            val charging = mainScreensLayers
                .find { it is MainScreenLayer.ChargingAnimation }
                ?: error("No charging animation provided in the list")

            (charging as MainScreenLayer.ChargingAnimation).enabled
        }
    }
}
