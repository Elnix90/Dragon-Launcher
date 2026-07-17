package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.elnix.dragonlauncher.base.cache.NestIntersectionShapesPathCache
import org.elnix.dragonlauncher.base.cache.PointStableCache
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel

@Composable
public fun CacheDebugOverlay(pointsViewModel: PointsViewModel = activityViewModel()) {
    val pointsService = pointsViewModel.pointsService
    val points by pointsService.points.collectAsState()
    val nests by pointsService.nests.collectAsState()
    val pointStableCacheSize = PointStableCache.size
    val nestIntersectionShapesPathCacheSize = NestIntersectionShapesPathCache.size

    DebugZone(DebugSettingsStore.cachesDebugOverlay) {
        Text("Points size: ${points.size}")
        Text("Nests size: ${nests.size}")
        Text("PointStableCache size: $pointStableCacheSize")
        Text("NestIntersectionShapesPathCache size: $nestIntersectionShapesPathCacheSize")
    }
}