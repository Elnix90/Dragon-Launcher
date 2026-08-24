package org.elnix.dragonlauncher.icons.providers

import android.content.Context
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.StaticIconLayer
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TransparentLayer
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Action.Companion.actionColor
import org.elnix.dragonlauncher.base.theme.ExtraColors

/**
 * ### Action icon provider
 * Provides an icon for every [Action]
 *
 * **This is the core and base function of all Dragon Launcher providers.**
 */
internal class ActionIconProvider(
    private val ctx: Context,
    private val extraColors: ExtraColors
) : IconProvider {
    override suspend fun getIcon(action: Action, size: Int): LauncherIcon? {


        when (action) {
            is Action.LaunchApp -> {
                // When the launch app is the dummy one, I return the app grid instead of an action icon
                if (action != Action.LaunchApp.dummy) return null
            }
            is Action.LaunchShortcut -> {
                if (action != Action.LaunchShortcut.dummy) return null
            }
            else -> {/* no-op */}
        }

        val drawable = action.drawableId.let {
            ContextCompat.getDrawable(ctx, it)
        } ?: return null


        val foregroundLayer = StaticIconLayer(
            icon = drawable,
            scale = 1f,
            tint = action.actionColor(extraColors).toArgb()
        )

        return StaticLauncherIcon(
            foregroundLayer = foregroundLayer,
            backgroundLayer = TransparentLayer
        )
    }
}