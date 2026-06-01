package org.elnix.dragonlauncher.ui.actions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.ktx.px
import org.elnix.dragonlauncher.ui.components.ShapedLauncherIcon
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
    viewModel: ApplicationItemVM = listItemViewModel(key = "search-${app.key}")
) {
    val badge by viewModel.badge.collectAsStateWithLifecycle(null)
    val icon by viewModel.icon.collectAsStateWithLifecycle()

    val maxIconSize by viewModel.iconService.maxIconSize.collectAsState(64)
    val maxIconSizePixels = maxIconSize.dp.px.toInt()

    LaunchedEffect(app) {
        viewModel.init(app, maxIconSizePixels)
    }

    ShapedLauncherIcon(
        modifier = modifier,
        maxIconSize = maxIconSize.dp,
        icon = { icon },
        badge = { badge }
    )
}


//@Composable
//fun ActionIcon(
//    action: ActionSerializable,
//    modifier: Modifier = Modifier,
//    size: Dp,
//    showLaunchAppVectorGrid: Boolean = false
//) {
//    val point = SwipePointSerializable.dummySwipePoint(action )
//
//    AppIcon(PointApp(point), size)
//
//
//    val ctx = LocalContext.current
//    val icons = LocalDrawerIconsCache.current
//    val extraColors = LocalExtraColors.current
//
//    val bitmap: ImageBitmap? = when {
//        action is ActionSerializable.LaunchApp && showLaunchAppVectorGrid ->
//            ctx.loadDrawableResAsBitmap(R.drawable.ic_app_grid, size, size)
//
//        else -> {
//            createUntintedBitmap(
//                icons = icons,
//                action = action,
//                ctx = ctx,
//                width = size,
//                height = size
//            )
//        }
//    }
//
//    if (bitmap == null) return
//
//    Image(
//        bitmap = bitmap,
//        contentDescription = null,
//        colorFilter = if (
//            ((action !is ActionSerializable.LaunchApp) || showLaunchAppVectorGrid) &&
//            (action !is ActionSerializable.LaunchShortcut || action.packageName.isEmpty()) &&
//            action !is ActionSerializable.OpenDragonLauncherSettings
//        ) ColorFilter.tint(actionColor(action, extraColors))
//        else null,
//        modifier = modifier
//            .clip(PlatformShape)
//    )
//}
