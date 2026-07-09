package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.elnix90.logging.NESTS_TAG
import io.github.elnix90.logging.logD
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState

@Composable
public fun SelfCheckNestPresent(
    pointsViewModel: PointsViewModel = activityViewModel()
) {
    val pointsService = pointsViewModel.pointsService

    val nestId by pointsViewModel.currentNestId.collectAsState()
    val nests by pointsService.nests.asState()
    /**
     * Used to ensure that there is always the requested nest
     */
    LaunchedEffect(Unit, nestId, nests.size) {
        if (nests.none { it.id == nestId }) {
            logD(NESTS_TAG) { "Creating missing nest $nestId" }
            pointsService.addNest(nestId)
        }
    }
}