package org.elnix.dragonlauncher.common.serializables

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.theme.ExtraColors
import org.elnix.dragonlauncher.common.messyfolder.BluetoothADBCommands
import org.elnix.dragonlauncher.common.messyfolder.DataADBCommands
import org.elnix.dragonlauncher.common.messyfolder.WifiADBCommands
import org.elnix.dragonlauncher.common.navigaton.NavigationRoute


/**
 * Swipe Actions Serializable, the core of the main gesture idea
 * Holds all the different actions the user can do
 *
 * Here are all the related settings in which you have to add logic in order to correctly add a new action:
 *  - `actionLabel`
 *  - `launchSwipeAction`
 *  - `actionColor`
* *  - [org.elnix.dragonlauncher.common.utils.ImageUtils.createUntintedBitmap]
 */
@Serializable
sealed class SwipeAction {
    @Serializable
    data class LaunchApp(
        val packageName: String,
        val isPrivateSpace: Boolean,
        val userId: Int?
    ) : SwipeAction()

    @Serializable
    data class LaunchShortcut(
        val packageName: String,
        val shortcutId: String
    ) : SwipeAction()

    @Serializable
    data class OpenUrl(val url: String) : SwipeAction()

    @Serializable
    data class OpenFile(
        val uri: String,
        val mimeType: String? = null
    ) : SwipeAction()

    @Serializable
    object NotificationShade : SwipeAction()

    @Serializable
    object ControlPanel : SwipeAction()

    @Serializable
    data class OpenAppDrawer(val workspaceId: String? = null) : SwipeAction()

    @Serializable
    data class OpenDragonLauncherSettings(val route: NavigationRoute = NavigationRoute.PointsSettings) : SwipeAction()

    @Serializable
    object Lock : SwipeAction()

    @Serializable
    object ReloadApps : SwipeAction()

    @Serializable
    object OpenRecentApps : SwipeAction()

    @Serializable
    data class OpenCircleNest(val nestId: Int) : SwipeAction()

    @Serializable
    object GoParentNest : SwipeAction()

    @Serializable
    data class OpenWidget(
        val widgetId: Int,
        val providerPackage: String,
        val providerClass: String
    ) : SwipeAction()

    @Serializable
    data class ToggleWifi(
        val command: WifiADBCommands = WifiADBCommands.Svc,
        val toast: Boolean? = false
    ) : SwipeAction()

    @Serializable
    data class ToggleBluetooth(
        val command: BluetoothADBCommands = BluetoothADBCommands.Cmd,
        val toast: Boolean? = false
    ) : SwipeAction()

    @Serializable
    data class ToggleData(
        val command: DataADBCommands = DataADBCommands.Svc,
        val toast: Boolean? = false
    ) : SwipeAction()

    @Serializable
    data class RunAdbCommand(
        val command: String,
        val toast: Boolean? = false
    ) : SwipeAction()

    @Serializable
    data object KillLauncher : SwipeAction()
    @Serializable
    object None : SwipeAction()

    companion object {
        fun SwipeAction?.actionColor(
            extra: ExtraColors,
            customColor: Color? = null
        ): Color =
            customColor
                ?: when (this) {
                    is LaunchApp, is LaunchShortcut, is OpenWidget -> extra.launchApp
                    is OpenUrl -> extra.openUrl
                    is OpenAppDrawer -> extra.openAppDrawer
                    is OpenDragonLauncherSettings -> extra.launcherSettings
                    is OpenFile -> extra.openFile
                    is ReloadApps -> extra.reload
                    is OpenCircleNest -> extra.openCircleNest
                    is RunAdbCommand -> extra.runAdbCommand
                    is ToggleBluetooth -> extra.toggleBluetooth
                    is ToggleData -> extra.toggleData
                    is ToggleWifi -> extra.toggleWifi
                    NotificationShade -> extra.notificationShade
                    ControlPanel -> extra.controlPanel
                    Lock -> extra.lock
                    OpenRecentApps -> extra.openRecentApps
                    GoParentNest -> extra.goParentNest
                    KillLauncher -> Color.Red

                    None, null -> Color.Unspecified
                }

        val defaultChoosableActions: Set<SwipeAction> = setOf(
            OpenCircleNest(0),
            GoParentNest,
            LaunchApp("", false, 0),
            LaunchShortcut("", ""),
            OpenUrl(""),
            OpenFile(""),
            NotificationShade,
            ControlPanel,
            OpenAppDrawer(),
            Lock,
            ReloadApps,
            OpenRecentApps,
            OpenDragonLauncherSettings(),
            RunAdbCommand(""),
            ToggleBluetooth(),
            ToggleWifi(),
            ToggleData()
        )

        object SwipeActionJson: DragonJson<SwipeAction>()

    }
}
