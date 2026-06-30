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
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Nest.Companion.NestJson
import org.elnix.dragonlauncher.base.model.serializables.Nests
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.PointJson
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.PointsJson
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.dummySwipePoint
import org.elnix.dragonlauncher.base.model.serializables.Points
import org.elnix.dragonlauncher.base.undoredo.UndoRedoManager
import org.elnix.dragonlauncher.base.undoredo.UndoRedoStack
import org.elnix.dragonlauncher.ktx.angle360FromOffset
import org.elnix.dragonlauncher.ktx.distance
import org.elnix.dragonlauncher.ktx.groupByTo
import org.elnix.dragonlauncher.settings.stores.array.NestsSettingsStore
import org.elnix.dragonlauncher.settings.stores.array.PointsSettingsStore
import org.elnix.dragonlauncher.settings.stores.objects.DefaultPointSettingsStore
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

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

    public fun editDefaultPoint(newDefaultPoint: Point)

    /**
     * Select the given [Point] by it id
     * If `null` is provided, the [selectedPoint] is deselected
     */
    public fun select(id: Int?)
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
        normalizedPos: Offset,
        nest: Nest,
        liveNestScale: Float,
        graceDistancePx: Int = 0
    ): HitResult

    /**
     * Uses the nest the points belongs to, combined with its offset and the intersection shapes that are in the nest to compute the position ([Offset])
     * of the point in the main Canva
     *
     * @return [Offset] the relative position of the point in ths nest
     */
    public fun computePointOffset(point: Point): Offset

    public fun getPointsForNest(
        nest: Nest,
        skipSelected: Boolean
    ): Points

    /**
     * Compute the closest point relative to the [normalizedPos] given their [Point.offset] and the eventual [shape][Point.collidingShapeId] they are tied to
     *
     * Special cases:
     *  - [points] is empty -> `null`
     *  - [points] contains a single element -> the single point
     */
    public fun computeClosest(
        normalizedPos: Offset,
        nestId: Int?
    ): Point?

    /**
     * Same as [computeClosest] but ignores the given [ignoredPointId].
     */
    public fun computeClosestExcept(
        ignoredPointId: Int?,
        normalizedPos: Offset,
        nestId: Int?
    ): Point?

    public fun getFurthestPoint(nest: Nest): Point?
}


internal class PointsServiceImpl(
    private val ctx: Context
) : PointsService {

    private typealias GridCase = Pair<Int, Int>
    private typealias MutablePoints = MutableSet<Point>
    private typealias GridMap = MutableMap<GridCase, MutablePoints>
    private typealias NestGrid = MutableMap<Int, MutablePoints>
    private typealias FurthestGrid = MutableMap<Int, Point?>

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val defaultPoint: SettingFlow<Point> = SettingFlow(dummySwipePoint())

    override val points: SettingFlow<Points> = SettingFlow(emptySet())

    override val nests: SettingFlow<Nests> = SettingFlow(emptySet())

    override val selectedPoint: SettingFlow<Point?> = SettingFlow(null)
    override fun select(id: Int?) {
        undoRedo.applyChange {
            selectedPoint.value = points.value.find { it.id == id }
        }
    }

    override val undoRedo: UndoRedoManager = UndoRedoManager(
        arrayOf(
            UndoRedoStack(
                snapshot = { points.value.map { it.copy() } },
                restore = {
                    set(newPoints = it.toSet())
                    selectedPoint.value = points.value.find { p -> p.id == selectedPoint.value?.id }
                }
            ),
            UndoRedoStack(
                snapshot = { nests.value.map { it.copy() } },
                restore = { set(newNests = it.toSet()) }
            ),
            UndoRedoStack(
                snapshot = { defaultPoint.value },
                restore = { set(newDefaultPoint = it) }
            ),
            UndoRedoStack(
                snapshot = { selectedPoint.value },
                restore = { selectedPoint.value = it }
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

            resetGrids()
        }
    }

    override fun addPoint(select: Boolean, newPoint: (Int) -> Point): Int {
        val existingIds = points.value.mapTo(mutableSetOf()) { it.id }
        val newId = getNextId(existingIds)
        val newPoint = newPoint(newId)
//        val nestId = newPoint.nestId
//
//        val pointGridCell: GridCase = cellKey(newPoint.offset)
//        grid.getOrPut(pointGridCell) { mutableSetOf() }.add(newPoint)
//        nestGrid[nestId]?.plus(newPoint)
//
//
//        val furthestForNest = furthestPointGrid[nestId]
//
//        if (furthestForNest != null && furthestForNest.offset.getDistanceSquared() > newPoint.offset.getDistanceSquared()) {
//            furthestPointGrid[nestId] = newPoint
//        }

        undoRedo.applyChange { points.value += newPoint }
        if (select) select(newId)
        resetGrids()
        return newId
    }

    override fun removePoint(id: Int): Boolean {
        val pointToRemove = points.value.find { it.id == id } ?: return false
//        val nestId = pointToRemove.nestId
//
//        val pointGridCell = cellKey(pointToRemove.offset)
//        grid[pointGridCell]?.remove(pointToRemove)
//        nestGrid[nestId]?.minus(pointToRemove)
//
//        val furthestForNest = furthestPointGrid[nestId]
//        if (furthestForNest?.id == pointToRemove.id) {
//            furthestPointGrid[nestId] = nestGrid[nestId]?.maxByOrNull { it.offset.getDistanceSquared() }
//        }

        undoRedo.applyChange { points.value -= pointToRemove }


        resetGrids()

        return true
    }

    override fun editPoint(id: Int, editedPoint: (Point) -> Point): Boolean {
        val oldPoint = points.value.find { it.id == id } ?: return false
        val editedPoint = editedPoint(oldPoint)
//
//        val oldNestId = oldPoint.nestId
//        val editedNestId = editedPoint.nestId
//
//        val oldPointGridCell = cellKey(oldPoint.offset)
//        grid[oldPointGridCell]?.remove(oldPoint)
//
//        val newPointGridCell = cellKey(editedPoint.offset)
//        grid.getOrPut(newPointGridCell) { mutableSetOf() }.add(editedPoint)
//
//        nestGrid[oldPoint.nestId]?.minus(oldPoint)
//        nestGrid[editedPoint.nestId]?.plus(editedPoint)
//
//
//        if (oldNestId == editedNestId) {
//            val furthestForNest = furthestPointGrid[oldNestId]
//            if (furthestForNest != null && furthestForNest.offset.getDistanceSquared() < editedPoint.offset.getDistanceSquared()) {
//                furthestPointGrid[oldNestId] = editedPoint
//            }
//        } else {
//            // Shit again here bc the point has moved from nest
//            val furthestForNest = furthestPointGrid[oldNestId]
//            if (furthestForNest != null && furthestForNest.id == id) {
//                // This is really annoying bc
//            }
//
//        }
//        if (furthestForNest?.id == pointToRemove.id) {
//            furthestPointGrid[nestId] = nestGrid[nestId]?.maxByOrNull { it.offset.getDistanceSquared() }
//        }

        undoRedo.applyChange {
            points.value -= oldPoint
            points.value += editedPoint
        }

        resetGrids()

        return true
    }

    override fun addNest(nestId: Int?): Int {
        val existingIds = nests.value.mapTo(mutableSetOf()) { it.id }
        val newId = if (nestId != null && nestId !in existingIds) nestId else getNextId(existingIds)
        val newNest = Nest(id = newId)
        undoRedo.applyChange { nests.value += newNest }
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

    override fun editDefaultPoint(newDefaultPoint: Point) {
        undoRedo.applyChange { set(newDefaultPoint = newDefaultPoint) }
    }

    override fun persist() {
        scope.launch {
            val encodedPoints = PointsJson.encode(points.value)
            PointsSettingsStore.jsonSetting.set(ctx, encodedPoints)

            val encodedNests = NestJson.encode(nests.value)
            NestsSettingsStore.jsonSetting.set(ctx, encodedNests)

            val encodedDefaultPoint = PointJson.encode(defaultPoint.value)
            DefaultPointSettingsStore.jsonSetting.set(ctx, encodedDefaultPoint)
        }
    }

    override fun set(
        newPoints: Points?,
        newNests: Nests?,
        newDefaultPoint: Point?
    ) {
        require(newPoints != null || newNests != null || newDefaultPoint != null) { "One of all 3 args must not bu null" }

        if (newPoints != null) {
            points.value = newPoints
            resetGrids()
        }

        if (newNests != null) {
            nests.value = newNests
        }

        if (newDefaultPoint != null) {
            defaultPoint.value = newDefaultPoint
        }

        persist()
    }


    override fun reset(
        resetPoints: Boolean,
        resetNests: Boolean,
        resetDefaultPoint: Boolean
    ) {
        require(resetPoints || resetNests || resetDefaultPoint) { "Must at least reset something" }

        undoRedo.applyChange {
            if (resetPoints) {
                points.value = emptySet()
                selectedPoint.value = null
                resetGrids()
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
        points.value = PointsJson.decode<Points>(PointsSettingsStore.jsonSetting.get(ctx), emptySet())
        resetGrids()
    }

    private suspend fun loadNests() {
        nests.value = NestJson.decode<Nests>(NestsSettingsStore.jsonSetting.get(ctx), emptySet())
    }

    private suspend fun loadDefaultPoint() {
        defaultPoint.value = PointJson.decode(DefaultPointSettingsStore.jsonSetting.get(ctx), Point.defaultSwipePointsValues)
    }

    private fun getNextId(existing: Set<Int>): Int {
        // Starts at index 0, and iterate trough each id to fill the missing ones (shouldn't happen)
        var newId = 0
        while (newId in existing) {
            newId++
        }
        return newId
    }


    private var grid: GridMap = mutableMapOf()
    private var nestGrid: NestGrid = mutableMapOf()
    private var furthestPointGrid: FurthestGrid = mutableMapOf()

    private var lastTarget: Offset = Offset.Zero
    private var searchRadius: Int = 1
    private val gridSize = 150f

    /**
     * I originally wanted to update the caches dynamically when any points is updated,
     * but it was way too many errors that could create caches misses and undefined behavior.
     * Now since the points shouldn't be updated when you usually drag in the main screen
     */
    private fun resetGrids() {
        grid = points.value.groupByTo(mutableMapOf<GridCase, MutablePoints>()) { point -> cellKey(point.offset) }
        nestGrid = points.value.groupByTo(mutableMapOf<Int, MutablePoints>()) { point -> point.nestId }


        furthestPointGrid = points.value
            .groupBy { it.nestId }
            .mapValues { (_, nestPoints) ->
                nestPoints.maxByOrNull { it.offset.getDistanceSquared() }
            }
            .toMutableMap()


        lastTarget = Offset.Zero
        searchRadius = 1
    }


    override fun getFurthestPoint(nest: Nest): Point? = furthestPointGrid[nest.id]

    override fun computeClosestExcept(
        ignoredPointId: Int?,
        normalizedPos: Offset,
        nestId: Int?
    ): Point? {
        return when (points.value.size) {
            0 -> null
            1 -> points.value.first()
            else -> {

                @Suppress("LiftReturnOrAssignment")
                if (distance(lastTarget, normalizedPos) > gridSize) {
                    searchRadius = 1
                } else {
                    searchRadius = minOf(3, searchRadius + 1)
                }

                val targetCell: GridCase = cellKey(normalizedPos)
                val candidates: MutablePoints = mutableSetOf()

                var expandRadius = searchRadius
                while (true) {
                    for (dx in -expandRadius..expandRadius) {
                        for (dy in -expandRadius..expandRadius) {
                            grid[Pair(targetCell.first + dx, targetCell.second + dy)]
                                ?.let { points ->
                                    val filteredPointsByNest = points.filter {
                                        it.nestId == nestId && it.id != ignoredPointId
                                    }
                                    candidates.addAll(filteredPointsByNest)
                                }
                        }
                    }
                    if (candidates.isNotEmpty()) break
                    expandRadius++
                }

                lastTarget = normalizedPos

                candidates.minBy { p ->
                    val dx: Float = normalizedPos.x - p.offset.x
                    val dy: Float = normalizedPos.y - p.offset.y
                    dx * dx + dy * dy
                }
            }
        }
    }

    override fun computeClosest(
        normalizedPos: Offset,
        nestId: Int?
    ): Point? =
        computeClosestExcept(
            ignoredPointId = null,
            normalizedPos = normalizedPos,
            nestId = nestId
        )


    override fun resolveLiveNestHit(
        normalizedPos: Offset,
        nest: Nest,
        liveNestScale: Float,
        graceDistancePx: Int
    ): HitResult {
        val dist = normalizedPos.getDistance()
        val angle360 = angle360FromOffset(normalizedPos)
        val outerRadius = getFurthestPoint(nest)?.offset?.getDistance() ?: Float.MAX_VALUE

        graceDistancePx.takeIf { it > -1 }?.let {
            if (outerRadius > 0f && dist > outerRadius + graceDistancePx) {
                return HitResult(
                    selectedPoint = null,
                    isOutsideBounds = true,
                    isInCancelZone = false,
                    angle360 = angle360,
                )
            }
        }

        val isInCancelZone = dist <= nest.cancelZone

        // When inside the cancel zone there is no point to select.
        val selectedPoint = if (isInCancelZone) {
            null
        } else {
            computeClosest(normalizedPos, nest.id)
        }

        return HitResult(
            selectedPoint = selectedPoint,
            isOutsideBounds = false,
            isInCancelZone = isInCancelZone,
            angle360 = angle360,
        )
    }

    override fun computePointOffset(point: Point): Offset {
        val nest = nests.value.find { it.id == point.nestId } ?: return point.offset
        val shapeId = point.collidingShapeId ?: return point.offset
        val shape = nest.intersectionShapes.find { it.id == shapeId } ?: return point.offset

        val angleRad = atan2(point.offset.y, point.offset.x)
        val halfSize = shape.size / 2f
        val rotationRad = Math.toRadians((shape.angle ?: 0).toDouble()).toFloat()

        val boundary = computeShapeBoundary(shape.shape, halfSize, angleRad, rotationRad)
        return shape.centerOffset + boundary
    }

    override fun getPointsForNest(
        nest: Nest,
        skipSelected: Boolean
    ): Points {
        val pointsInTheNest: MutablePoints = nestGrid[nest.id] ?: return emptySet()
        val selectedPoint = selectedPoint.value ?: return pointsInTheNest
        return if (skipSelected) pointsInTheNest - selectedPoint else pointsInTheNest
    }

    /** Returns the point where the ray at [angleRad] (from origin) first hits
     *  the boundary of [iconShape] when the shape is inscribed in a circle of
     *  radius [halfSize] and rotated by [rotationRad]. Unsupported shapes fall
     *  back to a circle boundary. */
    private fun computeShapeBoundary(
        iconShape: IconShape,
        halfSize: Float,
        angleRad: Float,
        rotationRad: Float,
    ): Offset = when (iconShape) {
        is IconShape.Circle -> circleBoundary(halfSize, angleRad)

        is IconShape.Square,
        is IconShape.RoundedSquare,
        is IconShape.Cookie4Sided ->
            polygonBoundary(4, halfSize, angleRad, rotationRad)

        is IconShape.Diamond ->
            polygonBoundary(4, halfSize, angleRad, rotationRad + (PI / 4f).toFloat())

        is IconShape.Triangle,
        is IconShape.PixelTriangle ->
            polygonBoundary(3, halfSize, angleRad, rotationRad)

        is IconShape.Pentagon ->
            polygonBoundary(5, halfSize, angleRad, rotationRad)

        is IconShape.Hexagon,
        is IconShape.Cookie6Sided ->
            polygonBoundary(6, halfSize, angleRad, rotationRad)

        is IconShape.Cookie7Sided ->
            polygonBoundary(7, halfSize, angleRad, rotationRad)

        is IconShape.Cookie9Sided ->
            polygonBoundary(9, halfSize, angleRad, rotationRad)

        is IconShape.Cookie12Sided ->
            polygonBoundary(12, halfSize, angleRad, rotationRad)

        is IconShape.Custom ->
            polygonBoundary(iconShape.numVertices, halfSize, angleRad, rotationRad)

        else -> circleBoundary(halfSize, angleRad)
    }

    /** Point on a circle of [radius] at the given angle. */
    private fun circleBoundary(
        radius: Float,
        angleRad: Float,
    ): Offset = Offset(radius * cos(angleRad), radius * sin(angleRad))

    /** Intersection of a ray at [angleRad] with a regular [numSides]-gon
     *  inscribed in a circle of [radius], rotated by [rotationRad]. */
    private fun polygonBoundary(
        numSides: Int,
        radius: Float,
        angleRad: Float,
        rotationRad: Float,
    ): Offset {
        val dir = Offset(cos(angleRad), sin(angleRad))
        val epsilon = 1e-6f
        var minT = Float.MAX_VALUE

        for (k in 0 until numSides) {
            val a1 = (2.0 * PI * k / numSides + rotationRad).toFloat()
            val a2 = (2.0 * PI * ((k + 1) % numSides) / numSides + rotationRad).toFloat()
            val v1 = Offset(radius * cos(a1), radius * sin(a1))
            val v2 = Offset(radius * cos(a2), radius * sin(a2))
            val edgeX = v2.x - v1.x
            val edgeY = v2.y - v1.y
            val det = dir.x * edgeY - dir.y * edgeX
            if (abs(det) < epsilon) continue
            val t = (v1.x * edgeY - v1.y * edgeX) / det
            val s = (v1.x * dir.y - v1.y * dir.x) / det
            if (t >= 0f && s >= 0f && s <= 1f && t < minT) {
                minT = t
            }
        }

        return if (minT < Float.MAX_VALUE) {
            dir * minT
        } else {
            circleBoundary(radius, angleRad)

        }
    }

    private fun cellKey(offset: Offset): GridCase =
        Pair((offset.x / gridSize).toInt(), (offset.y / gridSize).toInt())

}
