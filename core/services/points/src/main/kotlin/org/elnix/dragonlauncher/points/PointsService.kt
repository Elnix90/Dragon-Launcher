package org.elnix.dragonlauncher.points

import android.content.Context
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.model.models.HitResult
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Nest.Companion.NestJson
import org.elnix.dragonlauncher.base.model.serializables.Nests
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.PointsListJson
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.dummySwipePoint
import org.elnix.dragonlauncher.base.model.serializables.Points
import org.elnix.dragonlauncher.base.undoredo.UndoRedoManager
import org.elnix.dragonlauncher.base.undoredo.UndoRedoStack
import org.elnix.dragonlauncher.ktx.angle360FromOffset
import org.elnix.dragonlauncher.ktx.distance
import org.elnix.dragonlauncher.ktx.groupByTo
import org.elnix.dragonlauncher.settings.stores.array.NestsSettingsStore
import org.elnix.dragonlauncher.settings.stores.array.PointsSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.SwipeMapSettingsStore

public interface PointsService {
    public val defaultPoint: SettingFlow<Point>
    public val points: SettingFlow<Points>
    public val nests: SettingFlow<Nests>

    public val selectedPoint: SettingFlow<Point?>

    public val undoRedo: UndoRedoManager

    public fun addPoint(select: Boolean = true, newPoint: (Int) -> Point): Int
    public fun removePoint(id: Int): Boolean

    public fun editPoint(id: Int, editedPoint: (Point) -> Point): Boolean
    public fun addNest(nestId: Int? = null): Int
    public fun removeNest(id: Int): Boolean

    public fun editNest(id: Int, editedNest: (Nest) -> Nest): Boolean
    public fun select(point: Point?)
    public fun persist()

    /** Set the given [points], [nests] and [defaultPoint] if not null. */
    public fun set(
        newPoints: Points? = null,
        newNests: Nests? = null,
        newDefaultPoint: Point? = null
    )

    public fun reset(
        resetPoints: Boolean = false,
        resetNests: Boolean = false,
        resetDefaultPoint: Boolean = false
    )



    public fun resolveLiveNestHit(
        center: Offset,
        pointerPos: Offset,
        nest: Nest,
        liveNestScale: Float,
        graceDistancePx: Int = 0
    ): HitResult

    /**
     * Uses the nest the points belongs to, combined with its offset and the intersection shapes that are in the nest to compute the position ([Offset])
     * of the point in the main Canva
     *
     * The given calculation depends on the [depth] the drawing is at.
     * @return [Offset] the relative position of the point in ths nest
     */
    public fun computePointPosition(
        point: Point,
        depth: Int
    ): Offset

    public fun getPointsForNest(nest: Nest): Points

    public fun computeClosest(offset: Offset, nestId: Int?): Point

    public fun getFurthestPoint(nest: Nest): Point?
}


public class PointsServiceImpl(
    private val ctx: Context
): PointsService {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val defaultPoint: SettingFlow<Point> = SettingFlow(dummySwipePoint())

    override val points: SettingFlow<Points> = SettingFlow(emptySet())

    override val nests: SettingFlow<Nests> = SettingFlow(emptySet())

    override val selectedPoint: SettingFlow<Point?> = SettingFlow(null)
    override fun select(point: Point?) {
        selectedPoint.value = point
    }

    override val undoRedo: UndoRedoManager = UndoRedoManager(
        arrayOf(
            UndoRedoStack(
                snapshot = { points.value.map { it.copy() } },
                restore = {
                    set(newPoints = it.toSet())
                    selectedPoint.value = points.value.find { p -> p.id == (selectedPoint.value?.id ?: "") }
                }
            ),
            UndoRedoStack(
                snapshot = { nests.value.map { it.copy() } },
                restore = {
                    set(newNests = it.toSet())
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

//            logI(POINTS_TAG) { "Loaded Swipe Settings:\nPoints = ${points.value}\nNests = ${nests.value}\nDefault Point = ${defaultPoint.value}" }
        }
    }

    override fun addPoint(select: Boolean, newPoint: (Int) -> Point): Int {
        val existingIds = points.value.mapTo(mutableSetOf()) { it.id }
        val newId = getNextId(existingIds)
        val newPoint = newPoint(newId)

        val pointGridCell: Pair<Int, Int> = cellKey(newPoint.offset)
        grid.getOrPut(pointGridCell) { mutableSetOf() }.add(newPoint)

        undoRedo.applyChange { points.value += newPoint }
        if (select) select(newPoint)
        return newId
    }

    override fun removePoint(id: Int): Boolean {
        val pointToRemove = points.value.find { it.id == id } ?: return false

        val pointGridCell = cellKey(pointToRemove.offset)
        grid[pointGridCell]?.remove(pointToRemove)

        undoRedo.applyChange { points.value -= pointToRemove }
        return true
    }

    override fun editPoint(id: Int, editedPoint: (Point) -> Point): Boolean {
        val oldPoint = points.value.find { it.id == id } ?: return false

        val oldPointGridCell = cellKey(oldPoint.offset)
        grid[oldPointGridCell]?.remove(oldPoint)

        val editedPoint = editedPoint(oldPoint)
        val newPointGridCell = cellKey(editedPoint.offset)
        grid.getOrPut(newPointGridCell) { mutableSetOf() }.add(editedPoint)

        undoRedo.applyChange {
            points.value -= oldPoint
            points.value += editedPoint
        }
        return true
    }

    override fun addNest(nestId: Int?): Int {
        val existingIds = nests.value.mapTo(mutableSetOf()) { it.id }
        val newId = if (nestId != null && nestId !in existingIds) nestId else getNextId(existingIds)
        val newNest = Nest(id = newId)

        undoRedo.applyChange {
            nests.value += newNest
        }

        return newId
    }

    override fun removeNest(id: Int): Boolean {
        val nestToDelete = nests.value.find { it.id == id } ?: return false
        undoRedo.applyChange { nests.value -= nestToDelete }
        return true
    }

    override fun editNest(id: Int, editedNest: (Nest) -> Nest): Boolean {
        val oldNest = nests.value.find { it.id == id } ?: return false
        undoRedo.applyChange {
            nests.value -= oldNest
            nests.value += editedNest(oldNest)
        }
        return true
    }

    override fun persist() {
        scope.launch {
            val encodedPoints = PointsListJson.encode(points.value)
            val encodedNests = NestJson.encode(nests.value)

            PointsSettingsStore.jsonSetting.set(ctx, encodedPoints)
            NestsSettingsStore.jsonSetting.set(ctx, encodedNests)
            SwipeMapSettingsStore.defaultPoint.set(ctx, defaultPoint.value)
        }
    }

    override fun set(
        newPoints: Points?,
        newNests: Nests?,
        newDefaultPoint: Point?
    ) {
        require(newPoints != null || newNests != null || newDefaultPoint != null) {
            "One of all 3 arg must not bu null"
        }

        undoRedo.applyChange {
            if (newPoints != null) {
                points.value = newPoints
                resetGrid()
            }

            if (newNests != null) {
                nests.value = newNests
            }

            if (newDefaultPoint != null) {
                defaultPoint.value = newDefaultPoint
            }
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

        undoRedo.applyChange {
            if (resetPoints) {
                points.value = emptySet()
                resetGrid()
            }
            if (resetNests) {
                nests.value = emptySet()
            }
            if (resetDefaultPoint) {
                defaultPoint.value = Point.defaultSwipePointsValues
            }
        }

        persist()
    }

    private suspend fun loadPoints() {
        points.value = PointsListJson.decode<Set<Point>>(PointsSettingsStore.jsonSetting.get(ctx), emptySet())
        resetGrid()
    }

    private suspend fun loadNests() {
        nests.value = NestJson.decode<Set<Nest>>(NestsSettingsStore.jsonSetting.get(ctx), emptySet())
    }

    private suspend fun loadDefaultPoint() {
        defaultPoint.value = SwipeMapSettingsStore.defaultPoint.get(ctx)
    }

    private fun getNextId(existing: Set<Int>): Int {
        // Starts at index 0, and iterate trough each id to fill the missing ones (shouldn't happen)
        var newId = 0
        while (newId in existing) {
            newId++
        }
        return newId
    }


    // START OF COMPUTATION SYSTEM

    private var grid: MutableMap<Pair<Int, Int>, MutableSet<Point>> = mutableMapOf()
    private var lastTarget: Offset = Offset.Zero
    private var searchRadius: Int = 1

    private val gridSize = 150f


    private fun resetGrid() {
        grid = buildGrid(points.value)
        lastTarget = Offset.Zero
        searchRadius = 1
    }


    public override fun computeClosest(offset: Offset, nestId: Int?): Point {

        @Suppress("LiftReturnOrAssignment")
        if (distance(lastTarget, offset) > gridSize) {
            searchRadius = 1
        } else {
            searchRadius = minOf(3, searchRadius + 1)
        }

        val targetCell: Pair<Int, Int> = cellKey(offset)
        val candidates: MutableSet<Point> = mutableSetOf()

        var expandRadius = searchRadius
        while (true) {
            for (dx in -expandRadius..expandRadius) {
                for (dy in -expandRadius..expandRadius) {
                    grid[Pair(targetCell.first + dx, targetCell.second + dy)]
                        ?.let { points ->
                            val filteredPointsByNest = points.filter { it.nestId == nestId }
                            candidates.addAll(filteredPointsByNest)
                        }
                }
            }
            if (candidates.isNotEmpty()) break
            expandRadius++
        }

        lastTarget = offset

        return candidates.minBy { p ->
            val dx: Float = offset.x - p.offset.x
            val dy: Float = offset.y - p.offset.y
            dx * dx + dy * dy
        }
    }


    public override fun getFurthestPoint(nest: Nest): Point? {
        return points.value
            .filter { it.nestId == nest.id }
            .maxByOrNull { it.offset.getDistanceSquared() }
    }


    public override fun resolveLiveNestHit(
        center: Offset,
        pointerPos: Offset,
        nest: Nest,
        liveNestScale: Float,
        graceDistancePx: Int
    ): HitResult {
//        val scaledNest = nest scaledBy liveNestScale
        val dist = distance(center, pointerPos)
        val angle360 = angle360FromOffset(center, pointerPos)
        val outerRadius = getFurthestPoint(nest)?.offset?.getDistance() ?: Float.MAX_VALUE

        graceDistancePx.takeIf { it > -1 }?.let {
            if (outerRadius > 0f && dist > outerRadius + graceDistancePx) {
                return HitResult(
                    selectedPoint = null,
                    isOutsideBounds = true,
                    isInCancelZone = false,
                    angle360 = angle360,
                    targetShape = null // TODO
                )
            }
        }

        val isInCancelZone = dist <= nest.cancelZone

        // When inside the cancel zone there is no point to select.
        val selectedPoint = if (isInCancelZone) {
            null
        } else {
            computeClosest(pointerPos, nest.id)
        }

        return HitResult(
            selectedPoint = selectedPoint,
            isOutsideBounds = false,
            isInCancelZone = isInCancelZone,
            angle360 = angle360,
            targetShape = null // TODO
        )
    }


    private fun buildGrid(points: Points): MutableMap<Pair<Int, Int>, MutableSet<Point>> =
        points.groupByTo(mutableMapOf<Pair<Int, Int>, MutableSet<Point>>()) { point -> cellKey(point.offset) }

    private fun cellKey(offset: Offset): Pair<Int, Int> =
        Pair((offset.x / gridSize).toInt(), (offset.y / gridSize).toInt())

}