package org.elnix.dragonlauncher.ui.helpers.nests.cache

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.util.fastRoundToInt
import io.github.elnix90.logging.POINTS_TAG
import io.github.elnix90.logging.logD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.icons.DynamicLauncherIcon
import org.elnix.dragonlauncher.base.icons.LauncherIconRenderSettings
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.models.IconsViewModel
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.composition.LocalNestDebugOverlay

/**
 * Observes `points] and keeps [PointStableCache] synchronised with their
 * current configuration.
 *
 * Each point is processed in an isolated [key]ed sub-composition so that
 * a change to one point never invalidates the cached values of others.
 */
@Composable
fun RememberPointStableCaches(
    textMeasurer: TextMeasurer,
    pointsViewModel: PointsViewModel = activityViewModel(),
    iconsViewModel: IconsViewModel = activityViewModel()
) {
    val points by pointsViewModel.pointsService.points.asState()
    val defaultPoint by pointsViewModel.pointsService.defaultPoint.asState()

    LaunchedEffect(points.size) {
        PointStableCache.updateMaxCacheSize(points.size)
    }

    for (point in points) {
        RememberPointStableCacheEntry(
            textMeasurer = textMeasurer,
            point = point,
            defaultPoint = defaultPoint,
            iconsViewModel = iconsViewModel
        )
    }
}

/**
 * Per-point composable that computes [StablePointValues] and writes them
 * into [PointStableCache] when any dependency changes.
 */
@Composable
private fun RememberPointStableCacheEntry(
    textMeasurer: TextMeasurer,
    point: Point,
    defaultPoint: Point,
    iconsViewModel: IconsViewModel
) {
    val density = LocalDensity.current
    val colorScheme = MaterialTheme.colorScheme

    val systemInDarkTheme = isSystemInDarkTheme()
    val renderSettings = remember(defaultPoint) {
        LauncherIconRenderSettings(
            size = with(density) { defaultPoint.getSize(defaultPoint).toPx() }.toInt().coerceAtLeast(8) * 2,
            fgThemeColor = colorScheme.onPrimaryContainer.toArgb(),
            bgThemeColor = colorScheme.primaryContainer.toArgb(),
            fgTone = if (systemInDarkTheme) 90 else 10,
            bgTone = if (systemInDarkTheme) 30 else 90,
        )
    }

    val sizePx: Float = remember(point.size, defaultPoint) {
        with(density) { point.getSize(defaultPoint).toPx() }
    }
    val innerPaddingPx = remember(point.innerPadding, defaultPoint) {
        with(density) { point.getInnerPadding(defaultPoint).toPx() }
    }
    val borderRadii = remember(sizePx, innerPaddingPx) {
        (sizePx / 2 + innerPaddingPx).coerceAtLeast(0f)
    }


    val imageBitmap by loadPointIconBitmap(point, iconsViewModel, renderSettings)

    val labelSmall = MaterialTheme.typography.labelSmall
    val extraColors = LocalExtraColors.current
    val textStyle = remember(labelSmall, extraColors) {
        labelSmall.copy(color = extraColors.circle)
    }

    val nestDebugOverlay = LocalNestDebugOverlay.current
    val drawScopeText: DrawScopeText? = remember(nestDebugOverlay, textMeasurer, point.offset, textStyle) {
        if (nestDebugOverlay) {
            val pointOffset = point.offset
            val x = pointOffset.x.fastRoundToInt()
            val y = pointOffset.y.fastRoundToInt()

            val text = "$x ; $y"

            val textLayoutResult = textMeasurer.measure(
                text = AnnotatedString(text),
                constraints = Constraints(maxWidth = Int.MAX_VALUE),
                style = textStyle
            )

            val textWidth = textLayoutResult.size.width
            val textHeight = textLayoutResult.size.height

            val topLeft = Offset(
                x = textWidth / 2f,
                y = textHeight + sizePx
            )

            DrawScopeText(
                textLayoutResult = textLayoutResult,
                topLeft = topLeft
            )
        } else null
    }
    LaunchedEffect(imageBitmap) {
        logD(POINTS_TAG) { "img bmp: $imageBitmap" }
    }

    LaunchedEffect(
        point.id,
        sizePx,
        innerPaddingPx,
        borderRadii,
        renderSettings,
        imageBitmap,
        drawScopeText
    ) {
        PointStableCache.compute(point.id) {
            StablePointValues(
                sizePx = sizePx.coerceAtLeast(1f),
                innerPaddingPx = innerPaddingPx,
                borderRadii = borderRadii,
                iconSize = Size(borderRadii * 2f, borderRadii * 2f),
                imageBitmap = imageBitmap,
                drawScopeText = drawScopeText
            )
        }
    }
}

@Composable
private fun loadPointIconBitmap(
    point: Point,
    iconsViewModel: IconsViewModel,
    settings: LauncherIconRenderSettings,
): State<ImageBitmap?> {
    return produceState(initialValue = null, point.id, iconsViewModel, settings) {
        val staticIcon = when (val launcherIcon = iconsViewModel.getIcon(point).first()) {
            is DynamicLauncherIcon -> withContext(Dispatchers.Default) {
                launcherIcon.getIcon(System.currentTimeMillis())
            }
            is StaticLauncherIcon -> launcherIcon
            null -> null
        }
        value = staticIcon?.render(settings)?.asImageBitmap()
    }
}