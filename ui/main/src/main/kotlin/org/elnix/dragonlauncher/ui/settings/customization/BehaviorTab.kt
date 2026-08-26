package org.elnix.dragonlauncher.ui.settings.customization

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.enumsui.toggle.LockMethod.Device
import org.elnix.dragonlauncher.base.model.enumsui.toggle.LockMethod.None
import org.elnix.dragonlauncher.base.model.enumsui.toggle.LockMethod.Pattern
import org.elnix.dragonlauncher.base.model.enumsui.toggle.LockMethod.Pin
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.dialogs.security.LockMethodDialog
import org.elnix.dragonlauncher.ui.dialogs.security.SecretUnlockButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.model.ExpandableSectionMode
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.SettingActionSelector
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import kotlin.time.Duration.Companion.seconds


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
    val superWarningModeEnabled = lockMethod != None

    val paddingState = rememberExpandableSection(
        title = R.string.drag_zone_padding,
        description = R.string.drag_zone_padding_desc,
        icon = R.drawable.center_focus_strong,
        mode = ExpandableSectionMode.Expandable
    )
    val showAppPreviewOverlay = paddingState.isExpanded()

    val superWarningState = rememberExpandableSection(
        title = R.string.super_warning_mode,
        description = R.string.super_warning_mode_desc,
        icon = R.drawable.lock,
        mode = ExpandableSectionMode.ModalSheet(true),
        enabled = superWarningModeEnabled
    )

    var showLockMethodPicker by remember { mutableStateOf(false) }
    var hasEnabledSecretUnlockButton by remember { mutableStateOf(false) }
    LaunchedEffect(hasEnabledSecretUnlockButton) {
        if (!hasEnabledSecretUnlockButton) return@LaunchedEffect
        delay(1.seconds)
        hasEnabledSecretUnlockButton = false
    }

    Box {
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
                    this@DragonSettingsGroup.Setting(BehaviorSettingsStore.rightPadding)
                    this@DragonSettingsGroup.Setting(BehaviorSettingsStore.leftPadding)
                    this@DragonSettingsGroup.Setting(BehaviorSettingsStore.topPadding)
                    this@DragonSettingsGroup.Setting(BehaviorSettingsStore.bottomPadding)
                }
            }

            DragonSettingsGroup(R.string.security) {
                SettingsItem(
                    title = stringResource(R.string.lock_method),
                    description = when (lockMethod) {
                        None -> stringResource(R.string.lock_none)
                        Pin -> stringResource(R.string.lock_pin)
                        Device -> stringResource(R.string.lock_device_unlock)
                        Pattern -> stringResource(R.string.pattern)
                    },
                    icon = R.drawable.lock
                ) { showLockMethodPicker = true }

                Setting(BehaviorSettingsStore.secretUnlockButton, enabled = lockMethod != None) { hasEnabledSecretUnlockButton = it }

                ExpandableSection(superWarningState) {
                    this@DragonSettingsGroup.Setting(
                        setting = BehaviorSettingsStore.superWarningMode,
                        enabled = superWarningModeEnabled
                    )

                    this@DragonSettingsGroup.Setting(
                        setting = BehaviorSettingsStore.vibrateOnError,
                        enabled = superWarningModeEnabled
                    )

                    this@DragonSettingsGroup.Setting(
                        setting = BehaviorSettingsStore.alarmSound,
                        enabled = superWarningModeEnabled
                    )

                    this@DragonSettingsGroup.Setting(
                        setting = BehaviorSettingsStore.metalPipesSound,
                        enabled = superWarningModeEnabled
                    )

                    this@DragonSettingsGroup.Setting(
                        setting = BehaviorSettingsStore.superWarningModeSound,
                        enabled = superWarningModeEnabled
                    )
                }
            }
        }

        if (hasEnabledSecretUnlockButton) {
            SecretUnlockButton(false)
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
