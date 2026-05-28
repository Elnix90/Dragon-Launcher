package org.elnix.dragonlauncher.recents

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.serializables.Nest
import org.elnix.dragonlauncher.common.serializables.Point
import org.elnix.dragonlauncher.common.serializables.Point.Companion.dummySwipePoint
import org.elnix.dragonlauncher.settings.stores.SwipeSettingsStore

interface PointsService {
    val defaultPoint: Flow<Point>
    val points: Flow<List<Point>>
    val nests: Flow<List<Nest>>

    fun addPoint(newPoint: Point)
    fun removePoint(id: String): Boolean
    fun updateDefaultPoint(newDefaultPoint: Point)
    fun addNest(newNest: Nest)
    fun deleteNest(id: Int): Boolean
    fun editPoint(id: String, editedPoint: Point): Boolean
    fun editNest(id: Int, editedNest: Nest): Boolean
    fun persist()
}

internal class PointsServiceImpl(
    private val ctx: Context
) : PointsService {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _defaultPoint = MutableStateFlow(dummySwipePoint())
    override val defaultPoint: Flow<Point> = _defaultPoint.asStateFlow()

    private val _points = MutableStateFlow<List<Point>>(emptyList())
    override val points: Flow<List<Point>> = _points.asStateFlow()

    private val _nests = MutableStateFlow<List<Nest>>(emptyList())
    override val nests: Flow<List<Nest>> = _nests.asStateFlow()

    init {
        scope.launch {
            loadPoints()
            loadNests()
        }
    }

    override fun addPoint(newPoint: Point) {
        _points.value += newPoint
    }

    override fun removePoint(id: String): Boolean {
        val currentPoints = _points.value
        val newPoints = currentPoints.filterNot { it.id == id }
        if (newPoints.size == currentPoints.size) return false
        _points.value = newPoints
        return true
    }

    override fun updateDefaultPoint(newDefaultPoint: Point) {
        _defaultPoint.value = newDefaultPoint
    }

    override fun editPoint(id: String, editedPoint: Point): Boolean {
        _points.value = _points.value.map {
            if (it.id == id) editedPoint else it
        }
        return _points.value.any { it.id == editedPoint.id }
    }

    override fun addNest(newNest: Nest) {
        _nests.value += newNest
    }

    override fun deleteNest(id: Int): Boolean {
        val currentNests = _nests.value
        val newNests = currentNests.filterNot { it.id == id }
        if (newNests.size == currentNests.size) return false
        _nests.value = newNests
        return true
    }

    override fun editNest(id: Int, editedNest: Nest): Boolean {
        _nests.value = _nests.value.map {
            if (it.id == id) editedNest else it
        }
        return _nests.value.any { it.id == editedNest.id }
    }

    override fun persist() {
        scope.launch {
            SwipeSettingsStore.savePoints(ctx, _points.value)
            SwipeSettingsStore.saveNests(ctx, _nests.value)
        }
    }

    private suspend fun loadPoints() {
        _points.value = SwipeSettingsStore.getPoints(ctx)
    }

    private suspend fun loadNests() {
        _nests.value = SwipeSettingsStore.getNests(ctx)
    }
}