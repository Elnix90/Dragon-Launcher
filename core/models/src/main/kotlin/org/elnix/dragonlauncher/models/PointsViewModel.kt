package org.elnix.dragonlauncher.models

import android.app.Application
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.service.autofill.Validators.and
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.elnix90.logging.logD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.cache.DrawScopeText
import org.elnix.dragonlauncher.base.cache.NestIntersectionShapesPathCache
import org.elnix.dragonlauncher.base.cache.PointStableCache
import org.elnix.dragonlauncher.base.cache.StablePointValues
import org.elnix.dragonlauncher.base.icons.DynamicLauncherIcon
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.LauncherIconRenderSettings
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.colors.ColorService
import org.elnix.dragonlauncher.icons.IconService
import org.elnix.dragonlauncher.ktx.toPath
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.points.NestsNavigationService
import org.elnix.dragonlauncher.points.PointsService
import javax.inject.Inject

/**
 * Point view model, responsible for holding different values related to the points
 *
 * it exposes the [PointsService] to let the UI access it and the [NestsNavigationService] to access the current nest and other navigation useful stuff
 */
@HiltViewModel
public class PointsViewModel @Inject constructor(
    application: Application,
    private val colorService: ColorService,
    private val iconService: IconService,
    public val pointsService: PointsService,
    public val nestsNavigationService: NestsNavigationService
) : AndroidViewModel(application) {

    private val density: Density = Density(application.resources.displayMetrics.density)


    private val pointsTextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )

    private val fontFamilyResolver = FontFamily.Resolver { fontFamily ->
        // Return a Typeface for the given fontFamily
        // Example: Load a font from assets
        Typeface.createFromAsset(assets, "fonts/my_font.ttf")
    }

    private val textMeasurer = TextMeasurer(
        defaultFontFamilyResolver = FontFamily.Monospace,
        defaultDensity = density,
        defaultLayoutDirection = LayoutDirection.Ltr
    )

    private fun isSystemDarkModeEnabled(context: Context): Boolean {
        return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    init {

        // Points cache computing
        viewModelScope.launch {
            pointsService.points.collectLatest { points ->
                PointStableCache.updateMaxCacheSize(points.size)
            }
        }

        // Nests cache computing
        viewModelScope.launch {
            pointsService.nests.collectLatest { nests ->
                val uniqueShapes = nests.values.flatMap { it.intersectionShapes }
                NestIntersectionShapesPathCache.updateMaxCacheSize(uniqueShapes.size)

                for (shape in uniqueShapes) {
                    NestIntersectionShapesPathCache.compute(shape) {
                        shape.shape.resolveShape().toPath(shape.getSize(density.density), density)
                    }
                }
            }
        }

        viewModelInitialized()
    }


    /**
     * Precomputes [StablePointValues] for all points whenever their data or icons change.
     * Only recomputes values that actually changed, not the entire cache.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun precomputePointCaches() {
        pointsService.points
            .flatMapLatest { points ->
                // For each point, combine its data with its icon
                if (points.isEmpty()) {
                    return@flatMapLatest flowOf(emptyMap<Int, PrecomputeData>())
                }

                val iconFlows = points.values.associate { point ->
                    point.id to iconService.getPointIcon(point).distinctUntilChanged()
                }

                val flows = pointsService.defaultPoint.flow as Flow<Point> +
                        flowOf(true) +
                        iconFlows.values.toList()

                combine(
                    pointsService.defaultPoint.flow,
                    flowOf(true),
                    iconFlows.values
                ) {
                    @Suppress("UNCHECKED_CAST")
                    val defaultPoint = it[0] as Point
                    val systemTheme = it[1] as SystemTheme // or whatever your theme type is
                    val iconValues = it.drop(2)

                    // Reconstruct map of point.id -> (Point, Icon)
                    points.values.zip(iconValues).associate { (point, icon) ->
                        point.id to PrecomputeData(
                            point = point,
                            defaultPoint = defaultPoint,
                            icon = icon as? LauncherIcon,
                            darkTheme = systemTheme
                        )
                    }
                }
            }
            .collect { dataMap ->
                withContext(Dispatchers.Default) {
                    dataMap.forEach { (pointId, data) ->
                        computePointCacheEntry(pointId, data)
                    }
                }
            }
    }

    private suspend fun computePointCacheEntry(pointId: Int, data: PrecomputeData) {
        val (point, defaultPoint, icon, systemTheme) = data

        val sizePx = with(density) { point.getSize(defaultPoint).toPx() }
        val innerPaddingPx = with(density) { point.getInnerPadding(defaultPoint).toPx() }
        val borderRadii = (sizePx / 2 + innerPaddingPx).coerceAtLeast(0f)

        // Render icon
        val imageBitmap = when (icon) {
            is DynamicLauncherIcon -> {
                val staticIcon = icon.getIcon(System.currentTimeMillis())
                val settings = LauncherIconRenderSettings(
                    size = (defaultPoint.size ?: Point.defaultSize).coerceAtLeast(8) * 2,
                    fgThemeColor = systemTheme.colorScheme.onPrimaryContainer.toArgb(),
                    bgThemeColor = systemTheme.colorScheme.primaryContainer.toArgb(),
                    fgTone = if (systemTheme.isDarkTheme) 90 else 10,
                    bgTone = if (systemTheme.isDarkTheme) 30 else 90,
                )
                staticIcon?.render(settings)?.asImageBitmap()
            }

            is StaticLauncherIcon -> {
                val settings = LauncherIconRenderSettings(
                    size = (defaultPoint.size ?: Point.defaultSize).toInt().coerceAtLeast(8) * 2,
                    fgThemeColor = systemTheme.colorScheme.onPrimaryContainer.toArgb(),
                    bgThemeColor = systemTheme.colorScheme.primaryContainer.toArgb(),
                    fgTone = if (systemTheme.isDarkTheme) 90 else 10,
                    bgTone = if (systemTheme.isDarkTheme) 30 else 90,
                )
                icon.render(settings)?.asImageBitmap()
            }

            null -> null
        }

        // Compute custom texts (you may need to adapt this — it originally used LocalDensity)
        val customTexts = computeDrawScopeText(point, sizePx, defaultPoint)

        val stableValues = StablePointValues(
            sizePx = sizePx.coerceAtLeast(1f),
            innerPaddingPx = innerPaddingPx,
            borderRadii = borderRadii,
            iconSize = Size(borderRadii * 2f, borderRadii * 2f),
            imageBitmap = imageBitmap,
            customTexts = customTexts
        )

        PointStableCache.compute(pointId) { stableValues }
        logD { "Precomputed cache for point $pointId" }
    }

    private fun computeDrawScopeText(point: Point, sizePx: Float, defaultPoint: Point): List<DrawScopeText> {
        // This was originally rememberDrawScopeText() in Compose.
        // Extract its logic here, or provide a non-Compose helper function.
        // For now, return an empty list as placeholder:
        return emptyList()
    }

    private data class PrecomputeData(
        val point: Point,
        val defaultPoint: Point,
        val icon: LauncherIcon?,
        val darkTheme: Boolean
    )
}
