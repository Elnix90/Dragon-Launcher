package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.models.BluetoothADBCommands
import org.elnix.dragonlauncher.base.model.models.DataADBCommands
import org.elnix.dragonlauncher.base.model.models.DummyApp
import org.elnix.dragonlauncher.base.model.models.WifiADBCommands
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.base.theme.ExtraColors
import org.elnix.dragonlauncher.i18n.R


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
    abstract val drawable: Int

    @Serializable
    @SerialName("LaunchApp")
    data class LaunchApp(
        val packageName: String,
        val profile: Profile,
        @Transient
        val timerDuration: Int? = null
    ) : Action() {
        override val drawable: Int = R.drawable.ic_app_grid

        constructor(application: Application) : this(
            application.packageName,
            application.profile
        )

        companion object {
            fun dummy(): LaunchApp = LaunchApp(DummyApp())
        }
    }

    @Serializable
    @SerialName("LaunchShortcut")
    data class LaunchShortcut(
        val packageName: String,
        val shortcutId: String
    ) : Action() {
        override val drawable: Int = R.drawable.ic_action_pinned_shortcut

        companion object {
            fun dummy(): LaunchShortcut = LaunchShortcut("", "")
        }
    }

    @Serializable
    @SerialName("OpenUrl")
    data class OpenUrl(val url: String) : Action() {
        override val drawable: Int = R.drawable.web

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
        override val drawable: Int = R.drawable.ic_action_open_file
        companion object {
            fun dummy(): OpenFile = OpenFile("")
        }
    }


    @Serializable
    @SerialName("OpenAppDrawer")
    data class OpenAppDrawer(
        val workspaceId: String? = null
    ) : Action() {
        override val drawable: Int = R.drawable.workspaces
        companion object {
            fun dummy(): OpenAppDrawer = OpenAppDrawer("")
        }
    }

    @Serializable
    @SerialName("OpenDragonLauncherSettings")
    data class OpenDragonLauncherSettings(
        val route: NavigationRoute = NavigationRoute.PointsSettings()
    ) : Action() {
        override val drawable: Int = R.drawable.dragon_launcher_foreground
        companion object {
            fun dummy(): OpenDragonLauncherSettings = OpenDragonLauncherSettings(NavigationRoute.PointsSettings())
        }
    }

    @Serializable
    @SerialName("OpenCircleNest")
    data class OpenCircleNest(
        val nestId: Int
    ) : Action() {
        override val drawable: Int = R.drawable.nest_icon
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
        override val drawable: Int = R.drawable.widgets
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
        override val drawable: Int = R.drawable.wifi
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
        override val drawable: Int = R.drawable.bluetooth
        companion object {
            fun dummy(): ToggleBluetooth = ToggleBluetooth()
        }
    }

    @Serializable
    @SerialName("ToggleData")
    data class ToggleData(
        val command: DataADBCommands = DataADBCommands.Svc,
        val toast: Boolean? = false
    ) : Action() {
        override val drawable: Int = R.drawable.cellular_icon
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
        override val drawable: Int = R.drawable.adb_icon
        companion object {
            fun dummy(): RunAdbCommand = RunAdbCommand("")
        }
    }

    @Serializable
    @SerialName("Lock")
    object Lock : Action() {
        override val drawable: Int = R.drawable.lock
    }

    @Serializable
    @SerialName("ReloadApps")
    object ReloadApps : Action() {
        override val drawable: Int = R.drawable.reload
    }

    @Serializable
    @SerialName("OpenRecentApps")
    object OpenRecentApps : Action() {
        override val drawable: Int = R.drawable.recent
    }

    @Serializable
    @SerialName("NotificationShade")
    object NotificationShade : Action() {
        override val drawable: Int = R.drawable.notification
    }

    @Serializable
    @SerialName("ControlPanel")
    object ControlPanel : Action() {
        override val drawable: Int = R.drawable.ic_action_grid
    }

    @Serializable
    @SerialName("GoParentNest")
    object GoParentNest : Action() {
        override val drawable: Int = R.drawable.fullscreen_exit
    }

    @Serializable
    @SerialName("KillLauncher")
    data object KillLauncher : Action() {
        override val drawable: Int = R.drawable.ic_action_kill
    }

    @Serializable
    @SerialName("None")
    object None : Action() {
        override val drawable: Int = R.drawable.remove
    }

    companion object {
        fun Action?.actionColor(
            extraColors: ExtraColors,
            customColor: Color? = null
        ): Color =
            customColor
                ?: when (this) {
                    is LaunchApp, is LaunchShortcut, is OpenWidget -> extraColors.launchApp
                    is OpenUrl -> extraColors.openUrl
                    is OpenAppDrawer -> extraColors.openAppDrawer
                    is OpenDragonLauncherSettings -> extraColors.launcherSettings
                    is OpenFile -> extraColors.openFile
                    is ReloadApps -> extraColors.reload
                    is OpenCircleNest -> extraColors.openCircleNest
                    is RunAdbCommand -> extraColors.runAdbCommand
                    is ToggleBluetooth -> extraColors.toggleBluetooth
                    is ToggleData -> extraColors.toggleData
                    is ToggleWifi -> extraColors.toggleWifi
                    NotificationShade -> extraColors.notificationShade
                    ControlPanel -> extraColors.controlPanel
                    Lock -> extraColors.lock
                    OpenRecentApps -> extraColors.openRecentApps
                    GoParentNest -> extraColors.goParentNest
                    KillLauncher -> Color.Red

                    None, null -> Color.Unspecified
                }

        val defaultChoosableActions: List<Action> = listOf(
            LaunchApp.dummy(),
            OpenCircleNest.dummy(),
            GoParentNest,
            LaunchShortcut.dummy(),
            OpenUrl.dummy(),
            OpenFile.dummy(),
            NotificationShade,
            ControlPanel,
            OpenAppDrawer.dummy(),
            Lock,
            ReloadApps,
            OpenRecentApps,
            OpenDragonLauncherSettings.dummy(),
            RunAdbCommand.dummy(),
            ToggleBluetooth.dummy(),
            ToggleWifi.dummy(),
            ToggleData.dummy()
        )

        object ActionJson : DragonJson<Action>()

        val actionsNumber: Int = Action::class.sealedSubclasses.size

    }
}