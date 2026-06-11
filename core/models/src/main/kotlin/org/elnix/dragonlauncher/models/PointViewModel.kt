package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.recents.PointsService
import javax.inject.Inject

/**
 * Point view model, responsible for holding different values related to the point settings screen
 *
 * it exposes the points, the nests and the default point for the whole app
 */
@HiltViewModel
class PointViewModel @Inject constructor(
    application: Application,
    val pointsService: PointsService
) : AndroidViewModel(application) {

    val defaultPoint = pointsService.defaultPoint.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        Point.defaultSwipePointsValues
    )

    val points = pointsService.points.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptySet()
    )
    val nests = pointsService.nests.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptySet()
    )

    init {
        viewModelInitialized()
    }
}
