package org.elnix.dragonlauncher.ui.actions

import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.DragonCache
import org.elnix.dragonlauncher.base.icons.DynamicLauncherIcon
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.LauncherIconRenderSettings
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.models.IconsViewModel
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState

@Composable
fun PointsIconsUpdater(
    iconsViewModel: IconsViewModel = activityViewModel(),
    pointsViewModel: PointsViewModel = activityViewModel(),
) {
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
            bgTone = 30
        )
    }

    LaunchedEffect(points.toSortedSet(), defaultPoint, renderSettings) {
        for (point in points) {
            val imageBitmap = loadPointIconBitmap(point, iconsViewModel, renderSettings) ?: continue
            BitmapPointIconsCache.compute(point.id) { imageBitmap }
        }
    }
}


object BitmapPointIconsCache: DragonCache<Int, ImageBitmap>(200)

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
