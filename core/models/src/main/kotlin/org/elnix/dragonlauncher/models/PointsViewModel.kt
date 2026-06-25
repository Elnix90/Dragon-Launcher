package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.undoredo.UndoRedoManager
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.recents.PointsService
import javax.inject.Inject

/**
 * Point view model, responsible for holding different values related to the point settings screen
 *
 * it exposes the points, the nests and the default point for the whole app
 */
@HiltViewModel
public class PointsViewModel @Inject constructor(
    application: Application,
    private val pointsService: PointsService
) : AndroidViewModel(application) {

    public val defaultPoint: StateFlow<Point> = pointsService.defaultPoint.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = Point.defaultSwipePointsValues
    )

    public val points: StateFlow<Set<Point>> = pointsService.points.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    public val nests: StateFlow<Set<Nest>> = pointsService.nests.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    public val selectedPoint: SettingFlow<Point?> = pointsService.selectedPoint
    public fun select(point: Point?): Unit = pointsService.select(point)

    public val undoRedo: UndoRedoManager = pointsService.undoRedo



    public fun addPoint(newPoint: Point): Unit = pointsService.addPoint(newPoint)
    public fun removePoint(id: String): Boolean = pointsService.removePoint(id)
    public fun editPoint(id: String, editedPoint: (Point) -> Point): Boolean = pointsService.editPoint(id, editedPoint)

    public fun deleteNest(id: Int): Boolean = pointsService.deleteNest(id)
    public fun addNest(nestId: Int = 0): Int = pointsService.addNest(nestId)
    public fun editNest(id: Int, editedNest: (Nest) -> Nest): Boolean = pointsService.editNest(id, editedNest)

    public fun persist(): Unit = pointsService.persist()

    /** Set the given [points], [nests] and [defaultPoint] if not null. */
    public fun set(
        points: Set<Point>? = null,
        nests: Set<Nest>? = null,
        defaultPoint: Point? = null
    ): Unit = pointsService.set(points, nests, defaultPoint)

    public fun reset(
        resetPoints: Boolean = false,
        resetNests: Boolean = false,
        resetDefaultPoint: Boolean = false
    ): Unit = pointsService.reset(resetPoints, resetNests, resetDefaultPoint)

    init {
        viewModelInitialized()
    }
}
