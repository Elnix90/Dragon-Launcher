@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.customization

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.enumsui.toggle.LockMethod
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.dialogs.LockMethodDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.model.ExpandableSectionMode
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.SettingActionSelector
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold


@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun BehaviorTab() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val leftPadding by BehaviorSettingsStore.leftPadding.asState()
    val rightPadding by BehaviorSettingsStore.rightPadding.asState()
    val topPadding by BehaviorSettingsStore.topPadding.asState()
    val bottomPadding by BehaviorSettingsStore.bottomPadding.asState()

    val lockMethod by PrivateSettingsStore.lockMethod.asState()
    val superWarningModeEnabled = lockMethod != LockMethod.None

    val paddingState = rememberExpandableSection(stringResource(R.string.drag_zone_padding), mode = ExpandableSectionMode.Expandable)
    val showAppPreviewOverlay = paddingState.isExpanded()

    var showLockMethodPicker by remember { mutableStateOf(false) }

    val superWarningState = rememberExpandableSection(
        stringResource(R.string.super_warning_mode)
    ) { superWarningModeEnabled }


    val lockDescription = when (lockMethod) {
        LockMethod.None -> stringResource(R.string.lock_none)
        LockMethod.Pin -> stringResource(R.string.lock_pin)
        LockMethod.Device -> stringResource(R.string.lock_device_unlock)
    }

    SettingsScaffold(
        title = stringResource(R.string.behavior),
        helpText = stringResource(R.string.behavior_help),
        resetText = stringResource(R.string.reset_behavior_tab),
        onReset = {
            scope.launch {
                BehaviorSettingsStore.resetAll(ctx)
            }
        }
    ) {

        DragonSettingsGroup(R.string.action_settings) {
            SettingActionSelector(BehaviorSettingsStore.backAction)
            SettingActionSelector(BehaviorSettingsStore.doubleClickAction)
            SettingActionSelector(BehaviorSettingsStore.homeAction)
        }

        DragonSettingsGroup(R.string.common_settings) {
            Setting(BehaviorSettingsStore.keepScreenOn)
            Setting(BehaviorSettingsStore.disableHapticFeedbackGlobally)
            Setting(BehaviorSettingsStore.promptForShortcutsWhenAddingApp)
            Setting(BehaviorSettingsStore.openRootNestEachTime)
            Setting(BehaviorSettingsStore.createLiveNestByDefaultWhenCreatingOpenCircleNestPoint)
            Setting(BehaviorSettingsStore.offScreenTimeout)
        }

        DragonSettingsGroup(R.string.padding) {
            ExpandableSection(paddingState) {
                Setting(BehaviorSettingsStore.rightPadding)
                Setting(BehaviorSettingsStore.leftPadding)
                Setting(BehaviorSettingsStore.topPadding)
                Setting(BehaviorSettingsStore.bottomPadding)
            }
        }

        DragonSettingsGroup(R.string.security) {
            SettingsItem(
                title = stringResource(R.string.lock_method),
                description = lockDescription,
                icon = R.drawable.lock
            ) { showLockMethodPicker = true }

            ExpandableSection(superWarningState) {
                Setting(
                    setting = BehaviorSettingsStore.superWarningMode,
                    enabled = superWarningModeEnabled
                )

                Setting(
                    setting = BehaviorSettingsStore.vibrateOnError,
                    enabled = superWarningModeEnabled
                )

                Setting(
                    setting = BehaviorSettingsStore.alarmSound,
                    enabled = superWarningModeEnabled
                )

                Setting(
                    setting = BehaviorSettingsStore.metalPipesSound,
                    enabled = superWarningModeEnabled
                )

                Setting(
                    setting = BehaviorSettingsStore.superWarningModeSound,
                    enabled = superWarningModeEnabled
                )
            }
        }
    }

    if (showAppPreviewOverlay) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                color = Color(0x40FF0000),
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
