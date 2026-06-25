package org.elnix.dragonlauncher.recents

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Nest.Companion.NestJson
import org.elnix.dragonlauncher.base.model.serializables.Nest.Companion.defaultDragDistance
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.PointsListJson
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.dummySwipePoint
import org.elnix.dragonlauncher.base.undoredo.UndoRedoManager
import org.elnix.dragonlauncher.base.undoredo.UndoRedoStack
import org.elnix.dragonlauncher.settings.stores.array.NestsSettingsStore
import org.elnix.dragonlauncher.settings.stores.array.PointsSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.SwipeMapSettingsStore

public interface PointsService {
    public val defaultPoint: Flow<Point>
    public val points: Flow<Set<Point>>
    public val nests: Flow<Set<Nest>>

    public val undoRedo: UndoRedoManager

    public fun addPoint(newPoint: Point)
    public fun removePoint(id: String): Boolean
    public fun editPoint(id: String, editedPoint: (Point) -> Point): Boolean

    public fun addNest(circleNumber: Int = 3): Int
    public fun deleteNest(id: Int): Boolean
    public fun editNest(id: Int, editedNest: (Nest) -> Nest): Boolean


    public val selectedPoint: SettingFlow<Point?>
    public fun select(point: Point?)
    public fun persist()

    /** Set the given [points], [nests] and [defaultPoint] if not null. */
    public fun set(
        points: Set<Point>? = null,
        nests: Set<Nest>? = null,
        defaultPoint: Point? = null
    )

    public fun reset(
        resetPoints: Boolean = false,
        resetNests: Boolean = false,
        resetDefaultPoint: Boolean = false
    )
}

internal class PointsServiceImpl(
    private val ctx: Context
) : PointsService {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _defaultPoint = MutableStateFlow(dummySwipePoint())
    override val defaultPoint: Flow<Point> = _defaultPoint.asStateFlow()

    private val _points = MutableStateFlow<Set<Point>>(emptySet())
    override val points: Flow<Set<Point>> = _points.asStateFlow()

    private val _nests = MutableStateFlow<Set<Nest>>(emptySet())
    override val nests: Flow<Set<Nest>> = _nests.asStateFlow()

    override val selectedPoint: SettingFlow<Point?> = SettingFlow(null)
    override fun select(point: Point?) {
        selectedPoint.value = point
    }


    override val undoRedo: UndoRedoManager = UndoRedoManager(
        arrayOf(
            UndoRedoStack(
                snapshot = { _points.value.map { it.copy() } },
                restore = {
                    set(points = it.toSet())
                    selectedPoint.value = _points.value.find { p -> p.id == (selectedPoint.value?.id ?: "") }
                }
            ),
            UndoRedoStack(
                snapshot = { _nests.value.map { it.copy() } },
                restore = {
                    set(nests = it.toSet())
                }
            )
        )
    )


    init {
        scope.launch {
            async(start = CoroutineStart.ATOMIC) {
                loadPoints()
                loadNests()
                loadDefaultPoint()
            }.await()

//            logI(POINTS_TAG) { "Loaded Swipe Settings:\nPoints = ${_points.value}\nNests = ${_nests.value}\nDefault Point = ${_defaultPoint.value}" }
        }
    }

    override fun addPoint(newPoint: Point) {
        _points.value += newPoint
    }

    override fun removePoint(id: String): Boolean {
        val pointToRemove = _points.value.find { it.id == id } ?: return false
        _points.value -= pointToRemove
        return true
    }


    override fun editPoint(id: String, editedPoint: (Point) -> Point): Boolean {
        val oldPoint = _points.value.find { it.id == id } ?: return false
        _points.value -= oldPoint
        _points.value += editedPoint(oldPoint)
        return true
    }

    override fun addNest(circleNumber: Int): Int {
        val existingIds = _nests.value.map { it.id }.toSet()
        // Starts at index 0, and iterate trough each nests to fill the ids, and recreate the nest 0 if missing
        var newNestId = 0 //_nests.value.minOf { it.id }
        while (newNestId in existingIds) {
            newNestId++
        }

        val dragDistances = mutableStateMapOf<Int, Int>().apply {
            for (id in -1..<circleNumber) {
                this[id] = defaultDragDistance(id)
            }
        }

        val newNest = Nest(
            id = newNestId,
            dragDistances = dragDistances
        )

        _nests.value += newNest
        return newNestId
    }

    override fun deleteNest(id: Int): Boolean {
        val nestToDelete = _nests.value.find { it.id == id } ?: return false
        _nests.value -= nestToDelete
        return true
    }

    override fun editNest(id: Int, editedNest: (Nest) -> Nest): Boolean {
        val oldNest = _nests.value.find { it.id == id } ?: return false
        _nests.value -= oldNest
        _nests.value += editedNest(oldNest)
        return true
    }

    override fun persist() {
        scope.launch {


            val encodedPoints = PointsListJson.encode(_points.value)
            val encodedNests = NestJson.encode(_nests.value)

            PointsSettingsStore.jsonSetting.set(ctx, encodedPoints)
            NestsSettingsStore.jsonSetting.set(ctx, encodedNests)
            SwipeMapSettingsStore.defaultPoint.set(ctx, _defaultPoint.value)
        }
    }

    override fun set(
        points: Set<Point>?,
        nests: Set<Nest>?,
        defaultPoint: Point?
    ) {
        if (points != null) {
            _points.value = points
        }

        if (nests != null) {
            _nests.value = nests
        }

        if (defaultPoint != null) {
            _defaultPoint.value = defaultPoint
        }

        persist()
    }


    override fun reset(
        resetPoints: Boolean,
        resetNests: Boolean,
        resetDefaultPoint: Boolean
    ) {
        require(resetPoints || resetNests || resetDefaultPoint) {
            "Must at least reset something"
        }

        if (resetPoints) {
            _points.value = emptySet()
        }
        if (resetNests) {
            _nests.value = emptySet()
        }
        if (resetDefaultPoint) {
            _defaultPoint.value = Point.defaultSwipePointsValues
        }
    }

    private suspend fun loadPoints() {
        _points.value = PointsListJson.decode<Set<Point>>(PointsSettingsStore.jsonSetting.get(ctx), emptySet())
    }

    private suspend fun loadNests() {
        _nests.value = NestJson.decode<Set<Nest>>(NestsSettingsStore.jsonSetting.get(ctx), emptySet())
    }

    private suspend fun loadDefaultPoint() {
        _defaultPoint.value = SwipeMapSettingsStore.defaultPoint.get(ctx)
    }
}