@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.customization

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.modifiers.settingsGroupHorizontalPadding
import org.elnix.dragonlauncher.ui.dialogs.LockMethodDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSectionMode
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSlider
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSwitchRow
import org.elnix.dragonlauncher.ui.helpers.SettingActionSelector
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold


@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun BehaviorTab(
    onBack: () -> Unit,
    lockScreenViewModel: LockScreenViewModel = activityViewModel()
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val leftPadding by BehaviorSettingsStore.leftPadding.asState()
    val rightPadding by BehaviorSettingsStore.rightPadding.asState()
    val topPadding by BehaviorSettingsStore.topPadding.asState()
    val bottomPadding by BehaviorSettingsStore.bottomPadding.asState()

    val lockMethod by lockScreenViewModel.lockMethod.collectAsState()
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
        onBack = onBack,
        helpText = stringResource(R.string.behavior_help),
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
            SettingsSwitchRow(BehaviorSettingsStore.keepScreenOn)
            SettingsSwitchRow(BehaviorSettingsStore.disableHapticFeedbackGlobally)
            SettingsSwitchRow(BehaviorSettingsStore.pointsActionSnapsToOuterCircle)
            SettingsSwitchRow(BehaviorSettingsStore.promptForShortcutsWhenAddingApp)
            SettingsSwitchRow(BehaviorSettingsStore.createLiveNestByDefaultWhenCreatingOpenCircleNestPoint)

            SettingsSlider(
                setting = BehaviorSettingsStore.offScreenTimeout,
                modifier = Modifier
                    .settingsGroupHorizontalPadding()
                    .padding(bottom = 12.dp)
            )
        }

        ExpandableSection(paddingState) {
            SettingsSlider(BehaviorSettingsStore.rightPadding)
            SettingsSlider(BehaviorSettingsStore.leftPadding)
            SettingsSlider(BehaviorSettingsStore.topPadding)
            SettingsSlider(BehaviorSettingsStore.bottomPadding)
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
                    enabled = superWarningModeEnabled
                )

                SettingsSwitchRow(
                    setting = BehaviorSettingsStore.vibrateOnError,
                    enabled = superWarningModeEnabled
                )

                SettingsSwitchRow(
                    setting = BehaviorSettingsStore.alarmSound,
                    enabled = superWarningModeEnabled
                )

                SettingsSwitchRow(
                    setting = BehaviorSettingsStore.metalPipesSound,
                    enabled = superWarningModeEnabled
                )

                SettingsSlider(
                    setting = BehaviorSettingsStore.superWarningModeSound,
                    enabled = superWarningModeEnabled
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

//        DeadZoneCanva(
//            PaddingValues(leftPadding, topPadding, rightPadding, bottomPadding)
//        )
    }

    if (showLockMethodPicker) {
        LockMethodDialog { showLockMethodPicker = false }
    }
}

//
//private val boxColor = Color(0x55FF0000)
//private val cornersColor = boxColor.alphaMultiplier(1.5f)
//
//@Composable
//private fun DeadZoneCanva(
//    paddingValues: PaddingValues
//) {
//    Canvas(
//        Modifier
//            .fillMaxSize()
//            .padding(paddingValues)
//    ) {
//        drawRect(boxColor)
//        for (corner in ResizeCorner.entries) {
//            cornerAnchor(
//                center = when (corner) {
//                    TopLeft -> size
//                    TopRight -> TODO()
//                    BottomLeft -> TODO()
//                    BottomRight -> TODO()
//                },
//                corner = corner
//            )
//        }
//    }
//}
//
//
//
//private fun DrawScope.cornerAnchor(center: Offset, corner: ResizeCorner) {
//    rotate(when(corner) {
//        TopLeft -> 0f
//        TopRight -> 90f
//        BottomLeft -> 180f
//        BottomRight -> -90f
//    }) {
//        drawRect(
//            color = cornersColor,
//            topLeft = center,
//            size = Size(50f,50f)
//        )
//    }
//}