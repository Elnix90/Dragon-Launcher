package org.elnix.dragonlauncher.points

import android.content.Context
import androidx.compose.ui.geometry.Offset
import io.github.elnix90.logging.POINTS_TAG
import io.github.elnix90.logging.logD
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
import org.elnix.dragonlauncher.ktx.angleDeg
import org.elnix.dragonlauncher.ktx.angleRad
import org.elnix.dragonlauncher.ktx.distanceTo
import org.elnix.dragonlauncher.ktx.groupByTo
import org.elnix.dragonlauncher.ktx.toRadians
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

    public val recomposeTRigger: SettingFlow<Int>

    /**
     * Selected points ids, a [List] of all selected points, by order of selection.
     *
     * A *File* according to M.Morlong, thanks!
     */
    public val selectedPointsIds: SettingFlow<List<Int>>

    public val undoRedo: UndoRedoManager

    public fun addPoint(select: Boolean = true, newPoint: (Int) -> Point): Int
    public fun removePoint(id: Int): Boolean
    public fun editPoint(id: Int, editedPoint: (Point) -> Point): Boolean

    public fun addNest(nestId: Int? = null): Int
    public fun removeNest(id: Int): Boolean
    public fun editNest(id: Int, editedNest: (Nest) -> Nest): Boolean

    public fun editDefaultPoint(newDefaultPoint: Point)


    public fun findPointById(id: Int): Point?
    public fun findNestById(id: Int): Nest
    public fun findNestByIdOrNull(id: Int): Nest?


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
    public fun deselectAll()


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
        nestId: Int,
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

    public fun getFurthestPoint(nestId: Int): Point?

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
    private typealias GridMap = MutableMap<GridCase, MutablePoints>
    private typealias NestGrid = MutableMap<Int, MutablePoints>
    private typealias FurthestGrid = MutableMap<Int, Point?>

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val defaultPoint: SettingFlow<Point> = SettingFlow(dummySwipePoint())

    override val points: SettingFlow<Points> = SettingFlow(emptySet())

    override val nests: SettingFlow<Nests> = SettingFlow(emptySet())

    override val recomposeTRigger: SettingFlow<Int> = SettingFlow(0)
    override val selectedPointsIds: SettingFlow<List<Int>> = SettingFlow(emptyList())

    override fun select(id: Int) {
        val newSel: Point? = findPointById(id)
        val currentSelectedIds: List<Int> = selectedPointsIds.value

        when {
            // Deselect all if newSel is null and something is selected
            // I did not put the if below in the same line because of a failing smart cast to non-nullable point
            newSel == null -> {
                // Only deselect if the list isn't already empty to avoid undoRedo overhead
                if (currentSelectedIds.isNotEmpty()) {
                    applyChange {
                        selectedPointsIds.value = emptyList()
                    }
                }
            }

            // Deselect newSel if already selected
            newSel.id in currentSelectedIds -> {
                applyChange {
                    selectedPointsIds.value = currentSelectedIds - newSel.id
                }
            }

            // Select newSel (add to set or create new set)
            else -> {
                applyChange {
                    selectedPointsIds.value = currentSelectedIds + (newSel.id)
                }
            }
        }
        recomposeTRigger.value++
    }

    override fun deselect(id: Int) {
        if (id !in selectedPointsIds.value) return
        selectedPointsIds.value -= id
        recomposeTRigger.value++
    }

    override fun deselectAll() {
        selectedPointsIds.value = emptyList()
        recomposeTRigger.value++
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
                    applyChange {
                        selectedPointsIds.value = emptyList()
                    }
                }
            }

            else -> {
                applyChange {
                    selectedPointsIds.value = listOf(newSel.id)
                }
            }
        }
        recomposeTRigger.value++
    }

    override val undoRedo: UndoRedoManager = UndoRedoManager(
        stacks = arrayOf(
            UndoRedoStack(
                snapshot = { points.value.map { it.copy() } },
                restore = { points ->
                    set(newPoints = points.toSet())

                    selectedPointsIds.value = points.map { it.id }.filter { it in selectedPointsIds.value }
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
                snapshot = { selectedPointsIds.value },
                restore = { selectedPointsIds.value = it }
            )
        ),
        scope = scope
    )

    private inline fun applyChange(mutator: () -> Unit) {
        undoRedo.applyChange(mutator)
        recomposeTRigger.value++
    }

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

        applyChange { points.value += newPoint }

        if (select) select(newId)
        resetGrids()
        return newId
    }

    override fun removePoint(id: Int): Boolean {
        val pointToRemove = points.value.find { it.id == id } ?: return false

        applyChange { points.value -= pointToRemove }

        resetGrids()
        return true
    }

    override fun editPoint(id: Int, editedPoint: (Point) -> Point): Boolean {
        val oldPoint = points.value.find { it.id == id } ?: return false
        val editedPoint = editedPoint(oldPoint)

        applyChange {
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
        applyChange { nests.value += newNest }
        return newId
    }

    override fun removeNest(id: Int): Boolean {
        val nestToDelete = nests.value.find { it.id == id } ?: return false
        applyChange { nests.value -= nestToDelete }
        return true
    }

    override fun editNest(id: Int, editedNest: (Nest) -> Nest): Boolean {
        val oldNest = nests.value.find { it.id == id } ?: return false
        applyChange {
            nests.value -= oldNest
            nests.value += editedNest(oldNest)
        }
        return true
    }

    override fun editDefaultPoint(newDefaultPoint: Point) {
        applyChange { set(newDefaultPoint = newDefaultPoint) }
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
        recomposeTRigger.value++
    }


    override fun reset(
        resetPoints: Boolean,
        resetNests: Boolean,
        resetDefaultPoint: Boolean
    ) {
        require(resetPoints || resetNests || resetDefaultPoint) { "Must at least reset something" }

        applyChange {
            if (resetPoints) {
                points.value = emptySet()
                selectedPointsIds.value = emptyList()
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
        recomposeTRigger.value++
    }

    private suspend fun loadPoints() {
        points.value = PointsJson.decode<Points>(PointsSettingsStore.jsonSetting.get(ctx), emptySet())
        resetGrids()
        recomposeTRigger.value++
    }

    private suspend fun loadNests() {
        nests.value = NestJson.decode<Nests>(NestsSettingsStore.jsonSetting.get(ctx), emptySet())
        recomposeTRigger.value++
    }

    private suspend fun loadDefaultPoint() {
        defaultPoint.value = PointJson.decode(DefaultPointSettingsStore.jsonSetting.get(ctx), Point.defaultSwipePointsValues)
        recomposeTRigger.value++
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
        grid = points.value.groupByTo(mutableMapOf<GridCase, MutablePoints>()) { point ->
            cellKey(computePointOffset(point))
        }

        nestGrid = points.value.groupByTo(mutableMapOf<Int, MutablePoints>()) { point ->
            point.nestId
        }


        furthestPointGrid = points.value
            .groupBy { it.nestId }
            .mapValues { (_, nestPoints) ->
                nestPoints.maxByOrNull { computePointOffset(it).getDistanceSquared() }
            }
            .toMutableMap()


        lastTarget = Offset.Zero
        searchRadius = 1
    }


    override fun getFurthestPoint(nestId: Int): Point? = furthestPointGrid[nestId]

    override fun computeClosestExcept(
        ignoredPointId: Array<Int>?,
        normalizedPos: Offset,
        nestId: Int
    ): Point? {
        val pointsInNestFiltered = getPointsForNest(nestId = nestId, skipSelected = false)
            .filter { (ignoredPointId == null || it.id !in ignoredPointId) }

        return when (pointsInNestFiltered.size) {
            0 -> null
            1 -> pointsInNestFiltered.first()
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
                    val dx: Float = normalizedPos.x - p.offset.x
                    val dy: Float = normalizedPos.y - p.offset.y
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


    override fun resolveLiveNestHit(
        normalizedPos: Offset,
        nestId: Int,
        liveNestScale: Float,
        graceDistancePx: Int
    ): HitResult {
        val dist = normalizedPos.getDistance()
        val angle360 = normalizedPos.angleDeg()

        // If there's no point in that nest, the HitResult returns a out-of-bounds hit
        val outerRadius = getFurthestPoint(nestId)?.offset?.getDistance()

        graceDistancePx.takeIf { it > -1 }?.let {
            if (outerRadius == null || outerRadius > 0f && dist > outerRadius + graceDistancePx) {
                return HitResult(
                    selectedPoint = null,
                    isOutsideBounds = true,
                    isInCancelZone = false,
                    angle360 = angle360,
                )
            }
        }

        val nest = findNestById(nestId)
        val isInCancelZone = dist <= nest.cancelZone

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
        nestId: Int,
        skipSelected: Boolean
    ): Points {
        val pointsInTheNest: MutablePoints = nestGrid[nestId] ?: return emptySet()
        if (!skipSelected) return pointsInTheNest

        val selectedPoints = selectedPointsIds.value

        return pointsInTheNest.filterNotTo(mutableSetOf()) { it.id in selectedPoints }
    }


    override fun findPointById(id: Int): Point? = points.value.find { it.id == id }

    override fun findNestById(id: Int): Nest = findNestByIdOrNull(id) ?: Nest()
    override fun findNestByIdOrNull(id: Int): Nest? = nests.value.find { it.id == id }


    private val density = ctx.resources.displayMetrics.density

    override fun autoSeparate(
        nestId: Int,
        draggedPointId: Int
    ): Boolean {
        val draggedPoint = findPointById(draggedPointId) ?: return false
        if (points.value.size < 2) return false

        var hasMoved = false

        /**
         * Limit the number max of repetitions because otherwise the app could end up being unresponsive
         * I mean; it shouldn't as I am a pretty good programmer and I anticipated all the edge cases in [computeClosestExcept]
         * but we never know...
         */
        repeat(100) {
            val draggedPointOffset = computePointOffset(draggedPoint)

            val closest = computeClosestExcept(
                ignoredPointId = arrayOf(draggedPointId),
                normalizedPos = draggedPointOffset,
                nestId = nestId
            ) ?: return hasMoved

            val closestOffset = computePointOffset(closest)
            val distanceBetweenPoints = closestOffset distanceTo draggedPointOffset
            val pointsSizeTogether = (closest.getSize(defaultPoint.value) / 2 + draggedPoint.getSize(defaultPoint.value) / 2).value * density

//            logD(POINTS_TAG) {
//                "draggedPoint: $draggedPoint\n" +
//                        "draggedPointOffset: $draggedPointOffset\n" +
//                        "closest: $closest\n" +
//                        "closestOffset! $closestOffset\n" +
//                        "distanceBetweenPoins: $distanceBetweenPoints\n" +
//                        "pointSizeTogether: $pointsSizeTogether"
//            }

            if (distanceBetweenPoints > pointsSizeTogether) return hasMoved

            val angleInRadians = if (distanceBetweenPoints == 0f) {
                val angle = (0..360).random().toFloat().toRadians()
                logD(POINTS_TAG) { "Took random angle : $angle" }
                angle
            } else {
                /**
                 * Angle from the [Offset] that represents the vector to transform [closestOffset] into [draggedPointOffset]
                 */
                val offset = (draggedPointOffset - closestOffset)

                val angle = offset.angleRad()
                logD(POINTS_TAG) {
                    "Took.. -> offset: $offset\n           angle: $angle"
                }
                angle
            }

            val distanceToMove = distanceBetweenPoints.takeIf { it > 0f } ?: (pointsSizeTogether / 2)

            val offsetToMove = Offset(
                x = distanceToMove * cos(angleInRadians).toFloat(),
                y = distanceToMove * sin(angleInRadians).toFloat()
            )

            logD(POINTS_TAG) {
                "DistanceToMove: $distanceToMove\n" +
                        "offset to move: $offsetToMove\n" +
                        "offset to move angle: ${offsetToMove.angleRad()}\n "
            }

            editPoint(draggedPoint.id) { old ->
                old.copy(offset = old.offset - offsetToMove)
            }

            editPoint(closest.id) { old ->
                old.copy(offset = old.offset + offsetToMove)
            }

//            logI(POINTS_TAG) {
//                "Separating them by:\n" +
//                        "distanceToMove: $distanceToMove\n" +
//                        "offsetToMove: $offsetToMove\n\n" +
//                        "Points before: ${points.value.map { it.offset }}"
//            }

//            logW(POINTS_TAG) {
//                "Points after: ${points.value.map { it.offset }}"
//            }

            hasMoved = true

        }
        return hasMoved
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
