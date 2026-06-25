package org.elnix.dragonlauncher.ui.remembers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.cache.DrawPathCache
import org.elnix.dragonlauncher.base.model.models.SwipeDrawParams
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.models.IconsViewModel
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.SwipeMapSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel


@Composable
fun rememberSwipeDefaultParams(
    iconsViewModel: IconsViewModel = activityViewModel(),
    pointsViewModel: PointsViewModel = activityViewModel(),
    backgroundColor: Color? = null,
    defaultPointSerializable: Point? = null,
    forceShowAllActionsInCurrentNest: Boolean? = null,
    allowShowIconInCenter: Boolean = false
): MutableState<SwipeDrawParams> {
    val ctx = LocalContext.current
    val density = LocalDensity.current

    val points by pointsViewModel.points.collectAsState()
    val nests by pointsViewModel.nests.collectAsState()
    val defaultPointSettings by pointsViewModel.defaultPoint.collectAsState()

    val iconShape by DrawerSettingsStore.iconShape.asState()
    val extraColors = LocalExtraColors.current

    val surfaceColorDraw = backgroundColor ?: Color.Unspecified

    val defaultPoint by remember(defaultPointSerializable, defaultPointSettings) {
        mutableStateOf(
            defaultPointSerializable ?: defaultPointSettings
        )
    }

    val maxNestsDepth by UiSettingsStore.maxNestsDepth.asState()

    val subNestDefaultRadius by SwipeMapSettingsStore.subNestDefaultRadius.asState()
    val subNestDefaultRadiusPixels by remember(subNestDefaultRadius) {
        derivedStateOf { subNestDefaultRadius.dp.value * density.density }
    }

    LaunchedEffect(points.size) {
        DrawPathCache.updateMaxCacheSize(points.size)
    }

    val showAppLaunchPreview by UiSettingsStore.showAppLaunchingPreview.asState()
    val showAppCirclePreview by UiSettingsStore.showCirclePreview.asState()
    val showAllActionsOnCurrentCircle by UiSettingsStore.showAllActionsOnCurrentCircle.asState()
    val showAllActionsInCurrentNestSetting by UiSettingsStore.showAllActionsOnCurrentNest.asState()
    val showAppPreviewIconCenterStartPosition by UiSettingsStore.showAppPreviewIconCenterStartPosition.asState()

    val effectiveShowAppPreviewIconCenterStartPosition = allowShowIconInCenter && showAppPreviewIconCenterStartPosition
    val showAllActionsInCurrentNest = forceShowAllActionsInCurrentNest ?: showAllActionsInCurrentNestSetting

    return remember(
        backgroundColor,
        points,
        nests,
        defaultPointSerializable,
        ctx,
        defaultPointSettings,
        iconShape,
        extraColors,
        surfaceColorDraw,
        defaultPoint,
        maxNestsDepth,
        subNestDefaultRadius,
        showAppLaunchPreview,
        showAppCirclePreview,
        showAllActionsOnCurrentCircle,
        showAllActionsInCurrentNest,
        effectiveShowAppPreviewIconCenterStartPosition
    ) {
        mutableStateOf(
            SwipeDrawParams(
                nests = nests,
                points = points,
                ctx = ctx,
                defaultPoint = defaultPoint,
                surfaceColorDraw = surfaceColorDraw,
                extraColors = extraColors,
                maxDepth = maxNestsDepth,
                iconShape = iconShape,
                subNestDefaultRadius = subNestDefaultRadiusPixels,
                showAppCirclePreview = showAppCirclePreview,
                showAppLaunchPreview = showAppLaunchPreview,
                showAllActionsOnCurrentCircle = showAllActionsOnCurrentCircle,
                showAllActionsOnCurrentNest = showAllActionsInCurrentNest,
                showAppPreviewIconCenterStartPosition = effectiveShowAppPreviewIconCenterStartPosition,
                computeIcon = { iconsViewModel.reloadIcon(it) }
            )
        )
    }
}