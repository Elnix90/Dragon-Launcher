package org.elnix.dragonlauncher.ui.remembers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.common.serializables.CircleNest
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable
import org.elnix.dragonlauncher.settings.stores.SwipeMapSettingsStore
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.cache.DrawPathCache
import org.elnix.dragonlauncher.ui.base.cache.SwipeDrawParams
import org.elnix.dragonlauncher.ui.composition.LocalAppsViewModel
import org.elnix.dragonlauncher.ui.composition.LocalDefaultPoint
import org.elnix.dragonlauncher.ui.composition.LocalIconShape
import org.elnix.dragonlauncher.ui.composition.LocalNests
import org.elnix.dragonlauncher.ui.composition.LocalPointIconsCache
import org.elnix.dragonlauncher.ui.composition.LocalPoints


@Composable
fun rememberSwipeDefaultParams(
    backgroundColor: Color? = null,
    nests: List<CircleNest>? = null,
    defaultPointSerializable: SwipePointSerializable? = null,
    forceShowAllActionsInCurrentNest: Boolean? = null,
    allowShowIconInCenter: Boolean = false
): SwipeDrawParams {
    val ctx = LocalContext.current
    val density = LocalDensity.current

    val appsViewModel = LocalAppsViewModel.current

    val points = LocalPoints.current
    val defaultPointSettings = LocalDefaultPoint.current
    val nests = nests ?: LocalNests.current
    val icons = LocalPointIconsCache.current
    val iconShape = LocalIconShape.current
    val extraColors = LocalExtraColors.current

    val surfaceColorDraw = backgroundColor ?: Color.Unspecified

    val defaultPoint = defaultPointSerializable ?: defaultPointSettings

    val maxNestsDepth by UiSettingsStore.maxNestsDepth.asState()

    val subNestDefaultRadius by SwipeMapSettingsStore.subNestDefaultRadius.asState()
    val subNestDefaultRadiusPixels by remember(subNestDefaultRadius) {
        derivedStateOf { subNestDefaultRadius.dp.value * density.density }
    }

    val drawPathCache = remember { DrawPathCache(points.size) }

    LaunchedEffect(points.size) {
        drawPathCache.updateMaxCacheSize(points.size)
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
        icons,
        defaultPointSerializable,
        ctx,
        defaultPointSettings,
        iconShape,
        extraColors,
        surfaceColorDraw,
        defaultPoint,
        maxNestsDepth,
        subNestDefaultRadius,
        drawPathCache,
        showAppLaunchPreview,
        showAppCirclePreview,
        showAllActionsOnCurrentCircle,
        showAllActionsInCurrentNest,
        effectiveShowAppPreviewIconCenterStartPosition
    ) {
        SwipeDrawParams(
            nests = nests,
            points = points,
            ctx = ctx,
            defaultPoint = defaultPoint,
            icons = icons,
            surfaceColorDraw = surfaceColorDraw,
            extraColors = extraColors,
            maxDepth = maxNestsDepth,
            iconShape = iconShape,
            subNestDefaultRadius = subNestDefaultRadiusPixels,
            drawPathCache = drawPathCache,
            showAppCirclePreview = showAppCirclePreview,
            showAppLaunchPreview = showAppLaunchPreview,
            showAllActionsOnCurrentCircle = showAllActionsOnCurrentCircle,
            showAllActionsOnCurrentNest  = showAllActionsInCurrentNest,
            showAppPreviewIconCenterStartPosition = effectiveShowAppPreviewIconCenterStartPosition,
            computeIcon = {
                appsViewModel.reloadPointIcon(it)
            }
        )
    }
}
