package org.elnix.dragonlauncher.ui.settings.points

import android.app.Application
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.Constants.Settings.COLLIDING_SHAPE_THRESHOLD_PX
import org.elnix.dragonlauncher.base.Constants.Settings.TOUCH_THRESHOLD_PX
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.navigation.ManipulationSystem
import org.elnix.dragonlauncher.ktx.distanceTo
import org.elnix.dragonlauncher.points.NestsNavigationService
import org.elnix.dragonlauncher.points.PointsService
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore

/**
 * The temporary position of a selected point while it is being dragged or edited in the screen.
 */
data class TempPos(
	val shapeId: MutableState<Int?>,
	val offset: Animatable<Offset, AnimationVector2D>
)

@HiltViewModel
@Stable
class PointsSettingsViewModel
	@Inject
	constructor(
		application: Application,
		private val pointsService: PointsService,
		private val nestsNavigationService: NestsNavigationService
	) : AndroidViewModel(application) {
		val center = SettingFlow(Offset.Zero)
		val manipulationSystem = ManipulationSystem(center.value)

		/** The point currently hovered by the finger, if it can be merged into. */
		val closestHoveredPoint = mutableStateOf<Point?>(null)
		val closestHoveredTempOffset = mutableStateOf<Offset?>(null)
		val ableToLaunchHoverAction = mutableStateOf(false)

		val showMoreSheet = mutableStateOf(false)
		val showGambleDialog = mutableStateOf(false)
		val showEditDefaultPoint = mutableStateOf(false)
		val showAddDialog = mutableStateOf(false)
		val showEditDialog = mutableStateOf<Int?>(null)
		val showNestManagementDialog = mutableStateOf(false)
		val showResetPointsAndNestsDialog = mutableStateOf(false)

		/** The queue of apps to place one by one, non-empty in manual placement mode. */
		val manualPlacementQueue = mutableStateOf<List<Action>>(emptyList())
		val isInManualPlacementMode: Boolean
			get() = manualPlacementQueue.value.isNotEmpty()

		val isDragging = mutableStateOf(false)

		/** The temporary (animated) positions of the selected points. */
		val selectedPointTempOffset: SnapshotStateMap<Int, TempPos> = mutableStateMapOf()

		init {
			// Load the settings so we can access them through their `.value` property afterwards
			viewModelScope.launch {
				UiSettingsStore.snapPoints.load(application)
				UiSettingsStore.snapPointsToShapes.load(application)
				UiSettingsStore.allowFreePoints.load(application)
				UiSettingsStore.multiSelectPoints.load(application)
			}
		}

		/** Current value of the [UiSettingsStore.snapPoints] setting. */
		val snapPoints: Boolean
			get() = UiSettingsStore.snapPoints.value

		private val snapPointsToShapes: Boolean
			get() = UiSettingsStore.snapPointsToShapes.value

		private val allowFreePoints: Boolean
			get() = UiSettingsStore.allowFreePoints.value

		private val multiSelectPoints: Boolean
			get() = UiSettingsStore.multiSelectPoints.value

		private val nestId: Int
			get() = nestsNavigationService.currentNestId.value

		/**
		 * The number of grid cells to draw, computed from the current [zoom] and [offset] transformations.
		 */
		fun computeCellNumber(cellSizePx: Float, screenMaxDimensionPx: Float): Int {
			val dist = manipulationSystem.offset.value.getDistance()
			return (((dist + screenMaxDimensionPx) / cellSizePx) * 1.5 * (1 / manipulationSystem.zoom.value)).toInt().fastCoerceAtMost(5000)
		}

		/**
		 * Selects a point and registers its [TempPos] so the screen can animate it towards its position.
		 */
		fun select(id: Int) {
			if (multiSelectPoints) {
				pointsService.select(id)
			} else {
				pointsService.selectOnyOne(id)
				selectedPointTempOffset.clear()
			}

			val point = pointsService.findPointById(id) ?: return
			selectedPointTempOffset[id] =
				TempPos(
					shapeId = mutableStateOf(point.shapeId),
					offset = Animatable(manipulationSystem.undoBoth(point.getPos()), Offset.VectorConverter)
				)
		}

		/**
		 * Deselects a point and removes its [TempPos].
		 */
		fun deselect(id: Int) {
			pointsService.deselect(id)
			selectedPointTempOffset -= id
		}

		/**
		 * Computes the new offset for the selected point.
		 *
		 * When [snapPoints] is `false`, it simply skips the computation and returns the normalized offset.
		 * Otherwise, the function checks for each potential shapes the closest and determines the most probable shape to collide with
		 *
		 * @param point which point to move
		 * @param normalizedOffset the offset in which the gesture ends (normalized)
		 * @param shapes the intersection shapes of the currently displayed nest
		 * @return the optional [shapeId][Int] the point will take if dropped here
		 */
		fun computePointMoved(
			point: Point,
			normalizedOffset: Offset,
			shapes: Set<IntersectionShape>,
			forceSnap: Boolean = false
		): Int? {
			// Early return: if user don't want to snap to shapes, no need to compute them as it is a bit expensive
			if (allowFreePoints && !snapPointsToShapes && !forceSnap) return null

			if (shapes.isEmpty()) return null

			val (minOffset, shapeId) =
				shapes
					.map { shape ->
						pointsService.computePointOffset(
							point.copy(
								offset = normalizedOffset,
								shapeId = shape.id
							)
						) to shape.id
					}.minBy { (offset, _) -> offset distanceTo normalizedOffset }

			if (forceSnap) return shapeId

			/**
			 * The distance between the landing [Offset] and the finger's position
			 */
			val minOffsetDistanceToNormalized: Float = minOffset distanceTo normalizedOffset

			return if (minOffsetDistanceToNormalized < COLLIDING_SHAPE_THRESHOLD_PX) {
				shapeId
			} else {
				null
			}
		}

		val offset = manipulationSystem.offset
		val angle = manipulationSystem.angle
		val zoom = manipulationSystem.zoom

		/**
		 * Holds an Offset and provides helper functions and value to manage it in the [PointsSettingsScreen] scope.
		 */
		inner class TransformedOffset(
			/**
			 * Original offset, in normal screen coordinates
			 * It will be transformed to give the actual useful values
			 */
			private val offset: Offset
		) {
			/**
			 * Transformed offset, represents the coordinated in space of the [offset] after undoing the
			 * transformations of [angle], [zoom], and [offset] that are only for visual in the settings screen
			 */
			val transformedOffset: Offset by lazy {
				manipulationSystem.transform(this.offset)
			}

			/**
			 * Represents the offset of the point, if you do not account for both the [angle], [zoom], and [offset] transformations and the [center]
			 * in the middle of the screen.
			 *
			 * ### **It's the offset you want to save into the points property**
			 * as it can be interpreted by the [org.elnix.dragonlauncher.points.PointsService] and be
			 * converted back to screen coordinates.
			 */
			val normalizedOffset: Offset by lazy {
				manipulationSystem.normalize(this.transformedOffset)
			}

			/**
			 * Computes the closest point relative to this [transformedOffset].
			 * @see org.elnix.dragonlauncher.points.PointsService.computeClosest
			 */
			val bestP: Point? by lazy {
				pointsService.computeClosest(this.normalizedOffset, nestId)
			}

			private val distance: Float by lazy {
				val betsPOffset = this.bestP?.getPos() ?: return@lazy Float.MAX_VALUE
				betsPOffset distanceTo this.normalizedOffset
			}

			/**
			 * Whether the distance to the closest point is inferior to an arbitrary [TOUCH_THRESHOLD_PX].
			 *
			 * TODO Make this threshold dependent on the [zoom]
			 */
			val distanceSmallEnough: Boolean by lazy { distance <= TOUCH_THRESHOLD_PX }

			/** Executes [block] if [distanceSmallEnough] */
			inline infix fun ifDistanceIsSmallEnough(block: () -> Point?): Point? = if (distanceSmallEnough) block() else null

			override fun toString(): String =
				"TR(\n" +
					"   offset = ${this.offset}\n" +
					"   transformedOffset = $transformedOffset\n" +
					"   normalizedOffset = $normalizedOffset\n" +
					"   bestP = $bestP\n" +
					"   distance = $distance${if (!distanceSmallEnough) " (Too Far!)" else ""}\n" +
					")"
		}
	}
