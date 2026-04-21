package org.elnix.dragonlauncher.ui.actions

import androidx.compose.ui.graphics.Color
import org.elnix.dragonlauncher.base.theme.ExtraColors
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable

fun actionColor(
    action: SwipeActionSerializable?,
    extra: ExtraColors,
    customColor: Color? = null
): Color =
    customColor
        ?: when (action) {
            is SwipeActionSerializable.LaunchApp, is SwipeActionSerializable.LaunchShortcut, is SwipeActionSerializable.OpenWidget -> extra.launchApp
            is SwipeActionSerializable.OpenUrl -> extra.openUrl
            SwipeActionSerializable.NotificationShade -> extra.notificationShade
            SwipeActionSerializable.ControlPanel -> extra.controlPanel
            is SwipeActionSerializable.OpenAppDrawer -> extra.openAppDrawer
            is SwipeActionSerializable.OpenDragonLauncherSettings -> extra.launcherSettings
            SwipeActionSerializable.Lock -> extra.lock
            is SwipeActionSerializable.OpenFile -> extra.openFile
            is SwipeActionSerializable.ReloadApps -> extra.reload
            SwipeActionSerializable.OpenRecentApps -> extra.openRecentApps
            is SwipeActionSerializable.OpenCircleNest -> extra.openCircleNest
            SwipeActionSerializable.GoParentNest -> extra.goParentNest
            is SwipeActionSerializable.RunAdbCommand -> extra.runAdbCommand
            is SwipeActionSerializable.ToggleBluetooth -> extra.toggleBluetooth
            is SwipeActionSerializable.ToggleData -> extra.toggleData
            is SwipeActionSerializable.ToggleWifi -> extra.toggleWifi
            SwipeActionSerializable.KillLauncher -> Color.Red

            SwipeActionSerializable.None, null -> Color.Unspecified
        }
