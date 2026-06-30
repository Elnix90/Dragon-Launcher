package org.elnix.dragonlauncher.ui.actions

import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.icons.DynamicLauncherIcon
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.LauncherIconRenderSettings
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
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
    modifier: Modifier = Modifier,
    drawerViewModel: DrawerViewModel = activityViewModel(),
    pointsViewModel: PointsViewModel = activityViewModel()
) {
    val pointsService = pointsViewModel.pointsService
    val defaultPoint by pointsService.defaultPoint.asState()

    val pointSize = point.getSize(defaultPoint)

    when (val action = point.action) {

        is Action.LaunchApp -> {
            val app by drawerViewModel.findOne(action.packageName, action.profile.userHandle).collectAsState(null)
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
    iconsViewModel: IconsViewModel = activityViewModel(),
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
    iconsViewModel: IconsViewModel = activityViewModel(),
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

/**
 * Pre-renders point action icons to [ImageBitmap] for efficient Canvas drawing.
 *
 * Runs inside [LaunchedEffect] keyed on [points] / [defaultPoint] and re-renders
 * whenever the working set or fallback size changes. The returned map is memoised
 * via [mutableStateMapOf] so that [DrawScope.PointBg] can access it reactively.
 */
@Composable
fun rememberPointIconBitmaps(
    iconsViewModel: IconsViewModel = activityViewModel(),
    pointsViewModel: PointsViewModel = activityViewModel(),
): Map<Int, ImageBitmap> {
    val density = LocalDensity.current
    val colorScheme = MaterialTheme.colorScheme

    val pointsService = pointsViewModel.pointsService
    val defaultPoint by pointsService.defaultPoint.asState()
    val points by pointsService.points.asState()

    val renderSettings = remember(defaultPoint) {
        LauncherIconRenderSettings(
            size = with(density) { defaultPoint.getSize(defaultPoint).toPx() }.toInt().coerceAtLeast(8),
            fgThemeColor = colorScheme.onPrimaryContainer.toArgb(),
            bgThemeColor = colorScheme.primaryContainer.toArgb(),
            fgTone = 90,
            bgTone = 30,
        )
    }

    val icons = remember { mutableStateMapOf<Int, ImageBitmap>() }

    LaunchedEffect(points, defaultPoint, renderSettings) {
        icons.clear()
        val batch = mutableMapOf<Int, ImageBitmap>()
        for (point in points) {
            if (icons.containsKey(point.id)) continue
            val bitmap = loadPointIconBitmap(point, iconsViewModel, renderSettings) ?: continue
            batch[point.id] = bitmap
        }
        icons.putAll(batch)
    }

    return icons
}

private suspend fun loadPointIconBitmap(
    point: Point,
    iconsViewModel: IconsViewModel,
    settings: LauncherIconRenderSettings,
): ImageBitmap? {
    val launcherIcon: LauncherIcon = iconsViewModel.getPointIconOnce(point) ?: return null

    val staticIcon: StaticLauncherIcon =
        if (launcherIcon is DynamicLauncherIcon) {
            withContext(Dispatchers.Default) { launcherIcon.getIcon(System.currentTimeMillis()) }
        } else {
            launcherIcon as? StaticLauncherIcon ?: return null
        }

    val bitmap: Bitmap = withContext(Dispatchers.Default) { staticIcon.render(settings) }
    return bitmap.asImageBitmap()
}
