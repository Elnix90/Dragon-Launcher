package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Density
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import org.elnix.dragonlauncher.settings.stores.map.IconsSettingsStore
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * ViewModel responsible for maintaining [PointStableCache] entries for all points
 * and keeping [NestIntersectionShapesPathCache] synchronised with the current nests.
 *
 * ### Cache strategy
 *
 * Each point is tracked by an independent coroutine that combines:
 * - The point's own data (via [PointsService.recomposeTrigger] change detection)
 * - [PointsService.defaultPoint] configuration
 * - [ColorService.colors] for theme-aware icon rendering
 * - Per-point icon from [IconService.getPointIcon]
 *
 * When any dependency changes, only the affected cache entry is recomputed on
 * [Dispatchers.Default]. Entries for removed points are evicted immediately.
 *
 * The UI layer may optionally override `customTexts` at draw time for debug overlays;
 * the ViewModel cache intentionally leaves it as `null` since text measurement is
 * inherently Compose-dependent.
 *
 * Exposes [PointsService] and [NestsNavigationService] for direct UI consumption.
 */
@OptIn(ExperimentalAtomicApi::class)
@HiltViewModel
public class PointsViewModel @Inject constructor(
    application: Application,
    private val colorService: ColorService,
    private val iconService: IconService,
    public val pointsService: PointsService,
    public val nestsNavigationService: NestsNavigationService
) : AndroidViewModel(application) {

    private val density: Density = Density(application.resources.displayMetrics.density)

    private var isInitPhase = true

    init {
        viewModelScope.launch(Dispatchers.Default) {
            pointsService.recomposeTrigger.flow
                .collect {
                    if (isInitPhase) {
                        isInitPhase = false
                    } else {

                        val nests = pointsService.nests.value

                        val uniqueShapes = nests.values.flatMap { it.getInterSectionShapes(pointsService.defaultNest.value) }
                        NestIntersectionShapesPathCache.updateMaxCacheSize(uniqueShapes.size)

                        for (shape in uniqueShapes) {
                            NestIntersectionShapesPathCache.compute(shape) {
                                shape.getShape(pointsService.defaultIntersectionShape.value)
                                    .resolveShape()
                                    .toPath(
                                        shape.getSize(density.density, pointsService.defaultIntersectionShape.value),
                                        density
                                    )
                            }
                        }

                        val points = pointsService.points.value

                        PointStableCache.updateMaxCacheSize(points.size)
                        synchronizePointTracking(points)
                    }
                }
        }

        viewModelInitialized()
    }

    /**
     * Last known snapshot of each point, used to detect data changes via
     * structural equality without requiring the [PointsService.points] StateFlow
     * to emit on in-place mutations.
     */
    private val lastKnownPoints by lazy { ConcurrentHashMap<Int, Point>() }


    /** Per-point cache observation jobs, keyed by point id. */
    private val pointTrackingJobs by lazy { ConcurrentHashMap<Int, Job>() }


    /**
     * Compares the current points with [lastKnownPoints] and starts, restarts, or
     * cancels per-point cache observation as needed.
     *
     * Uses structural equality on [Point] (a data class) to detect additions,
     * edits, and removals without relying on [org.elnix.dragonlauncher.base.SettingFlow]
     * emission semantics.
     *
     * @param points the current points map read from [PointsService.points]
     */
    private fun synchronizePointTracking(points: Map<Int, Point>?) {
        if (points == null) return

        val currentIds = points.keys

        // Cancel and evict tracking for points that no longer exist
        val iterator = pointTrackingJobs.iterator()
        while (iterator.hasNext()) {
            val (id, job) = iterator.next()
            if (id !in currentIds) {
                job.cancel()
                iterator.remove()
                lastKnownPoints.remove(id)
                PointStableCache.evict(id)
            }
        }

        // Start or restart tracking for each current point whose data changed
        for ((id, point) in points) {
            val lastKnown = lastKnownPoints[id]
            if (lastKnown == point) continue

            lastKnownPoints[id] = point
            pointTrackingJobs[id]?.cancel()
            pointTrackingJobs[id] = viewModelScope.launch {
                observePoint(id)
            }
        }
    }

    /**
     * Observes a single point's dependencies and keeps [PointStableCache] up-to-date.
     *
     * The outer flow uses [PointsService.recomposeTrigger] to detect point-data changes
     * (since [PointsService.points] does not emit on in-place mutations), combined with
     * [distinctUntilChanged] to skip unchanged points. The inner [combine] tracks
     * default configuration, color scheme, and per-point icon changes.
     *
     * Only [StablePointValues.customTexts] is left as `null`; the Compose layer
     * supplies debug overlay text as a draw-time override when needed.
     *
     * @param pointId the id of the point to observe
     */
    private suspend fun observePoint(pointId: Int) {
        pointsService.recomposeTrigger.flow
            .mapNotNull { pointsService.findPointById(pointId) }
            .distinctUntilChanged()
            .collectLatest { currentPoint ->
                combine(
                    pointsService.defaultPoint.flow,
                    iconService.getPointIcon(currentPoint).distinctUntilChanged(),
                    IconsSettingsStore.renderForeground.flow(application),
                    IconsSettingsStore.renderBackground.flow(application)
                ) { defaultPoint, icon, renderForeground, renderBackground ->
                    computeStableValues(currentPoint, defaultPoint, icon, renderForeground, renderBackground)
                }.collect { values ->
                    PointStableCache.compute(pointId) { values }
                }
            }
    }

    /**
     * Computes [StablePointValues] for a single point on [Dispatchers.Default].
     *
     * @param point the point to compute values for
     * @param defaultPoint the default point configuration used for fallback sizes
     * @param icon the resolved [LauncherIcon] for this point, or null
     * @return the computed stable values ready for caching
     */
    private suspend fun computeStableValues(
        point: Point,
        defaultPoint: Point,
        icon: LauncherIcon?,
        renderForeground: Boolean,
        renderBackground: Boolean,
    ): StablePointValues = withContext(Dispatchers.Default) {
        val sizePx = with(density) { point.getSize(defaultPoint).toPx() }
        val innerPaddingPx = with(density) { point.getInnerPadding(defaultPoint).toPx() }
        val borderRadii = (sizePx / 2 + innerPaddingPx).coerceAtLeast(0f)

        val imageBitmap = renderPointIcon(icon, point, defaultPoint, renderForeground, renderBackground)

        StablePointValues(
            sizePx = sizePx.coerceAtLeast(1f),
            innerPaddingPx = innerPaddingPx,
            borderRadii = borderRadii,
            iconSize = Size(borderRadii * 2f, borderRadii * 2f),
            imageBitmap = imageBitmap,
            customTexts = null
        )
    }

        /**
         * Renders a [LauncherIcon] into an [ImageBitmap]
         * using the current theme colors.
         *
         * Delegates to [StaticLauncherIcon.render] or resolves [DynamicLauncherIcon]
         * to its current frame before rendering.
         *
         * @param icon the launcher icon to render, or null
         * @param defaultPoint the default point configuration (used for icon size)
         * @return the rendered bitmap, or null if [icon] is null
         */
        private suspend fun renderPointIcon(
            icon: LauncherIcon?,
            point: Point,
            defaultPoint: Point,
            renderForeground: Boolean,
            renderBackground: Boolean,
        ): ImageBitmap? {
            val renderSettings = LauncherIconRenderSettings(
                size = (point.getSize(defaultPoint).value * density.density).toInt() * 2,
                renderForeground = renderForeground,
                renderBackground = renderBackground
            )

            return when (icon) {
                is DynamicLauncherIcon -> {
                    val staticIcon = icon.getIcon(System.currentTimeMillis())
                    staticIcon.render(renderSettings).asImageBitmap()
                }

                is StaticLauncherIcon -> icon.render(renderSettings).asImageBitmap()

                null -> null
            }
        }

    override fun onCleared() {
        pointTrackingJobs.values.forEach { it.cancel() }
        pointTrackingJobs.clear()
        lastKnownPoints.clear()
    }
}
