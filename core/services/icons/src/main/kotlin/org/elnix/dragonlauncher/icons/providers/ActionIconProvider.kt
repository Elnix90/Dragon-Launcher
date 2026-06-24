package org.elnix.dragonlauncher.icons.providers

import android.content.Context
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.StaticIconLayer
import org.elnix.dragonlauncher.base.icons.TransparentLayer
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.Action.Companion.actionColor
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.theme.ExtraColors

class PointIconProvider(
    private val ctx: Context,
    private val point: Point,
    private val extrasColors: ExtraColors
) : IconProvider {
    override suspend fun getIcon(application: Application, size: Int): LauncherIcon? {

        val drawable = point.action.drawable.let {
            ContextCompat.getDrawable(ctx, it)
        } ?: return null

        val foregroundLayer = StaticIconLayer(
            icon = drawable,
            scale = 1f,
            tint = point.action.actionColor(extrasColors).toArgb()
        )

        return StaticLauncherIcon(
            foregroundLayer = foregroundLayer,
            backgroundLayer = TransparentLayer
        )
    }
}