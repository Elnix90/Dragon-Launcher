@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.customization

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.enumsui.toggle.LockMethod
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.LockScreenViewModel
import org.elnix.dragonlauncher.models.ProfilesViewModel
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.modifiers.settingsGroupHorizontalPadding
import org.elnix.dragonlauncher.ui.dialogs.LockMethodDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSectionMode
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSlider
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSwitchRow
import org.elnix.dragonlauncher.ui.helpers.CustomActionSelector
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold


@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun BehaviorTab(
    onBack: () -> Unit,
    lockScreenViewModel: LockScreenViewModel = activityViewModel(),
    profilesViewModel: ProfilesViewModel = activityViewModel()
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val backAction by BehaviorSettingsStore.backAction.asState()
    val doubleClickAction by BehaviorSettingsStore.doubleClickAction.asState()
    val homeAction by BehaviorSettingsStore.homeAction.asState()
    val leftPadding by BehaviorSettingsStore.leftPadding.asState()
    val rightPadding by BehaviorSettingsStore.rightPadding.asState()
    val topPadding by BehaviorSettingsStore.topPadding.asState()
    val bottomPadding by BehaviorSettingsStore.bottomPadding.asState()

    val lockMethod by lockScreenViewModel.lockMethod.collectAsState()
    val superWarningModeEnabled = lockMethod != LockMethod.NONE

    val paddingState = rememberExpandableSection(stringResource(R.string.drag_zone_padding), mode = ExpandableSectionMode.Expandable)

    val showAppPreviewOverlay = paddingState.isExpanded()


    // Lock settings state
    var showLockMethodPicker by remember { mutableStateOf(false) }

    val superWarningState = rememberExpandableSection(
        stringResource(R.string.super_warning_mode)
    ) { superWarningModeEnabled }


    val lockDescription = when (lockMethod) {
        LockMethod.NONE -> stringResource(R.string.lock_none)
        LockMethod.PIN -> stringResource(R.string.lock_pin)
        LockMethod.DEVICE_UNLOCK -> stringResource(R.string.lock_device_unlock)
    }

    SettingsScaffold(
        title = stringResource(R.string.behavior),
        onBack = onBack,
        helpText = stringResource(R.string.behavior_help),
        onReset = {
            scope.launch {
                BehaviorSettingsStore.resetAll(ctx)
            }
        }
    ) {

        DragonSettingsGroup(R.string.action_settings) {
            CustomActionSelector(
                currentAction = backAction,
                label = stringResource(R.string.back_action),
                onToggle = {
                    scope.launch {
                        BehaviorSettingsStore.backAction.reset(ctx)
                    }
                }
            ) {
                scope.launch {
                    BehaviorSettingsStore.backAction.set(ctx, it)
                }
            }

            CustomActionSelector(
                currentAction = doubleClickAction,
                label = stringResource(R.string.double_click_action),
                onToggle = {
                    scope.launch {
                        BehaviorSettingsStore.doubleClickAction.reset(ctx)
                    }
                }
            ) {
                scope.launch {
                    BehaviorSettingsStore.doubleClickAction.set(ctx, it)
                }
            }
            CustomActionSelector(
                currentAction = homeAction,
                label = stringResource(R.string.home_action),
                onToggle = {
                    scope.launch {
                        BehaviorSettingsStore.homeAction.reset(ctx)
                    }
                }
            ) {
                scope.launch {
                    BehaviorSettingsStore.homeAction.set(ctx, it)
                }
            }
        }

        DragonSettingsGroup(R.string.common_settings) {
            SettingsSwitchRow(
                setting = BehaviorSettingsStore.keepScreenOn,
                title = stringResource(R.string.keep_screen_on),
                description = stringResource(R.string.keep_screen_on_desc)
            )

            SettingsSwitchRow(
                setting = BehaviorSettingsStore.disableHapticFeedbackGlobally,
                title = stringResource(R.string.disable_haptic_globally),
                description = stringResource(R.string.disable_haptic_globally_desc)
            )

            SettingsSwitchRow(
                setting = BehaviorSettingsStore.pointsActionSnapsToOuterCircle,
                title = stringResource(R.string.point_action_snaps_to_outer_circle),
                description = stringResource(R.string.point_action_snaps_to_outer_circle_desc)
            )

            SettingsSwitchRow(
                setting = BehaviorSettingsStore.promptForShortcutsWhenAddingApp,
                title = stringResource(R.string.prompt_shortcuts_when_adding_app),
                description = stringResource(R.string.prompt_shortcuts_when_adding_app_desc)
            )

            SettingsSwitchRow(
                setting = BehaviorSettingsStore.createLiveNestByDefaultWhenCreatingOpenCircleNestPoint,
                title = stringResource(R.string.create_live_nest_by_default),
                description = stringResource(R.string.create_live_nest_by_default_desc)
            )

            SettingsSlider(
                setting = BehaviorSettingsStore.offScreenTimeout,
                title = stringResource(R.string.off_screen_timeout),
                description = stringResource(R.string.off_screen_timeout_desc),
                valueRange = -1..60,
                modifier = Modifier
                    .settingsGroupHorizontalPadding()
                    .padding(bottom = 12.dp)
            )
        }

        ExpandableSection(paddingState) {
            SliderWithLabel(
                label = stringResource(R.string.left_padding),
                value = leftPadding,
                valueRange = 0..300,
                color = MaterialTheme.colorScheme.primary,
                showValue = true,
                onReset = {
                    scope.launch {
                        BehaviorSettingsStore.leftPadding.reset(ctx)
                    }
                },
                onChange = {
                    scope.launch {
                        BehaviorSettingsStore.leftPadding.set(ctx, it)
                    }
                }
            )

            SliderWithLabel(
                label = stringResource(R.string.right_padding),
                value = rightPadding,
                valueRange = 0..300,
                color = MaterialTheme.colorScheme.primary,
                showValue = true,
                onReset = {
                    scope.launch {
                        BehaviorSettingsStore.rightPadding.reset(ctx)
                    }
                },
                onChange = {
                    scope.launch {
                        BehaviorSettingsStore.rightPadding.set(ctx, it)
                    }
                }
            )

            SliderWithLabel(
                label = stringResource(R.string.top_padding),
                value = topPadding,
                valueRange = 0..300,
                color = MaterialTheme.colorScheme.primary,
                showValue = true,
                onReset = {
                    scope.launch {
                        BehaviorSettingsStore.topPadding.reset(ctx)
                    }
                },
                onChange = {
                    scope.launch {
                        BehaviorSettingsStore.topPadding.set(ctx, it)
                    }
                }
            )

            SliderWithLabel(
                label = stringResource(R.string.bottom_padding),
                value = bottomPadding,
                valueRange = 0..300,
                color = MaterialTheme.colorScheme.primary,
                showValue = true,
                onReset = {
                    scope.launch {
                        BehaviorSettingsStore.bottomPadding.reset(ctx)
                    }
                },
                onChange = {
                    scope.launch {
                        BehaviorSettingsStore.bottomPadding.set(ctx, it)
                    }
                }
            )
        }

        DragonSettingsGroup(R.string.security) {
            SettingsItem(
                title = stringResource(R.string.lock_method),
                description = lockDescription,
                icon = R.drawable.lock
            ) { showLockMethodPicker = true }

            ExpandableSection(superWarningState) {
                SettingsSwitchRow(
                    setting = BehaviorSettingsStore.superWarningMode,
                    enabled = superWarningModeEnabled,
                    title = stringResource(R.string.super_warning_mode),
                    description = stringResource(R.string.super_warning_mode_desc),
                )

                SettingsSwitchRow(
                    setting = BehaviorSettingsStore.vibrateOnError,
                    enabled = superWarningModeEnabled,
                    title = stringResource(R.string.vibrate_on_error),
                    description = stringResource(R.string.vibrate_on_error_desc),
                )

                SettingsSwitchRow(
                    setting = BehaviorSettingsStore.alarmSound,
                    enabled = superWarningModeEnabled,
                    title = stringResource(R.string.alarm_sound),
                    description = stringResource(R.string.super_warning_mode_desc),
                )

                SettingsSwitchRow(
                    setting = BehaviorSettingsStore.metalPipesSound,
                    enabled = superWarningModeEnabled,
                    title = stringResource(R.string.metal_pipes_sound),
                    description = stringResource(R.string.metal_pipes_sound_desc),
                )

                SettingsSlider(
                    setting = BehaviorSettingsStore.superWarningModeSound,
                    enabled = superWarningModeEnabled,
                    title = stringResource(R.string.super_warning_mode_sound),
                    description = stringResource(R.string.super_warning_mode_sound_desc),
                    valueRange = 0..100
                )
            }
        }
    }

    if (showAppPreviewOverlay) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                color = Color(0x55FF0000),
                topLeft = Offset(
                    leftPadding.toFloat(),
                    topPadding.toFloat()
                ),
                size = Size(
                    size.width - leftPadding - rightPadding.toFloat(),
                    size.height - topPadding - bottomPadding.toFloat()
                )
            )
        }
    }

    if (showLockMethodPicker) {
        LockMethodDialog { showLockMethodPicker = false }
    }
}

