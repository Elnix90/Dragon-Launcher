package org.elnix.dragonlauncher.base.model.serializables

import android.content.pm.ShortcutInfo
import android.os.Process
import android.os.UserHandle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import org.elnix.dragonlauncher.base.model.serializables.serializers.UserHandleSerializer
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
 *  - [org.elnix.dragonlauncher.base.util.ImageUtils.createUntintedBitmap]
 */
@Serializable
@SerialName("Action")
public sealed class Action {
    @get:DrawableRes
    public abstract val drawableId: Int

    @get:StringRes
    public abstract val resId: Int

    @Serializable
    @SerialName("LaunchApp")
    public data class LaunchApp(
        val packageName: String,
        val profile: Profile,
        @Transient
        val timerDuration: Int? = null
    ) : Action() {
        override val drawableId: Int = R.drawable.ic_app_grid
        override val resId: Int = R.string.open_app

        public constructor(application: Application) : this(
            application.packageName,
            application.profile
        )

        public companion object {
            public val dummy: LaunchApp = LaunchApp(DummyApp)
        }
    }

    @Serializable
    @SerialName("LaunchShortcut")
    public data class LaunchShortcut(
        val packageName: String,
        val shortcutId: String,
        @Serializable(UserHandleSerializer::class)
        val user: UserHandle
    ) : Action() {
        override val drawableId: Int = R.drawable.ic_action_pinned_shortcut
        override val resId: Int = R.string.pinned_shortcuts

        public companion object {
            public val dummy: LaunchShortcut = LaunchShortcut("", "", Process.myUserHandle())

            public fun ShortcutInfo.toAction(): LaunchShortcut = LaunchShortcut(`package`,id, userHandle)
        }
    }

    @Serializable
    @SerialName("OpenUrl")
    public data class OpenUrl(val url: String) : Action() {
        override val drawableId: Int = R.drawable.web
        override val resId: Int = R.string.open_url

        public companion object {
            public val dummy: OpenUrl = OpenUrl("")
        }
    }

    @Serializable
    @SerialName("OpenFile")
    public data class OpenFile(
        val uri: String,
        val mimeType: String? = null
    ) : Action() {
        override val drawableId: Int = R.drawable.ic_action_open_file
        override val resId: Int = R.string.open_file

        public companion object {
            public val dummy: OpenFile = OpenFile("")
        }
    }


    @Serializable
    @SerialName("OpenAppDrawer")
    public data class OpenAppDrawer(
        val workspaceId: String? = null
    ) : Action() {
        override val drawableId: Int = R.drawable.workspaces
        override val resId: Int = R.string.app_drawer

        public companion object {
            public val dummy: OpenAppDrawer = OpenAppDrawer("")
        }
    }

    @Serializable
    @SerialName("OpenDragonLauncherSettings")
    public data class OpenDragonLauncherSettings(
        val route: NavigationRoute = NavigationRoute.PointsSettings()
    ) : Action() {
        override val drawableId: Int = R.drawable.dragon_launcher_foreground
        override val resId: Int = R.string.dragon_launcher_settings

        public companion object {
            public val dummy: OpenDragonLauncherSettings = OpenDragonLauncherSettings(NavigationRoute.PointsSettings())
        }
    }

    @Serializable
    @SerialName("OpenCircleNest")
    public data class OpenCircleNest(
        val nestId: Int
    ) : Action() {
        override val drawableId: Int = R.drawable.nest_icon
        override val resId: Int = R.string.open_nest

        public companion object {
            public val dummy: OpenCircleNest = OpenCircleNest(0)
        }
    }

    @Serializable
    @SerialName("OpenWidget")
    public data class OpenWidget(
        val widgetId: Int,
        val providerPackage: String,
        val providerClass: String
    ) : Action() {
        override val drawableId: Int = R.drawable.widgets
        override val resId: Int = R.string.widgets

        public companion object {
            public val dummy: OpenWidget = OpenWidget(0, "", "")
        }
    }

    @Serializable
    @SerialName("ToggleWifi")
    public data class ToggleWifi(
        val command: WifiADBCommands = WifiADBCommands.Svc,
        val toast: Boolean? = false
    ) : Action() {
        override val drawableId: Int = R.drawable.wifi
        override val resId: Int = R.string.toggle_wifi

        public companion object {
            public val dummy: ToggleWifi = ToggleWifi()
        }
    }

    @Serializable
    @SerialName("ToggleBluetooth")
    public data class ToggleBluetooth(
        val command: BluetoothADBCommands = BluetoothADBCommands.Cmd,
        val toast: Boolean? = false
    ) : Action() {
        override val drawableId: Int = R.drawable.bluetooth
        override val resId: Int = R.string.toggle_bluetooth

        public companion object {
            public val dummy: ToggleBluetooth = ToggleBluetooth()
        }
    }

    @Serializable
    @SerialName("ToggleData")
    public data class ToggleData(
        val command: DataADBCommands = DataADBCommands.Svc,
        val toast: Boolean? = false
    ) : Action() {
        override val drawableId: Int = R.drawable.cellular_icon
        override val resId: Int = R.string.toggle_mobile_data

        public companion object {
            public val dummy: ToggleData = ToggleData()
        }
    }

    @Serializable
    @SerialName("RunAdbCommand")
    public data class RunAdbCommand(
        val command: String,
        val toast: Boolean? = false
    ) : Action() {
        override val drawableId: Int = R.drawable.adb_icon
        override val resId: Int = R.string.adb_command

        public companion object {
            public val dummy: RunAdbCommand = RunAdbCommand("")
        }
    }

    @Serializable
    @SerialName("Lock")
    public object Lock : Action() {
        override val drawableId: Int = R.drawable.lock
        override val resId: Int = R.string.lock

    }

    @Serializable
    @SerialName("ReloadApps")
    public object ReloadApps : Action() {
        override val drawableId: Int = R.drawable.reload
        override val resId: Int = R.string.reload_apps

    }

    @Serializable
    @SerialName("OpenRecentApps")
    public object OpenRecentApps : Action() {
        override val drawableId: Int = R.drawable.recent
        override val resId: Int = R.string.recent_apps

    }

    @Serializable
    @SerialName("NotificationShade")
    public object NotificationShade : Action() {
        override val drawableId: Int = R.drawable.notification
        override val resId: Int = R.string.notifications

    }

    @Serializable
    @SerialName("ControlPanel")
    public object ControlPanel : Action() {
        override val drawableId: Int = R.drawable.ic_action_grid
        override val resId: Int = R.string.control_panel

    }

    @Serializable
    @SerialName("GoParentNest")
    public object GoParentNest : Action() {
        override val drawableId: Int = R.drawable.fullscreen_exit
        override val resId: Int = R.string.go_parent_nest

    }

    @Serializable
    @SerialName("KillLauncher")
    public data object KillLauncher : Action() {
        override val drawableId: Int = R.drawable.ic_action_kill
        override val resId: Int = R.string.kill_launcher

    }

    @Serializable
    @SerialName("None")
    public object None : Action() {
        override val drawableId: Int = R.drawable.remove
        override val resId: Int = R.string.none

    }

    public companion object {
        public fun Action?.actionColor(
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

        public val defaultChoosableActions: List<Action> = listOf(
            LaunchApp.dummy,
            OpenCircleNest.dummy,
            GoParentNest,
            LaunchShortcut.dummy,
            OpenUrl.dummy,
            OpenFile.dummy,
            NotificationShade,
            ControlPanel,
            OpenAppDrawer.dummy,
            Lock,
            ReloadApps,
            OpenRecentApps,
            OpenDragonLauncherSettings.dummy,
            RunAdbCommand.dummy,
            ToggleBluetooth.dummy,
            ToggleWifi.dummy,
            ToggleData.dummy
        )

        public object ActionJson : DragonJson<Action>()

        public val actionsNumber: Int = Action::class.sealedSubclasses.size

    }
}