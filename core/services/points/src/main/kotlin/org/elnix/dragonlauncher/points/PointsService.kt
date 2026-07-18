@file:Suppress("OVERRIDE_BY_INLINE")

package org.elnix.dragonlauncher.points

import android.content.Context
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.cache.NestIntersectionShapesPathCache
import org.elnix.dragonlauncher.base.cache.PointStableCache
import org.elnix.dragonlauncher.base.model.models.HitResult
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
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
import org.elnix.dragonlauncher.ktx.angleDeg
import org.elnix.dragonlauncher.ktx.angleRad
import org.elnix.dragonlauncher.ktx.distanceTo
import org.elnix.dragonlauncher.ktx.getNextId
import org.elnix.dragonlauncher.ktx.groupByTo
import org.elnix.dragonlauncher.settings.stores.array.NestsSettingsStore
import org.elnix.dragonlauncher.settings.stores.array.PointsSettingsStore
import org.elnix.dragonlauncher.settings.stores.objects.DefaultPointSettingsStore
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

public interface PointsService {
    public val defaultPoint: SettingFlow<Point>
    public val points: StateFlow<Points>
    public val nests: StateFlow<Nests>

    public val recomposeTrigger: SettingFlow<Int>

    /**
     * Selected points ids, a [List] of all selected points, by order of selection.
     *
     * A *File* according to M.Morlong, thanks!
     */
    public val selectedPointsIds: SettingFlow<List<Int>>

    public val undoRedo: UndoRedoManager

    public fun addPoint(select: Boolean = true, newPoint: (Int) -> Point): Int
    public fun removePoint(id: Int)
    public fun editPoint(
        id: Int,
        editedPoint: (Point) -> Point
    )

    public fun addNest(nestId: Int? = null): Int
    public fun removeNest(id: Int)
    public fun editNest(
        id: Int,
        editedNest: (Nest) -> Nest
    )

    public fun resetNest(id: Int)

    public fun updateNest(
        nestId: Int,
        shapeId: Int?,
        netOffsetChange: Offset,
        editedNest: (Nest) -> Nest
    )

    public fun editDefaultPoint(newDefaultPoint: Point)


    public fun findPointById(id: Int): Point?
    public fun findNestById(id: Int): Nest


    /**
     * Select the given [Point] by it id
     * If `null` is provided, the [selectedPointsIds] is cleared
     */
    public fun select(id: Int)

    /**
     * Select ony one, means that either all selected points are removed from the list and only the one provided is added, or if it is null, they are all removed
     */
    public fun selectOnyOne(id: Int?)

    /**
     * Pretty much self-explanatory ig
     */
    public fun deselect(id: Int)
    public fun selectAll(nestId: Int)
    public fun deselectAll()
    public fun invertSelection(nestId: Int)


    /**
     * Persist the values: [points], [nests] and [defaultPoint] into datastore
     * Do not call this too repetitively to prevent I/O overhead
     */
    public fun persist()

    /** Set the given [points], [nests] and [defaultPoint] if not null. */
    public fun set(
        newPoints: Points? = null,
        newNests: Nests? = null,
        newDefaultPoint: Point? = null
    )

    /**
     * Reset [points], [nests] and/or [defaultPoint] whether the value is given in parameter
     *
     * Must at least reset one of these
     * @throws [IllegalArgumentException] if all 3 parameters are false
     */
    public fun reset(
        resetPoints: Boolean = false,
        resetNests: Boolean = false,
        resetDefaultPoint: Boolean = false
    )


    public fun resolveLiveNestHit(
        normalizedPos: Offset,
        nestId: Int,
        liveNestScale: Float,
        graceDistancePx: Int?
    ): HitResult

    /**
     * Uses the nest the points belongs to, combined with its offset and the intersection shapes that are in the nest to compute the position ([Offset])
     * of the point in the main Canva
     *
     * @return [Offset] the relative position of the point in ths nest
     */
    public fun computePointOffset(point: Point): Offset

    public fun computePointOffsetRealTime(point: Point, shape: IntersectionShape): Offset

    public fun getPointsForNest(
        nestId: Int,
        skipSelected: Boolean
    ): Points

    public fun getSelectedShapeIds(nestId: Int): Set<Int>

    /**
     * Compute the closest point relative to the [normalizedPos] given their [Point.offset] and the eventual [shape][Point.shapeId] they are tied to
     *
     * Special cases:
     *  - [points] is empty -> `null`
     *  - [points] contains a single element -> the single point
     */
    public fun computeClosest(
        normalizedPos: Offset,
        nestId: Int
    ): Point?

    /**
     * Same as [computeClosest] but ignores the given [ignoredPointId].
     */
    public fun computeClosestExcept(
        ignoredPointId: Array<Int>?,
        normalizedPos: Offset,
        nestId: Int
    ): Point?

    public fun autoSeparate(
        nestId: Int,
        draggedPointId: Int
    ): Boolean
}


internal class PointsServiceImpl(
    private val ctx: Context
) : PointsService {

    private typealias GridCase = Pair<Int, Int>
    private typealias MutablePoints = MutableSet<Point>
    private typealias NestGrid = MutableMap<Int, MutablePoints>

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val density = ctx.resources.displayMetrics.density


    override val defaultPoint: SettingFlow<Point> = SettingFlow(dummySwipePoint())
    private val _points = MutableStateFlow(ConcurrentHashMap<Int, Point>())
    override val points: StateFlow<Map<Int, Point>> = _points.asStateFlow()

    private val _nests = MutableStateFlow(ConcurrentHashMap<Int, Nest>())
    override val nests: StateFlow<Map<Int, Nest>> = _nests.asStateFlow()

    override val recomposeTrigger: SettingFlow<Int> = SettingFlow(0)

    override val selectedPointsIds: SettingFlow<List<Int>> = SettingFlow(emptyList())


    override val undoRedo: UndoRedoManager = UndoRedoManager(
        stacks = arrayOf(
            UndoRedoStack(
                snapshot = { _points.value },
                restore = { points ->
                    set(newPoints = points)

                    selectedPointsIds.value = points.keys.filter { it in selectedPointsIds.value }
                }
            ),
            UndoRedoStack(
                snapshot = { _nests.value },
                restore = { nests -> set(newNests = nests) }
            ),
            UndoRedoStack(
                snapshot = { defaultPoint.value },
                restore = { set(newDefaultPoint = it) }
            )
        ),
        scope = scope
    )

    private inline fun applyChange(mutator: () -> Unit) {

        undoRedo.applyChange(mutator)
        resetGrids()

        recomposeTrigger.value++
    }

    override fun select(id: Int) {
        val newSel: Point? = findPointById(id)
        val currentSelectedIds: List<Int> = selectedPointsIds.value

        when {
            // Deselect all if newSel is null and something is selected
            // I did not put the if below in the same line because of a failing smart cast to non-nullable point
            newSel == null -> {
                // Only deselect if the list isn't already empty to avoid undoRedo overhead
                if (currentSelectedIds.isNotEmpty()) {
                    selectedPointsIds.value = emptyList()

                }
            }

            // Deselect newSel if already selected
            newSel.id in currentSelectedIds -> {
                selectedPointsIds.value = currentSelectedIds - newSel.id

            }

            // Select newSel (add to set or create new set)
            else -> {
                selectedPointsIds.value = currentSelectedIds + (newSel.id)

            }
        }
        recomposeTrigger.value++
    }

    override fun deselect(id: Int) {
        if (id !in selectedPointsIds.value) return
        selectedPointsIds.value -= id
        recomposeTrigger.value++
    }

    override fun selectAll(nestId: Int) {
        selectedPointsIds.value = _points.value.filterValues { it.nestId == nestId }.keys.toList()
        recomposeTrigger.value++
    }

    override fun deselectAll() {
        selectedPointsIds.value = emptyList()
        recomposeTrigger.value++
    }

    override fun invertSelection(nestId: Int) {
        selectedPointsIds.value = _points.value.filterValues { it.nestId == nestId }.keys.toList() - selectedPointsIds.value.toSet()
        recomposeTrigger.value++
    }

    override fun selectOnyOne(id: Int?) {
        if (id == null) {
            selectedPointsIds.value = emptyList()
            return
        }

        val newSel: Point? = findPointById(id)
        val currentSelectedIds: List<Int> = selectedPointsIds.value

        when {
            // Deselect all if newSel is null and something is selected
            // I did not put the if below in the same line because of a failing smart cast to non-nullable point
            newSel == null -> {
                // Only deselect if the list isn't already empty to avoid undoRedo overhead
                if (currentSelectedIds.isNotEmpty()) {
                    selectedPointsIds.value = emptyList()

                }
            }

            else -> {
                selectedPointsIds.value = listOf(newSel.id)
            }
        }
        recomposeTrigger.value++
    }

    init {
        scope.launch {
            load()
            resetGrids()
            recomposeTrigger.value++
        }
    }

    override fun addPoint(select: Boolean, newPoint: (Int) -> Point): Int {
        val existingIds = _points.value.keys
        val newId = existingIds.getNextId()
        val newPoint = newPoint(newId)

        applyChange { _points.value[newPoint.id] = newPoint }

        if (select) select(newId)
        return newId
    }

    override fun removePoint(id: Int) {
        applyChange { _points.value.remove(id) }
    }

    override fun editPoint(
        id: Int,
        editedPoint: (Point) -> Point
    ) {
        val oldPoint = _points.value[id] ?: return
        val newPoint = editedPoint(oldPoint)
        if (oldPoint != newPoint) {
            applyChange {
                _points.value[id] = newPoint
            }
        }
    }

    override inline fun updateNest(
        nestId: Int,
        shapeId: Int?,
        netOffsetChange: Offset,
        editedNest: (Nest) -> Nest
    ) {
        applyChange {
            if (shapeId != null) {
                _points.value
                    .filter { (_, point) -> point.nestId == nestId && point.shapeId == shapeId }
                    .forEach { (id, point) ->

                        val pointChanged = point.copy(offset = point.offset + netOffsetChange)
                        _points.value[id] = pointChanged
                    }
            }

            _nests.value[nestId] = editedNest(findNestById(nestId))
        }
    }

    override fun addNest(nestId: Int?): Int {
        val existingIds = _nests.value.keys
        val newId = if (nestId != null && nestId !in existingIds) nestId else existingIds.getNextId()

        applyChange { _nests.value[newId] = Nest(id = newId) }

        return newId
    }

    override fun removeNest(id: Int) {
        applyChange { _nests.value.remove(id) }
    }

    override fun editNest(
        id: Int,
        editedNest: (Nest) -> Nest
    ) {
        val oldNest = findNestById(id)
        val newNest = editedNest(oldNest)

        if (oldNest != newNest) {
            applyChange {
                _nests.value[id] = newNest
            }
        }
    }

    override fun resetNest(id: Int) {
        applyChange {
            _nests.value[id] = Nest(id)
        }
    }

    override fun editDefaultPoint(newDefaultPoint: Point) {
        applyChange { set(newDefaultPoint = newDefaultPoint) }
    }


    private suspend fun load() {
        val decodedPoints = PointsJson.decode<Set<Point>>(PointsSettingsStore.jsonSetting.get(ctx), emptySet())
        _points.value = ConcurrentHashMap(decodedPoints.associateBy { it.id })

        val decodedNests = NestJson.decode<Set<Nest>>(NestsSettingsStore.jsonSetting.get(ctx), emptySet())
        _nests.value = ConcurrentHashMap(decodedNests.associateBy { it.id })

        val decodedDefaultPoint = PointJson.decode(DefaultPointSettingsStore.jsonSetting.get(ctx), Point.defaultSwipePointsValues)
        defaultPoint.value = decodedDefaultPoint
    }

    override fun persist() {
        scope.launch {
            val encodedPoints = PointsJson.encode<Set<Point>>(_points.value.values.toSet())
            PointsSettingsStore.jsonSetting.set(ctx, encodedPoints)

            val encodedNests = NestJson.encode<Set<Nest>>(_nests.value.values.toSet())
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
            _points.value = ConcurrentHashMap(newPoints)
        }

        if (newNests != null) {
            _nests.value = ConcurrentHashMap(newNests)
        }

        if (newDefaultPoint != null) {
            defaultPoint.value = newDefaultPoint
        }

        persist()
        resetGrids()
        recomposeTrigger.value++
    }


    override fun reset(
        resetPoints: Boolean,
        resetNests: Boolean,
        resetDefaultPoint: Boolean
    ) {
        require(resetPoints || resetNests || resetDefaultPoint) { "Must at least reset something" }


        applyChange {
            if (resetPoints) {
                _points.value.clear()
                selectedPointsIds.value = emptyList()

                PointStableCache.evictAll()
                deselectAll()
            }
            if (resetNests) {
                _nests.value.clear()
                NestIntersectionShapesPathCache.evictAll()
            }
            if (resetDefaultPoint) {
                defaultPoint.value = Point.defaultSwipePointsValues
            }
        }
    }

    private var grid: MutableMap<GridCase, MutablePoints> = mutableMapOf()
    private var nestGrid: NestGrid = mutableMapOf()
    private var furthestPointGrid: MutableMap<Int, Point?> = mutableMapOf()

    private var lastTarget: Offset = Offset.Zero
    private var searchRadius: Int = 1
    private val gridSize = 150f

    /**
     * I originally wanted to update the caches dynamically when any points is updated,
     * but it was way too many errors that could create caches misses and undefined behavior.
     * Now since the points shouldn't be updated when you usually drag in the main screen
     */
    private fun resetGrids() {
        val points: MutableCollection<Point> = _points.value.values

        for (point in points) {
            point.pos = computePointOffset(point)
        }

        grid = points.groupByTo(mutableMapOf<GridCase, MutablePoints>()) { point ->
            cellKey(point.getPos())
        }

        nestGrid = points.groupByTo(mutableMapOf<Int, MutablePoints>()) { point ->
            point.nestId
        }

        furthestPointGrid = points
            .groupBy { it.nestId }
            .mapValues { (_, nestPoints) ->
                nestPoints.maxByOrNull { it.getPos().getDistanceSquared() }
            }
            .toMutableMap()


        lastTarget = Offset.Zero
        searchRadius = 1
    }

    override fun computeClosestExcept(
        ignoredPointId: Array<Int>?,
        normalizedPos: Offset,
        nestId: Int
    ): Point? {
        val pointsInNestFiltered = getPointsForNest(nestId = nestId, skipSelected = false)
            .filterValues { (ignoredPointId == null || it.id !in ignoredPointId) }

        return when (pointsInNestFiltered.size) {
            0 -> null
            1 -> pointsInNestFiltered.values.first()
            else -> {

                @Suppress("LiftReturnOrAssignment")
                if (lastTarget distanceTo normalizedPos > gridSize) {
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
                                        it.nestId == nestId && (ignoredPointId == null || it.id !in ignoredPointId)
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
                    val dx: Float = normalizedPos.x - p.getPos().x
                    val dy: Float = normalizedPos.y - p.getPos().y
                    dx * dx + dy * dy
                }
            }
        }
    }

    override fun computeClosest(
        normalizedPos: Offset,
        nestId: Int
    ): Point? =
        computeClosestExcept(
            ignoredPointId = null,
            normalizedPos = normalizedPos,
            nestId = nestId
        )


    /**
     * Compute the size of a nest.
     *
     * @param nestId which nest to process
     * @return the furthest point of the nest or `null` if the nest is empty or absent
     */
    private fun computeOuterRadius(nestId: Int): Float? = furthestPointGrid[nestId]?.getPos()?.getDistance()


    override fun resolveLiveNestHit(
        normalizedPos: Offset,
        nestId: Int,
        liveNestScale: Float,
        graceDistancePx: Int?
    ): HitResult {

        val dist = normalizedPos.getDistance()
        val angle360 = normalizedPos.angleDeg()

        // If there's no point in that nest, the HitResult returns an out-of-bounds hit
        val outerRadius = computeOuterRadius(nestId)

        graceDistancePx?.let { graceDist ->
            if (outerRadius == null || outerRadius > 0f && dist > outerRadius + graceDist) {
                return HitResult(
                    selectedPoint = null,
                    isOutsideBounds = true,
                    isInCancelZone = false,
                    angle360 = angle360,
                )
            }
        }

        val isInCancelZone = dist <= findNestById(nestId).cancelZone * density

        // When inside the cancel zone there is no point to select.
        val selectedPoint = if (isInCancelZone) {
            null
        } else {
            computeClosest(normalizedPos, nestId)
        }

        return HitResult(
            selectedPoint = selectedPoint,
            isOutsideBounds = false,
            isInCancelZone = isInCancelZone,
            angle360 = angle360,
        )
    }

    override fun computePointOffset(point: Point): Offset {

        // When the point has no shape, return its offset directly
        val shapeId = point.shapeId ?: run { return point.offset }

        val nest = findNestById(point.nestId)
        val shape = nest.intersectionShapes.find { it.id == shapeId } ?: return point.offset

        return computePointOffsetRealTime(point, shape)
    }

    @Suppress("NOTHING_TO_INLINE")
    override inline fun computePointOffsetRealTime(point: Point, shape: IntersectionShape): Offset {
        val angleRad = (point.offset - shape.offset).angleRad()

        val halfSize = shape.getSize(density).width / 2
        val rotationRad = Math.toRadians(shape.angle.toDouble()).toFloat()

        return shape.offset + computeShapeBoundary(shape.shape, halfSize, angleRad, rotationRad)
    }

    override fun getPointsForNest(
        nestId: Int,
        skipSelected: Boolean
    ): Points {
        val pointsInTheNest: MutablePoints = nestGrid[nestId] ?: return emptyMap()
        if (!skipSelected) return pointsInTheNest.associateBy { it.id }
        return pointsInTheNest
            .filterNotTo(mutableSetOf()) { it.id in selectedPointsIds.value }
            .associateBy { it.id }
    }

    override fun getSelectedShapeIds(nestId: Int): Set<Int> {
        val selectedPointIds = selectedPointsIds.value.takeIf { it.isNotEmpty() } ?: return emptySet()

        return selectedPointIds.mapNotNullTo(mutableSetOf()) { id ->
            val point = findPointById(id) ?: return@mapNotNullTo null
            if (point.shapeId == null || point.nestId != nestId) return@mapNotNullTo null

            point.shapeId
        }
    }


    override fun findPointById(id: Int): Point? = _points.value[id]
    override fun findNestById(id: Int): Nest = _nests.value[id] ?: Nest(id)


    override fun autoSeparate(
        nestId: Int,
        draggedPointId: Int
    ): Boolean {
        return false // TODO
//        val draggedPoint = findPointById(draggedPointId) ?: return false
//        if (getPointsForNest(nestId, false).size < 2) return false
//
//        var hasMoved = false
//
//        /**
//         * Limit the number max of repetitions because otherwise the app could end up being unresponsive
//         * I mean; it shouldn't as I am a pretty good programmer and I anticipated all the edge cases in [computeClosestExcept]
//         * but we never know...
//         */
//        repeat(100) {
//            val draggedPointOffset = computePointOffset(draggedPoint)
//
//            val closest = computeClosestExcept(
//                ignoredPointId = arrayOf(draggedPointId),
//                normalizedPos = draggedPointOffset,
//                nestId = nestId
//            ) ?: return hasMoved
//
//            val closestOffset = computePointOffset(closest)
//            val distanceBetweenPoints = closestOffset distanceTo draggedPointOffset
//            val pointsSizeTogether = (closest.getSize(defaultPoint.value) / 2 + draggedPoint.getSize(defaultPoint.value) / 2).value * density
//
////            logD(POINTS_TAG) {
////                "draggedPoint: $draggedPoint\n" +
////                        "draggedPointOffset: $draggedPointOffset\n" +
////                        "closest: $closest\n" +
////                        "closestOffset! $closestOffset\n" +
////                        "distanceBetweenPoins: $distanceBetweenPoints\n" +
////                        "pointSizeTogether: $pointsSizeTogether"
////            }
//
//            if (distanceBetweenPoints > pointsSizeTogether) return hasMoved
//
//            val angleInRadians = if (distanceBetweenPoints == 0f) {
//                val angle = (0..360).random().toFloat().toRadians().toFloat()
//                logD(POINTS_TAG) { "Took random angle : $angle" }
//                angle
//            } else {
//                /**
//                 * Angle from the [Offset] that represents the vector to transform [closestOffset] into [draggedPointOffset]
//                 */
//                val offset = (draggedPointOffset - closestOffset)
//
//                val angle = offset.angleRad()
//                logD(POINTS_TAG) {
//                    "Took.. -> offset: $offset\n           angle: $angle"
//                }
//                angle
//            }
//
//            val distanceToMove = distanceBetweenPoints.takeIf { it > 0f } ?: (pointsSizeTogether / 2)
//
//            val offsetToMove = Offset(
//                x = distanceToMove * cos(angleInRadians),
//                y = distanceToMove * sin(angleInRadians)
//            )
//
//            logD(POINTS_TAG) {
//                "DistanceToMove: $distanceToMove\n" +
//                        "offset to move: $offsetToMove\n" +
//                        "offset to move angle: ${offsetToMove.angleRad()}\n "
//            }
//
//            editPoint(draggedPoint.id) { old ->
//                old.copy(offset = old.offset - offsetToMove)
//            }
//
//            editPoint(closest.id) { old ->
//                old.copy(offset = old.offset + offsetToMove)
//            }
//
////            logI(POINTS_TAG) {
////                "Separating them by:\n" +
////                        "distanceToMove: $distanceToMove\n" +
////                        "offsetToMove: $offsetToMove\n\n" +
////                        "Points before: ${points.value.map { it.offset }}"
////            }
//
////            logW(POINTS_TAG) {
////                "Points after: ${points.value.map { it.offset }}"
////            }
//
//            hasMoved = true
//
//        }
//        return hasMoved
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
    ): Offset {
//        logD(POINTS_TAG) { "Computing shape: $iconShape" }
        return when (iconShape) {
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
