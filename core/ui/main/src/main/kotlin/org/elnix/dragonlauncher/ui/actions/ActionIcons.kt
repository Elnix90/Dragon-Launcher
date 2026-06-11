package org.elnix.dragonlauncher.ui.actions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.models.PointViewModel
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.components.ShapedLauncherIcon


@Composable
fun PointIcon(
    point: Point,
    modifier: Modifier = Modifier,
    drawerViewModel: DrawerViewModel = activityViewModel(),
    pointViewModel: PointViewModel = activityViewModel()
) {
    when (val action = point.action) {

        is Action.LaunchApp -> {
            val app by drawerViewModel.findOne(action.packageName, action.profile.userHandle).collectAsState(null)
            if (app != null) {
                AppIcon(app!!)
            }
        }

        is Action.LaunchShortcut -> {
            TODO()
        }

        else -> {
            val defaultPoint by pointViewModel.defaultPoint.collectAsState()
            val resolvedResolution =
                point.resolution ?: defaultPoint.resolution
                ?: point.size ?: defaultPoint.size
                ?: Point.defaultSwipePointsValues.size!!

            ActionIcon(action, modifier, size = resolvedResolution.dp)
        }
    }
}

@Composable
fun AppIcon(
    app: Application,
    modifier: Modifier = Modifier,
    size: Dp? = null,
    drawerViewModel: DrawerViewModel = activityViewModel()
) {
    val badge by drawerViewModel.getBadge(app).collectAsStateWithLifecycle()
    val icon by drawerViewModel.getIcon(app).collectAsStateWithLifecycle()

    val iconSize by DrawerSettingsStore.iconSize.asState()
    val currentIconSize = size ?: iconSize

    ShapedLauncherIcon(
        modifier = modifier,
        size = currentIconSize,
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

//    val ctx = LocalContext.current
//    val extraColors = LocalExtraColors.current
//
//    val intSizePx = size.px.toInt()
//
//    val launcherIcon: LauncherIcon =
//        ActionIconCache.getOrCompute(action::class) {
//        }
//
//    Image(
//        bitmap = bitmap.asImageBitmap(),
//        contentDescription = null,
//        colorFilter = if (
//            ((action !is Action.LaunchApp)) &&
//            (action !is Action.LaunchShortcut || action.packageName.isEmpty()) &&
//            action !is Action.OpenDragonLauncherSettings
//        ) ColorFilter.tint(action.actionColor(extraColors))
//        else null,
//        modifier = modifier.clip(LocalIconShape.current.resolveShape())
//    )
}
