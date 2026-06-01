package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.model.models.BluetoothADBCommands
import org.elnix.dragonlauncher.base.model.models.DataADBCommands
import org.elnix.dragonlauncher.base.model.models.WifiADBCommands
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.base.theme.ExtraColors


/**
 * Swipe Actions Serializable, the core of the main gesture idea
 * Holds all the different actions the user can do
 *
 * Here are all the related settings in which you have to add logic in order to correctly add a new action:
 *  - `actionLabel`
 *  - `launchAction`
 *  - `actionColor`
 * *  - [org.elnix.dragonlauncher.base.util.ImageUtils.createUntintedBitmap]
 */
@Serializable
@SerialName("Action")
sealed class Action {

    @Serializable
    @SerialName("LaunchApp")
    data class LaunchApp(
        val packageName: String,
        val profile: Profile,
        @Transient
        val timerDuration: Int? = null
    ) : Action() {
        companion object {
            fun dummy(): OpenUrl = OpenUrl("")
        }
    }

    @Serializable
    @SerialName("LaunchShortcut")
    data class LaunchShortcut(
        val packageName: String,
        val shortcutId: String
    ) : Action() {
        companion object {
            fun dummy(): LaunchShortcut = LaunchShortcut("", "")
        }
    }

    @Serializable
    @SerialName("OpenUrl")
    data class OpenUrl(val url: String) : Action() {
        companion object {
            fun dummy(): OpenUrl = OpenUrl("")
        }
    }

    @Serializable
    @SerialName("OpenFile")
    data class OpenFile(
        val uri: String,
        val mimeType: String? = null
    ) : Action() {
        companion object {
            fun dummy(): OpenFile = OpenFile("")
        }
    }


    @Serializable
    @SerialName("OpenAppDrawer")
    data class OpenAppDrawer(
        val workspaceId: String? = null
    ) : Action() {
        companion object {
            fun dummy(): OpenAppDrawer = OpenAppDrawer("")
        }
    }

    @Serializable
    @SerialName("OpenDragonLauncherSettings")
    data class OpenDragonLauncherSettings(
        val route: NavigationRoute = NavigationRoute.PointsSettings()
    ) : Action() {
        companion object {
            fun dummy(): OpenDragonLauncherSettings = OpenDragonLauncherSettings(NavigationRoute.PointsSettings())
        }
    }

    @Serializable
    @SerialName("OpenCircleNest")
    data class OpenCircleNest(
        val nestId: Int
    ) : Action() {
        companion object {
            fun dummy(): OpenCircleNest = OpenCircleNest(0)
        }
    }

    @Serializable
    @SerialName("OpenWidget")
    data class OpenWidget(
        val widgetId: Int,
        val providerPackage: String,
        val providerClass: String
    ) : Action() {
        companion object {
            fun dummy(): OpenWidget = OpenWidget(0, "", "")
        }
    }

    @Serializable
    @SerialName("ToggleWifi")
    data class ToggleWifi(
        val command: WifiADBCommands = WifiADBCommands.Svc,
        val toast: Boolean? = false
    ) : Action() {
        companion object {
            fun dummy(): ToggleWifi = ToggleWifi()
        }
    }

    @Serializable
    @SerialName("ToggleBluetooth")
    data class ToggleBluetooth(
        val command: BluetoothADBCommands = BluetoothADBCommands.Cmd,
        val toast: Boolean? = false
    ) : Action() {
        companion object {
            fun dummy(): ToggleWifi = ToggleWifi()
        }
    }

    @Serializable
    @SerialName("ToggleData")
    data class ToggleData(
        val command: DataADBCommands = DataADBCommands.Svc,
        val toast: Boolean? = false
    ) : Action() {
        companion object {
            fun dummy(): ToggleData = ToggleData()
        }
    }

    @Serializable
    @SerialName("RunAdbCommand")
    data class RunAdbCommand(
        val command: String,
        val toast: Boolean? = false
    ) : Action() {
        companion object {
            fun dummy(): RunAdbCommand = RunAdbCommand("")
        }
    }

    @Serializable
    @SerialName("Lock")
    object Lock : Action()

//    @Serializable
//    @SerialName("ReloadApps")
//    object ReloadApps : Action()

    @Serializable
    @SerialName("OpenRecentApps")
    object OpenRecentApps : Action()

    @Serializable
    @SerialName("NotificationShade")
    object NotificationShade : Action()

    @Serializable
    @SerialName("ControlPanel")
    object ControlPanel : Action()

    @Serializable
    @SerialName("GoParentNest")
    object GoParentNest : Action()

    @Serializable
    @SerialName("KillLauncher")
    data object KillLauncher : Action()

    @Serializable
    @SerialName("None")
    object None : Action()

    companion object {
        fun Action?.actionColor(
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
//                    is ReloadApps -> extra.reload
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

        val defaultChoosableActions: Set<Action> = setOf(
            OpenCircleNest.dummy(),
            GoParentNest,
            LaunchApp.dummy(),
            LaunchShortcut.dummy(),
            OpenUrl.dummy(),
            OpenFile.dummy(),
            NotificationShade,
            ControlPanel,
            OpenAppDrawer.dummy(),
            Lock,
//            ReloadApps,
            OpenRecentApps,
            OpenDragonLauncherSettings.dummy(),
            RunAdbCommand.dummy(),
            ToggleBluetooth.dummy(),
            ToggleWifi.dummy(),
            ToggleData.dummy()
        )

        object ActionJson : DragonJson<Action>()

    }
}