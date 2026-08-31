package org.elnix.dragonlauncher.ui.actions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.models.IconsViewModel
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.components.ShapedLauncherIcon

@Composable
fun FinalPointIcon(
    point: Point,
    size: Dp?,
    modifier: Modifier = Modifier,
    drawerViewModel: DrawerViewModel = activityViewModel(),
    pointsViewModel: PointsViewModel = activityViewModel()
) {
    val pointsService = pointsViewModel.pointsService
    val defaultPoint by pointsService.defaultPoint.asState()

    val pointSize = size ?: point.getSize(defaultPoint, false)

    when (val action = point.action) {
        is Action.LaunchApp -> {
            val app by drawerViewModel.findOne(action).collectAsState(null)
            if (app != null) {
                AppIcon(
                    app = app!!,
                    size = pointSize,
                    modifier = modifier
                )
            }
        }

        is Action.LaunchShortcut -> {
            ShortcutIcon(action, pointSize)
        }

        else -> {
            ActionIcon(
                action = action,
                size = pointSize,
                modifier = modifier
            )
        }
    }
}

@Composable
fun AppIcon(
    app: Application,
    size: Dp,
    modifier: Modifier = Modifier,
    iconsViewModel: IconsViewModel = activityViewModel()
) {
    val badge by iconsViewModel.getBadge(app).collectAsStateWithLifecycle()
    val icon by iconsViewModel.getIcon(app).collectAsStateWithLifecycle()

    ShapedLauncherIcon(
        modifier = modifier,
        size = size,
        icon = { icon },
        badge = { badge }
    )
}

@Composable
fun ShortcutIcon(
    shortcut: Action.LaunchShortcut,
    size: Dp,
    modifier: Modifier = Modifier,
    iconsViewModel: IconsViewModel = activityViewModel()
) {
    val icon by iconsViewModel.getIcon(shortcut).collectAsStateWithLifecycle()

    ShapedLauncherIcon(
        modifier = modifier,
        size = size,
        icon = { icon }
    )
}

@Composable
fun ActionIcon(
    action: Action,
    size: Dp,
    modifier: Modifier = Modifier,
    iconsViewModel: IconsViewModel = activityViewModel()
) {
    val icon by iconsViewModel.getIcon(action).collectAsStateWithLifecycle()

    ShapedLauncherIcon(
        modifier = modifier,
        size = size,
        icon = { icon }
    )
}
