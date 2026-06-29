@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.map.HoldToActivateArcSettingsStore
import org.elnix.dragonlauncher.ui.base.UiConstants
import org.elnix.dragonlauncher.ui.base.modifiers.settingsGroupHorizontalPadding
import org.elnix.dragonlauncher.ui.base.withHaptic
import org.elnix.dragonlauncher.ui.composition.LocalHoldCustomObject
import org.elnix.dragonlauncher.ui.dialogs.HoldSettingsOrderSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSlider
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSwitchRow
import org.elnix.dragonlauncher.ui.helpers.HoldToActivateArc
import org.elnix.dragonlauncher.ui.helpers.customobjects.EditCustomObjectBlock
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.remembers.CustomObjectJson
import kotlin.time.Duration.Companion.milliseconds


@Composable
fun HoldToActivateArcTab(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()


    val holdDelayBeforeStartingLongClickSettings by HoldToActivateArcSettingsStore.holdDelayBeforeStartingLongClickSettings.asState()
    val longCLickSettingsDuration by HoldToActivateArcSettingsStore.longCLickSettingsDuration.asState()
    val holdToActivateSettingsTolerance by HoldToActivateArcSettingsStore.holdToActivateSettingsTolerance.asState()
    val showToleranceOnMainScreen by HoldToActivateArcSettingsStore.showToleranceOnMainScreen.asState()
    val rotationPerSecond by HoldToActivateArcSettingsStore.rotationPerSecond.asState()

    val holdCustomObject = LocalHoldCustomObject.current

    var mutableHoldObject by remember(holdCustomObject) { mutableStateOf(holdCustomObject) }
    var showHoldSettingsOrderDialog by remember { mutableStateOf(false) }
    var playAnimation by remember { mutableStateOf(true) }


    val rgbLoading by HoldToActivateArcSettingsStore.rgbLoading.asState()

    val progress = remember { Animatable(0f) }

    fun save() {
        val newAngleJson = CustomObjectJson.encode(mutableHoldObject)
        scope.launch {
            HoldToActivateArcSettingsStore.holdToActivateArcCustomObject.set(ctx, newAngleJson)
        }
    }

    SettingsScaffold(
        title = stringResource(R.string.hold_settings),
        onBack = {
            save()
            onBack()
        },
        helpText = stringResource(R.string.hold_settings_help),
        onReset = {
            scope.launch {
                HoldToActivateArcSettingsStore.resetAll(ctx)
            }
        },
        topContent = {
            var boxSize by remember { mutableStateOf(IntSize.Zero) }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {

                IconButton(
                    onClick = withHaptic(HapticFeedbackType.LongPress) { playAnimation = !playAnimation }
                ) {
                    AnimatedPlayPauseIcon(playAnimation)
                }


                SliderWithLabel(
                    label = stringResource(R.string.animated_progress),
                    showValue = false,
                    value = progress.value,
                    valueRange = 0f..1f
                ) {
                    scope.launch {
                        progress.animateTo(it)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .onSizeChanged { boxSize = it }
            ) {
                val center = Offset(
                    x = boxSize.width / 2f,
                    y = boxSize.height / 2f - 15f
                )

                HoldToActivateArc(
                    center = center,
                    progress = progress.value,
                    rgbLoading = rgbLoading,
                    rotationsPerSecond = rotationPerSecond,
                    customObject = mutableHoldObject,
                    playAnimation = playAnimation,
                    showHoldTolerance = if (showToleranceOnMainScreen) {
                        { holdToActivateSettingsTolerance }
                    } else null
                )
            }
        }
    ) {
        LaunchedEffect(
            holdDelayBeforeStartingLongClickSettings,
            longCLickSettingsDuration,
            playAnimation
        ) {
            while (playAnimation) {
                progress.snapTo(0f)
                delay(holdDelayBeforeStartingLongClickSettings.milliseconds)

                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = longCLickSettingsDuration,
                        easing = LinearEasing
                    )
                )
            }
        }

        DragonSettingsGroup(
            title = R.string.object_properties,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            EditCustomObjectBlock(
                editObject = mutableHoldObject,
                default = UiConstants.defaultAngleCustomObject
            ) { mutableHoldObject = it }
        }


        DragonSettingsGroup(
            title = R.string.configuration,
            contentPadding = PaddingValues(top = 12.dp)
        ) {
            SettingsSlider(
                setting = HoldToActivateArcSettingsStore.longCLickSettingsDuration,
                modifier = Modifier.settingsGroupHorizontalPadding()
            )
            SettingsSlider(
                setting = HoldToActivateArcSettingsStore.holdDelayBeforeStartingLongClickSettings,
                modifier = Modifier.settingsGroupHorizontalPadding()
            )
            SettingsSlider(
                setting = HoldToActivateArcSettingsStore.holdToActivateSettingsTolerance,
                modifier = Modifier.settingsGroupHorizontalPadding()
            )
            SettingsSlider(
                setting = HoldToActivateArcSettingsStore.rotationPerSecond,
                modifier = Modifier.settingsGroupHorizontalPadding()
            )
            SettingsItem(
                title = stringResource(R.string.edit_hold_to_activate_elements),
                description = stringResource(R.string.edit_hold_to_activate_elements_desc),
                icon = R.drawable.edit_rounded
            ) {
                showHoldSettingsOrderDialog = true
            }
            SettingsSwitchRow(HoldToActivateArcSettingsStore.showToleranceOnMainScreen)
            SettingsSwitchRow(HoldToActivateArcSettingsStore.rgbLoading)
        }
    }

    if (showHoldSettingsOrderDialog) {
        HoldSettingsOrderSheet { showHoldSettingsOrderDialog = false }
    }
}


@Composable
fun AnimatedPlayPauseIcon(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    val playToPause = rememberAnimatedVectorPainter(
        animatedImageVector = AnimatedImageVector.animatedVectorResource(R.drawable.pause_to_play),
        atEnd = !isPlaying
    )

    Icon(
        painter = playToPause,
        contentDescription = null,
        modifier = modifier.size(size)
    )
}
