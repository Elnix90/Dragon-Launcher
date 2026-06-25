package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.elnix.dragonlauncher.base.model.serializables.Nest
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
class PointsViewModel @Inject constructor(
    application: Application,
    private val pointsService: PointsService
) : AndroidViewModel(application) {

    val defaultPoint: StateFlow<Point> = pointsService.defaultPoint.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = Point.defaultSwipePointsValues
    )

    val points: StateFlow<Set<Point>> = pointsService.points.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    val nests: StateFlow<Set<Nest>> = pointsService.nests.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    val selectedPoint = pointsService.selectedPoint
    fun select(point: Point?) = pointsService.select(point)

    val undoRedo = pointsService.undoRedo



    fun addPoint(newPoint: Point) = pointsService.addPoint(newPoint)
    fun removePoint(id: String): Boolean = pointsService.removePoint(id)
    fun editPoint(id: String, editedPoint: (Point) -> Point): Boolean = pointsService.editPoint(id, editedPoint)

    fun deleteNest(id: Int): Boolean = pointsService.deleteNest(id)
    fun addNest(nestId: Int = 0) = pointsService.addNest(nestId)
    fun editNest(id: Int, editedNest: (Nest) -> Nest): Boolean = pointsService.editNest(id, editedNest)

    fun persist() = pointsService.persist()

    /** Set the given [points], [nests] and [defaultPoint] if not null. */
    fun set(
        points: Set<Point>? = null,
        nests: Set<Nest>? = null,
        defaultPoint: Point? = null
    ) = pointsService.set(points, nests, defaultPoint)

    fun reset(
        resetPoints: Boolean = false,
        resetNests: Boolean = false,
        resetDefaultPoint: Boolean = false
    ) = pointsService.reset(resetPoints, resetNests, resetDefaultPoint)

    init {
        viewModelInitialized()
    }
}
