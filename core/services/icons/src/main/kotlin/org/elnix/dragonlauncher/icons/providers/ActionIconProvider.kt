package org.elnix.dragonlauncher.icons.providers

import android.content.Context
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.StaticIconLayer
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TintedIconLayer
import org.elnix.dragonlauncher.base.icons.TransparentLayer
import org.elnix.dragonlauncher.common.search.Application
import org.elnix.dragonlauncher.base.theme.ExtraColors
import org.elnix.dragonlauncher.common.serializables.SwipeAction
import org.elnix.dragonlauncher.common.serializables.SwipeAction.Companion.actionColor
import org.elnix.dragonlauncher.i18n.R

class ActionIconProvider(
    private val ctx: Context,
    private val action: SwipeAction,
//    private val showIconGrid: Boolean,
    private val extrasColors: ExtraColors
) : IconProvider {
    override suspend fun getIcon(application: Application, size: Int): LauncherIcon? {

        val drawableRes = when (action) {
            is SwipeAction.LaunchApp ->null // if (showIconGrid) null else R.drawable.ic_app_grid
            is SwipeAction.LaunchShortcut ->null // if (showIconGrid) null else R.drawable.ic_action_pinned_shortcut

            SwipeAction.None -> R.drawable.cancel
            SwipeAction.NotificationShade -> R.drawable.notification
            SwipeAction.ReloadApps -> R.drawable.ic_action_reload
            SwipeAction.OpenRecentApps -> R.drawable.ic_action_recent
            SwipeAction.KillLauncher -> R.drawable.ic_action_kill
            SwipeAction.GoParentNest -> R.drawable.fullscreen_exit
            SwipeAction.Lock -> R.drawable.ic_action_lock
            SwipeAction.ControlPanel -> R.drawable.ic_action_grid
            is SwipeAction.OpenUrl -> R.drawable.web
            is SwipeAction.OpenAppDrawer -> R.drawable.ic_action_drawer
            is SwipeAction.OpenDragonLauncherSettings -> R.drawable.dragon_launcher_foreground
            is SwipeAction.OpenFile -> R.drawable.ic_action_open_file
            is SwipeAction.OpenCircleNest -> R.drawable.ic_action_target
            is SwipeAction.OpenWidget -> R.drawable.ic_action_widgets
            is SwipeAction.RunAdbCommand -> R.drawable.adb_icon
            is SwipeAction.ToggleBluetooth -> R.drawable.bluetooth
            is SwipeAction.ToggleData -> R.drawable.cellular_icon
            is SwipeAction.ToggleWifi -> R.drawable.wifi
        }
        val drawable = drawableRes?.let {
            ContextCompat.getDrawable(ctx, it)
        } ?: return null

        val foregroundLayer = action.takeIf { it.a }?.let {
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