package org.elnix.dragonlauncher.ui.actions

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.elnix.dragonlauncher.base.cache.ActionIconCache
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Action.Companion.actionColor
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.base.util.ImageUtils.createUntintedBitmap
import org.elnix.dragonlauncher.ktx.px
import org.elnix.dragonlauncher.ui.components.ShapedLauncherIcon
import org.elnix.dragonlauncher.ui.composition.LocalIconShape
import org.elnix.dragonlauncher.ui.drawer.ApplicationItemVM
import org.elnix.dragonlauncher.ui.drawer.listItemViewModel


//@Composable TODO
//fun appIcon(
//    app: Application,
//    appsViewModel: AppsViewModel = activityViewModel()
//): Painter {
//    val icons = LocalDrawerIconsCache.current
//    val profileKey = app.key
//
//    val iconsTrigger by icons.iconsTrigger.collectAsState()
//
//    key(iconsTrigger) {
//        val cached = icons.getOrLazyCompute(profileKey) {
//            appsViewModel.reloadAppIcon(app)
//        }
//
//        return if (cached != null) {
//            BitmapPainter(cached)
//        } else {
//            val totalIconsNumber = icons.size
//
//            logW(ICONS_TAG) { "Failed to get icon for ${app.key}, unknown reason\niconsTrigger: $iconsTrigger\ntotal icons number: $totalIconsNumber" }
//            painterResource(R.drawable.ic_app_default)
//        }
//    }
//}

@Composable
fun AppIcon(
    app: Application,
    modifier: Modifier = Modifier,
    maxSize: Dp? = null,
    viewModel: ApplicationItemVM = listItemViewModel(key = "search-${app.key}")
) {
    val badge by viewModel.badge.collectAsStateWithLifecycle(null)
    val icon by viewModel.icon.collectAsStateWithLifecycle()

    val maxIconSize by viewModel.iconService.maxIconSize.collectAsState(64)
    val currentMaxIconSize = maxSize ?: maxIconSize.dp
    val maxIconSizePixels = currentMaxIconSize.px.toInt()

    LaunchedEffect(app) {
        viewModel.init(app, maxIconSizePixels)
    }

    ShapedLauncherIcon(
        modifier = modifier,
        maxIconSize = currentMaxIconSize,
        icon = { icon },
        badge = { badge }
    )
}



@Composable
fun ActionIcon(
    action: Action,
    modifier: Modifier = Modifier,
    size: Dp,
//    showLaunchAppVectorGrid: Boolean = false
) {

    val ctx = LocalContext.current
    val extraColors = LocalExtraColors.current

    val intSizePx = size.px.toInt()

    val bitmap: Bitmap =
        ActionIconCache.getOrCompute(action::class) {


//        when {
//        action is Action.LaunchApp && showLaunchAppVectorGrid ->
//            ctx.loadDrawableResAsBitmap(R.drawable.ic_app_grid, size, size)
//
//        else -> {
            createUntintedBitmap(
                action = action,
                ctx = ctx,
                width = intSizePx,
                height = intSizePx
            )
        }

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        colorFilter = if (
            ((action !is Action.LaunchApp)) &&
            (action !is Action.LaunchShortcut || action.packageName.isEmpty()) &&
            action !is Action.OpenDragonLauncherSettings
        ) ColorFilter.tint(action.actionColor(extraColors))
        else null,
        modifier = modifier.clip(LocalIconShape.current.resolveShape())
    )
}
