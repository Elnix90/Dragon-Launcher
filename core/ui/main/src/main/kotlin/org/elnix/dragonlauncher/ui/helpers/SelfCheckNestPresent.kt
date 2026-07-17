package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.elnix90.logging.NESTS_TAG
import io.github.elnix90.logging.logD
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel

/**
 * Used to ensure that there is always the requested [nest][org.elnix.dragonlauncher.base.model.serializables.Nest]
 *
 * Fires at every nest updates from the [PointService][org.elnix.dragonlauncher.points.PointsService] and checks whether if a nest with the [id][org.elnix.dragonlauncher.base.model.serializables.Nest.id] `0` exists
 */
@Composable
public fun SelfCheckNestPresent(
    pointsViewModel: PointsViewModel = activityViewModel()
) {
    val pointsService = pointsViewModel.pointsService

    val nestId by pointsViewModel.nestsNavigationService.currentNestId.collectAsState()
    val nests by pointsService.nests.collectAsState()

    LaunchedEffect(Unit, nestId, nests.size) {
        if (nests[nestId] == null) {
            logD(NESTS_TAG) { "Creating missing nest $nestId" }
            pointsService.addNest(nestId)
        }
    }
}