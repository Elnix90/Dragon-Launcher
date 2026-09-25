package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import kotlin.math.PI
import kotlin.math.abs

/**
 * Similar to [detectTransformGestures]
 *
 * Handles only the real drag gestures, skips the clicks, and return the number of down touches for the [onGesture] lambda
 */
suspend fun PointerInputScope.detectTransformGesturesWithStartAndEnd(
	panZoomLock: Boolean = false,
	onGestureEnd: () -> Unit,
	onGesture: (fingerNumbers: Int, centroid: Offset, pan: Offset, zoom: Float, rotation: Float) -> Unit
) {
	awaitEachGesture {
		var rotation = 0f
		var zoom = 1f
		var pan = Offset.Zero
		var pastTouchSlop = false
		val touchSlop = viewConfiguration.touchSlop
		var lockedToPanZoom = false

		val down = awaitFirstDown(requireUnconsumed = false)
		var drag: PointerInputChange?

		// I'm really proud of this code too
		do {
			drag =
				awaitTouchSlopOrCancellation(down.id) { change, over ->
					change.consume()
				}
		} while (drag != null && !drag.isConsumed)

		if (drag != null) {
			do {
				val event = awaitPointerEvent()
				val canceled = event.changes.fastAny { it.isConsumed }
				if (!canceled) {
					val zoomChange = event.calculateZoom()
					val rotationChange = event.calculateRotation()
					val panChange = event.calculatePan()

					if (!pastTouchSlop) {
						zoom *= zoomChange
						rotation += rotationChange
						pan += panChange

						val centroidSize = event.calculateCentroidSize(useCurrent = false)
						val zoomMotion = abs(1 - zoom) * centroidSize
						val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
						val panMotion = pan.getDistance()

						if (
							zoomMotion > touchSlop ||
							rotationMotion > touchSlop ||
							panMotion > touchSlop
						) {
							pastTouchSlop = true
							lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
						}
					}

					if (pastTouchSlop) {
						val centroid = event.calculateCentroid(useCurrent = false)
						val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange
						if (effectiveRotation != 0f || zoomChange != 1f || panChange != Offset.Zero) {
							onGesture(event.changes.size, centroid, panChange, zoomChange, effectiveRotation)
						}
						event.changes.fastForEach {
							if (it.positionChanged()) {
								it.consume()
							}
						}
					}
				}
			} while ((!canceled && event.changes.fastAny { it.pressed }))

			onGestureEnd()
		}
	}
}
