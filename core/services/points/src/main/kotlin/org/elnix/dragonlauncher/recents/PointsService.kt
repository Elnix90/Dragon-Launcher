package org.elnix.dragonlauncher.recents

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Nest.Companion.NestJson
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.PointsListJson
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.dummySwipePoint
import org.elnix.dragonlauncher.settings.stores.array.NestsSettingsStore
import org.elnix.dragonlauncher.settings.stores.array.PointsSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.SwipeMapSettingsStore

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
    fun set(points: List<Point>, nests: List<Nest>)
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
            async(start = CoroutineStart.ATOMIC) {
                loadPoints()
                loadNests()
                loadDefaultPoint()
            }.await()
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
            val encodedPoints = PointsListJson.encode(_points.value)
            val encodedNests = NestJson.encode(_nests.value)

            PointsSettingsStore.jsonSetting.set(ctx, encodedPoints)
            NestsSettingsStore.jsonSetting.set(ctx, encodedNests)
        }
    }

    override fun set(
        points: List<Point>,
        nests: List<Nest>
    ) {
        _points.value = points
        _nests.value = nests

        persist()
    }

    private suspend fun loadPoints() {
        _points.value = PointsListJson.decode<List<Point>>(PointsSettingsStore.jsonSetting.get(ctx)) ?: emptyList()
    }

    private suspend fun loadNests() {
        _nests.value = NestJson.decode<List<Nest>>(NestsSettingsStore.jsonSetting.get(ctx)) ?: emptyList()
    }

    private suspend fun loadDefaultPoint() {
        _defaultPoint.value = SwipeMapSettingsStore.defaultPoint.get(ctx)
    }
}