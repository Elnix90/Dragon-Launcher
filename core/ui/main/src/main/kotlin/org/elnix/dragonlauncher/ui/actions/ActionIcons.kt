package org.elnix.dragonlauncher.ui.actions

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.serializables.AppModel
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.utils.Constants.Logging.ICONS_TAG
import org.elnix.dragonlauncher.common.utils.ImageUtils.createUntintedBitmap
import org.elnix.dragonlauncher.common.utils.ImageUtils.loadDrawableResAsBitmap
import org.elnix.dragonlauncher.common.utils.PlatformShape
import org.elnix.dragonlauncher.common.utils.resolveShape
import org.elnix.dragonlauncher.logging.logW
import org.elnix.dragonlauncher.ui.composition.LocalAppsViewModel
import org.elnix.dragonlauncher.ui.composition.LocalDrawerIconsCache
import org.elnix.dragonlauncher.ui.composition.LocalIconShape


@Composable
fun appIcon(app: AppModel): Painter {
    val icons = LocalDrawerIconsCache.current
    val appsViewModel = LocalAppsViewModel.current
    val profileKey = app.iconCacheKey

    val iconsTrigger by icons.iconsTrigger.collectAsState()

    key(iconsTrigger) {
        val cached = icons.getOrLazyCompute(profileKey) {
            appsViewModel.reloadAppIcon(app)
        }

        return if (cached != null) {
            BitmapPainter(cached)
        } else {
            val totalIconsNumber = icons.size

            logW(ICONS_TAG) { "Failed to get icon for ${app.iconCacheKey}, unknown reason\niconsTrigger: $iconsTrigger\ntotal icons number: $totalIconsNumber" }
            painterResource(R.drawable.ic_app_default)
        }
    }
}

@Composable
fun AppIcon(
    app: AppModel,
    maxIconSize: Dp
) {

    val iconShape = LocalIconShape.current

    Image(
        painter = appIcon(app),
        contentDescription = app.name,
        modifier = Modifier
            .sizeIn(maxWidth = maxIconSize)
            .aspectRatio(1f)
            .clip(iconShape.resolveShape()),
        contentScale = ContentScale.Fit
    )
}


@Composable
fun ActionIcon(
    action: SwipeActionSerializable,
    modifier: Modifier = Modifier,
    size: Int = 64,
    showLaunchAppVectorGrid: Boolean = false
) {
    val ctx = LocalContext.current
    val icons = LocalDrawerIconsCache.current
    val extraColors = LocalExtraColors.current

    val bitmap: ImageBitmap? = when {
        action is SwipeActionSerializable.LaunchApp && showLaunchAppVectorGrid ->
            ctx.loadDrawableResAsBitmap(R.drawable.ic_app_grid, size, size)

        else -> {
            createUntintedBitmap(
                icons = icons,
                action = action,
                ctx = ctx,
                width = size,
                height = size
            )
        }
    }

    if (bitmap == null) return

    Image(
        bitmap = bitmap,
        contentDescription = null,
        colorFilter = if (
            ((action !is SwipeActionSerializable.LaunchApp) || showLaunchAppVectorGrid) &&
            (action !is SwipeActionSerializable.LaunchShortcut || action.packageName.isEmpty()) &&
            action !is SwipeActionSerializable.OpenDragonLauncherSettings
        ) ColorFilter.tint(actionColor(action, extraColors))
        else null,
        modifier = modifier
            .clip(PlatformShape)
    )
}
