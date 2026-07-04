package org.elnix.dragonlauncher.ui.helpers.swipe.cache.points

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.helpers.customobjects.shapeToPath
import org.elnix.dragonlauncher.ui.helpers.swipe.cache.nests.PointStableCache
import org.elnix.dragonlauncher.ui.helpers.swipe.cache.nests.StablePointValues

/**
 * Observes `points] and keeps [PointStableCache] synchronised with their
 * current configuration.
 *
 * Each point is processed in an isolated [key]ed sub-composition so that
 * a change to one point never invalidates the cached values of others.
 */
@Composable
fun RememberNestsStableCaches(
    pointsViewModel: PointsViewModel = activityViewModel(),
) {
    val nests by pointsViewModel.pointsService.nests.asState()

    val uniqueShapes by remember(nests) {
        derivedStateOf {
            nests.flatMap { it.intersectionShapes }.toSet()
        }
    }

    LaunchedEffect(uniqueShapes.size) {
        PointStableCache.updateMaxCacheSize(uniqueShapes.size)
    }

    for (shape in uniqueShapes) {
        RememberShapePath(shape)
    }
}

/**
 * Per-point composable that computes [StablePointValues] and writes them
 * into [PointStableCache] when any dependency changes.
 */
@Composable
private fun RememberShapePath(
    shape: IntersectionShape
) {
    val density = LocalDensity.current
    val resolvedShape = remember(shape.shape) {
        shape.shape.resolveShape()
    }

    val resolvedSizePx = remember(shape.size) {
        shape.size * density.density
    }
    val resolvedSize = remember(shape.size) {
        Size(resolvedSizePx, resolvedSizePx)
    }

    LaunchedEffect(
        shape,
        resolvedShape,
        resolvedSize
    ) {
        NestIntersectionShapesPathCache.compute(shape) {
            shapeToPath(resolvedShape, resolvedSize, density)
        }
    }
}


