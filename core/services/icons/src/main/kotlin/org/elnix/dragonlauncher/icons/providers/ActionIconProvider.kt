package org.elnix.dragonlauncher.icons.providers

import android.content.Context
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.StaticIconLayer
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TintedIconLayer
import org.elnix.dragonlauncher.base.icons.TransparentLayer
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Action.Companion.actionColor
import org.elnix.dragonlauncher.base.theme.ExtraColors
import org.elnix.dragonlauncher.i18n.R

class ActionIconProvider(
    private val ctx: Context,
    private val action: Action,
//    private val showIconGrid: Boolean,
    private val extrasColors: ExtraColors
) : IconProvider {
    override suspend fun getIcon(application: Application, size: Int): LauncherIcon? {

        val drawableRes = when (action) {
            is Action.LaunchApp -> null // if (showIconGrid) null else R.drawable.ic_app_grid
            is Action.LaunchShortcut -> null // if (showIconGrid) null else R.drawable.ic_action_pinned_shortcut

            Action.None -> R.drawable.cancel
            Action.NotificationShade -> R.drawable.notification
            Action.ReloadApps -> R.drawable.ic_action_reload
            Action.OpenRecentApps -> R.drawable.ic_action_recent
            Action.KillLauncher -> R.drawable.ic_action_kill
            Action.GoParentNest -> R.drawable.fullscreen_exit
            Action.Lock -> R.drawable.ic_action_lock
            Action.ControlPanel -> R.drawable.ic_action_grid
            is Action.OpenUrl -> R.drawable.web
            is Action.OpenAppDrawer -> R.drawable.ic_action_drawer
            is Action.OpenDragonLauncherSettings -> R.drawable.dragon_launcher_foreground
            is Action.OpenFile -> R.drawable.ic_action_open_file
            is Action.OpenCircleNest -> R.drawable.ic_action_target
            is Action.OpenWidget -> R.drawable.ic_action_widgets
            is Action.RunAdbCommand -> R.drawable.adb_icon
            is Action.ToggleBluetooth -> R.drawable.bluetooth
            is Action.ToggleData -> R.drawable.cellular_icon
            is Action.ToggleWifi -> R.drawable.wifi
        }
        val drawable = drawableRes?.let {
            ContextCompat.getDrawable(ctx, it)
        } ?: return null

        // TODO(rework that color tint thing)
        val foregroundLayer = action?.let {
            TintedIconLayer(
                icon = drawable,
                scale = 1f,
                color = it.actionColor(extrasColors).toArgb()
            )
        } ?: StaticIconLayer(
            icon = drawable
        )

        return StaticLauncherIcon(
            foregroundLayer = foregroundLayer,
            backgroundLayer = TransparentLayer
        )
    }
}