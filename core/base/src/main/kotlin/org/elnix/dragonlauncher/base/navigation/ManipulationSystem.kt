package org.elnix.dragonlauncher.base.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.ktx.applyTransformations
import org.elnix.dragonlauncher.ktx.undoTransformations

@Stable
public class ManipulationSystem(
	public var center: Offset
) {
	public val offset: Animatable<Offset, AnimationVector2D> = Animatable(Offset.Zero, Offset.VectorConverter)
	public val zoom: Animatable<Float, AnimationVector1D> = Animatable(1f)
	public val angle: Animatable<Float, AnimationVector1D> = Animatable(0f)

	public fun canReset(): Boolean {
		val canResetOffset = offset.value != Offset.Zero
		val canResetZoom = zoom.value != 1f
		val canResetRotation = angle.value != 0f

		return canResetOffset || canResetZoom || canResetRotation
	}

	/**
	 * Resets [offset], [zoom] and [angle] from the current [ManipulationSystem] to the default values
	 *
	 * This function needs to be called from a coroutine scope. it does not use the [CoroutineScope] receiver,
	 * because all animations are instant and therefore doesn't need async calls
	 */
	public suspend fun reset() {
		offset.snapTo(Offset.Zero)
		zoom.snapTo(1f)
		angle.snapTo(0f)
	}

	/**
	 * Same as [reset], but animates the reset asynchronously.
	 * All animations are launched in parallel within the provided [scope].
	 */
	public fun resetAnimated(scope: CoroutineScope) {
		scope.launch { offset.animateTo(Offset.Zero, tween(easing = FastOutSlowInEasing)) }
		scope.launch { zoom.animateTo(1f, tween(easing = FastOutSlowInEasing)) }
		scope.launch { angle.animateTo(0f, tween(easing = FastOutSlowInEasing)) }
	}

	/**
	 * Normalize a [org.elnix.dragonlauncher.base.model.serializables.Point.offset]
	 *
	 * It should take a **non-transformed** offset in entry and returns its offset normalized around [Offset.Zero], effectively subtracting [center] from it
	 */
	public fun normalize(offset: Offset): Offset = offset - center

	/**
	 * Undo normalization of a [org.elnix.dragonlauncher.base.model.serializables.Point.offset]
	 * Basically adds [center]
	 * @returns the `transformed` [Offset] corresponding to the offset in local coordinates space, when transformations are applied
	 */
	public fun undoNormalization(offset: Offset): Offset = offset + center

	/**
	 * Transform a pointer position [Offset] into its coordinated, after applying [offset], [zoom] and [angle] transformations.
	 * The resulted [Offset] is meant to be used within the [androidx.compose.ui.graphics.graphicsLayer] block in the Main drawing block
	 */
	public fun transform(offset: Offset): Offset =
		offset.applyTransformations(
			zoom = this.zoom.value,
			offset = this.offset.value,
			angle = this.angle.value
		)

	/**
	 * Undo transformation of the above [transform] function, basically applying the same calculation in the opposite direction.
	 * It provides real screen [Offset] from a transformed [Offset]
	 * It is uses in the [androidx.compose.ui.input.pointer.pointerInput] block in this file, to provide the real screen position of the computed point offset, when user want to snap point to the shapes
	 *
	 * May be removed in the future
	 */
	public fun undoTransformation(offset: Offset): Offset =
		offset.undoTransformations(
			zoom = this.zoom.value,
			offset = this.offset.value,
			angle = this.angle.value
		)

	/** [undoNormalization] then [undoTransformation] */
	public fun undoBoth(offset: Offset): Offset = undoTransformation(undoNormalization(offset))
}
