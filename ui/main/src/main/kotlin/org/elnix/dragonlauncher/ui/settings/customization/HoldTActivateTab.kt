package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.enumsui.toggle.HoldActions
import org.elnix.dragonlauncher.base.model.serializables.CustomGlow
import org.elnix.dragonlauncher.base.model.serializables.CustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.CustomObjectBlockProperties
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultHoldCustomObject
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.getCenter
import org.elnix.dragonlauncher.ktx.round
import org.elnix.dragonlauncher.ktx.toDp
import org.elnix.dragonlauncher.ktx.toHexWithAlpha
import org.elnix.dragonlauncher.models.SwipeViewModel
import org.elnix.dragonlauncher.settings.stores.map.ColorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.HoldToActivateArcSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.components.Preset
import org.elnix.dragonlauncher.ui.components.PresetRow
import org.elnix.dragonlauncher.ui.components.VerticalDragZone
import org.elnix.dragonlauncher.ui.compositionslocals.LocalHoldToActivateSettings
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.dialogs.HoldSettingsOrderSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.HoldToActivateArc
import org.elnix.dragonlauncher.ui.helpers.customobjects.EditCustomObjectBlock
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.remembers.rememberHoldToOpenSettings
import kotlin.time.Duration.Companion.milliseconds

@Stable
private data class HoldPreset(
    override val name: String,
    val customObject: CustomObject = defaultHoldCustomObject,
    val holdDelayBeforeStartingLongClickSettings: Int? = null,
    val longCLickSettingsDuration: Int? = null,
    val holdToActivateSettingsTolerance: Dp? = null,
    val showToleranceOnMainScreen: Boolean? = null,
    val rotationsPerSecond: Float? = null,
    val holdRgbLoading: Boolean? = null,
    val pulsingRadius: Float? = null,
    val pulsingRDuration: Int? = null,
    val color: Color? = null
) : Preset {
    override fun toString(): String =
        "HoldPreset(\n" +
            "    name = \"$name\",\n" +
            "    customObject = $customObject,\n" +
            "    holdDelayBeforeStartingLongClickSettings = $holdDelayBeforeStartingLongClickSettings,\n" +
            "    longCLickSettingsDuration = $longCLickSettingsDuration,\n" +
            "    holdToActivateSettingsTolerance = ${holdToActivateSettingsTolerance?.value?.round(2)},\n" +
            "    showToleranceOnMainScreen = $showToleranceOnMainScreen,\n" +
            "    rotationsPerSecond = ${rotationsPerSecond?.round(2)}f,\n" +
            "    holdRgbLoading = $holdRgbLoading,\n" +
            "    pulsingRadius = ${pulsingRadius?.round(2)}f,\n" +
            "    pulsingRDuration = $pulsingRDuration\n" +
            "    color = ${color?.let { "Color(0x${color.toHexWithAlpha.replace("#", "")}" }})\n" +
            ")"
}

@Composable
fun HoldToActivateTab(
    swipeViewModel: SwipeViewModel = activityViewModel()
) {
    val ctx = LocalContext.current
    val extraColors = LocalExtraColors.current
    val navigator = LocalNavigator.current
    val hapticFeedback = LocalHapticFeedback.current

    val scope = rememberCoroutineScope()

    val swipeService = swipeViewModel.swipeService
    val holdObject by swipeService.holdObject.asState()

    val holdSettings = LocalHoldToActivateSettings.current
    val holdDelayBeforeStartingLongClickSettings = holdSettings.holdDelayBeforeStartingLongClickSettings
    val longCLickSettingsDuration = holdSettings.longCLickSettingsDuration

    var showHoldSettingsOrderDialog by remember { mutableStateOf(false) }
    var playAnimation by remember { mutableStateOf(true) }
    var manualMode by remember { mutableStateOf(false) }

    val progress = remember { Animatable(0f) }

    val hold =
        rememberHoldToOpenSettings(
            onSettings = { },
            holdDelay = holdDelayBeforeStartingLongClickSettings.toLong(),
            loadDuration = longCLickSettingsDuration.toLong()
        )

    SettingsScaffold(
        title = stringResource(R.string.hold_settings),
        onBack = {
            scope.launch {
                swipeService.saveHoldObject()
                navigator.onBack()
            }
        },
        helpText = stringResource(R.string.hold_settings_help),
        resetText = stringResource(R.string.reset_hold_tab),
        onReset = {
            scope.launch {
                HoldToActivateArcSettingsStore.resetAll(ctx)
                swipeService.resetHoldObject()
            }
        },
        topContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                MultiSelectConnectedButtonRow(
                    entries = HoldActions.entries,
                    checked = {
                        when (it) {
                            HoldActions.ManualMode -> manualMode
                            HoldActions.PlayPause -> playAnimation
                        }
                    }
                ) {
                    when (it) {
                        HoldActions.ManualMode -> manualMode = !manualMode
                        HoldActions.PlayPause -> {
                            playAnimation = !playAnimation
                            manualMode = false
                        }
                    }
                }

                Spacer(5.dp)

                DragonSettingsGroup {
                    SliderWithLabel(
                        label = stringResource(R.string.animated_progress),
                        value = progress.value,
                        valueRange = 0f..1f,
                        resetEnabled = progress.value != 0f,
                        onReset = {
                            scope.launch {
                                progress.snapTo(0f)
                            }
                        }
                    ) {
                        scope.launch {
                            progress.animateTo(it)
                        }
                    }
                }
            }

            Column {
                var height by remember { mutableIntStateOf(0) }
                var isFirstPositioning by remember { mutableStateOf(true) }

                BoxWithConstraints(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(height.toDp)
                            .onGloballyPositioned { layoutCoordinates ->
                                if (isFirstPositioning) {
                                    height = layoutCoordinates.size.width
                                    isFirstPositioning = false
                                }
                            }.then(hold.pointerModifier)
                ) {
                    val center =
                        if (!manualMode) {
                            this.constraints.getCenter()
                        } else {
                            hold.center
                        }

                    val progress =
                        if (!manualMode) {
                            progress.value
                        } else {
                            hold.progress
                        }

                    HoldToActivateArc(
                        center = center,
                        progress = progress,
                        customObject = holdObject,
                        playAnimation = playAnimation
                    )
                }

                VerticalDragZone { height += it.toInt() }
            }
        }
    ) {
        LaunchedEffect(
            holdDelayBeforeStartingLongClickSettings,
            longCLickSettingsDuration,
            playAnimation,
            manualMode
        ) {
            if (!manualMode) {
                while (playAnimation) {
                    progress.snapTo(0f)
                    delay(holdDelayBeforeStartingLongClickSettings.milliseconds)

                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec =
                            tween(
                                durationMillis = longCLickSettingsDuration,
                                easing = LinearEasing
                            )
                    )
                }
            }
        }

        PresetRow(
            listOf(
                HoldPreset("Default"),
                HoldPreset(
                    name = "Elnix's",
                    customObject = CustomObject(
                        stroke = 4.5.dp,
                        color = null,
                        glow = CustomGlow(radius = 12.dp, color = null),
                        shape = IconShape.Random,
                        size = 75.0.dp,
                        rotation = -1,
                        mirror = false,
                        eraseBackground = false,
                        alignsWithDragAngle = false
                    ),
                    holdDelayBeforeStartingLongClickSettings = 300,
                    longCLickSettingsDuration = 500,
                    holdToActivateSettingsTolerance = 10.0.dp,
                    showToleranceOnMainScreen = false,
                    rotationsPerSecond = 0.50f,
                    holdRgbLoading = false,
                    pulsingRadius = 2.0f,
                    pulsingRDuration = 500,
                    color = Color(0xFFB902FF)
                )
            ),
            get = {
                HoldPreset(
                    name = "new",
                    customObject = swipeService.holdObject.value,
                    holdDelayBeforeStartingLongClickSettings = holdSettings.holdDelayBeforeStartingLongClickSettings,
                    longCLickSettingsDuration = holdSettings.longCLickSettingsDuration,
                    holdToActivateSettingsTolerance = holdSettings.holdToActivateSettingsTolerance,
                    showToleranceOnMainScreen = holdSettings.showToleranceOnMainScreen,
                    rotationsPerSecond = holdSettings.rotationsPerSecond,
                    holdRgbLoading = holdSettings.holdRgbLoading,
                    pulsingRadius = holdSettings.pulsingRadius,
                    pulsingRDuration = holdSettings.pulsingRDuration,
                    color = extraColors.holdToActivate
                )
            },
            set = { preset ->
                scope.launch {
                    swipeService.holdObject.value = preset.customObject
                    HoldToActivateArcSettingsStore.holdDelayBeforeStartingLongClickSettings.set(
                        ctx,
                        preset.holdDelayBeforeStartingLongClickSettings
                    )
                    HoldToActivateArcSettingsStore.longCLickSettingsDuration.set(ctx, preset.longCLickSettingsDuration)
                    HoldToActivateArcSettingsStore.holdToActivateSettingsTolerance.set(ctx, preset.holdToActivateSettingsTolerance)
                    HoldToActivateArcSettingsStore.showToleranceOnMainScreen.set(ctx, preset.showToleranceOnMainScreen)
                    HoldToActivateArcSettingsStore.rotationsPerSecond.set(ctx, preset.rotationsPerSecond)
                    HoldToActivateArcSettingsStore.holdRgbLoading.set(ctx, preset.holdRgbLoading)
                    HoldToActivateArcSettingsStore.pulsingRadius.set(ctx, preset.pulsingRadius)
                    HoldToActivateArcSettingsStore.pulsingRDuration.set(ctx, preset.pulsingRDuration)
                }
            }
        )

        EditCustomObjectBlock(
            title = R.string.object_properties,
            editObject = holdObject,
            default = defaultHoldCustomObject,
            properties =
                CustomObjectBlockProperties(
                    allowAlignCustomization = false,
                    allowEraseBackgroundCustomization = false
                )
        ) { swipeService.holdObject.value = it }

        DragonSettingsGroup(R.string.configuration) {
            Setting(HoldToActivateArcSettingsStore.longCLickSettingsDuration)
            Setting(HoldToActivateArcSettingsStore.holdDelayBeforeStartingLongClickSettings)
            Setting(HoldToActivateArcSettingsStore.rotationsPerSecond)
            DragonButton(
                onClick = {
                    scope.launch {
                        /**
                         * The number of rotations to achieve the same speed in both sides of the shape when playing (works best with circle)
                         */
                        val magicNumber = 1000f / holdSettings.longCLickSettingsDuration
                        HoldToActivateArcSettingsStore.rotationsPerSecond.set(ctx, magicNumber)
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.flash_auto),
                    contentDescription = null
                )
                Spacer(5.dp)
                Text(
                    text = stringResource(R.string.automatic_magic_number),
                    style = MaterialTheme.typography.labelMediumEmphasized
                )
            }

            SettingsItem(
                title = stringResource(R.string.edit_hold_to_activate_elements),
                description = stringResource(R.string.edit_hold_to_activate_elements_desc),
                icon = R.drawable.edit_rounded
            ) { showHoldSettingsOrderDialog = true }
            Setting(HoldToActivateArcSettingsStore.holdToActivateSettingsTolerance)
            Setting(HoldToActivateArcSettingsStore.showToleranceOnMainScreen)
            Setting(HoldToActivateArcSettingsStore.pulsingRadius)
            Setting(HoldToActivateArcSettingsStore.pulsingRDuration)
            Setting(HoldToActivateArcSettingsStore.holdRgbLoading)
            Setting(ColorSettingsStore.holdToActivateColor)
        }
    }

    if (showHoldSettingsOrderDialog) {
        HoldSettingsOrderSheet { showHoldSettingsOrderDialog = false }
    }
}
