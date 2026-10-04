package org.elnix.dragonlauncher.base.model.models

import org.elnix.dragonlauncher.base.model.serializables.Point

/**
 * Resolved result of one pointer position against a Live Nest.
 *
 * @property selectedPoint The best matching point, or null when outside angle tolerance or empty.
 * @property isOutsideBounds True when the pointer has moved beyond the outermost ring.
 * @property isInCancelZone True when the drag is in the cancel zone
 */
public data class HitResult(
	val selectedPoint: Point?,
	val isOutsideBounds: Boolean,
	val isInCancelZone: Boolean,
	val angle360: Float
)
