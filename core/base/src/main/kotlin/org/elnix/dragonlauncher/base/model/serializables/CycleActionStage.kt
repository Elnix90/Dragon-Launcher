package org.elnix.dragonlauncher.base.model.serializables

import kotlinx.serialization.Serializable

/**
 * One timed stage in a Cycle Actions sequence.
 *
 * Stage 0 is implicit (the point's base action, 0 ms). Stages 1..N are stored in
 * [Point.cycleActions] and evaluated continuously during a hold to
 * determine which action fires on release.
 *
 * @param triggerTimeMs Extra milliseconds to hold **after the previous stage** before this stage
 *   becomes current (after finger-down for Stage 1). The runtime sums these into absolute thresholds.
 * @param action        Action executed on release while this stage is active.
 * @param hapticFeedback Haptic pulse played once when transitioning into this stage.
 *                      Null falls back to the point's own haptic setting.
 */
@Serializable
data class CycleActionStage(
    val triggerTimeMs: Int,
    val action: Action,
    val hapticFeedback: CustomHapticFeedback? = null
)
