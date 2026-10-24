package org.elnix.dragonlauncher.ui.remembers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import io.github.elnix90.shared.ui.base.asState
import io.github.elnix90.shared.ui.base.compositionlocals.LocalDisableHapticFeedbackGlobally
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.CycleActionStage
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.defaultHapticFeedback
import kotlin.time.Duration.Companion.milliseconds

/**
 * For each extra stage, [CycleActionStage.triggerTimeMs] is the **additional** hold time after the
 * previous stage before that stage becomes current. This returns absolute thresholds from
 * finger-down: cumulative sums of those delays.
 */
private fun cumulativeTriggerThresholdsMs(stages: List<CycleActionStage>): List<Long> {
	var acc = 0L
	return stages.map { stage ->
		acc += stage.triggerTimeMs.toLong().coerceAtLeast(1)
		acc
	}
}

/**
 * Snapshot of Cycle Actions state returned per recomposition.
 *
 * @property isActive True while the finger is down on a point with Cycle Actions configured.
 * @property currentStageIndex 0 = base action; 1..N = timed stages; N+1 = "Loop Over" when loop is on.
 * @property currentStageAction Action for the current extra stage or Loop Over (last stage's action).
 *   Null when on the base stage.
 * @property clear Resets all cycle state; call after a launch or after a cancel.
 */
data class CycleActionsState(
	val isActive: Boolean,
	val currentStageIndex: Int,
	val currentStageAction: Action?,
	val clear: () -> Unit
)

/**
 * Composable controller that manages the Cycle Actions elapsed timer, stage derivation,
 * per-stage haptic pulses, optional loop wrap with a "Loop Over" tail, and release resolution.
 *
 * Each stage's [CycleActionStage.triggerTimeMs] is an **extra** hold duration after the previous
 * stage (finger-down for the first stage); thresholds are cumulative sums for comparison with
 * elapsed time since finger-down.
 *
// * Runs independently of [rememberLiveNestControllerStack]. The overlay passes null for [currentAction]
// * when Live Nest is active, which automatically pauses the timer and deactivates cycle state.
 *
 * @param currentAction Currently selected point on the main nest, or null if Live Nest is active.
 * @param isDragging    True while a finger is on screen.
 */
@Composable
fun rememberCycleActionsController(
	pointsViewModel: PointsViewModel = activityViewModel(),
	currentAction: Point?,
	isDragging: Boolean
): CycleActionsState {
	val ctx = LocalContext.current
	val pointsService = pointsViewModel.pointsService
	val defaultPoint by pointsService.defaultPoint.asState()

	val disableHapticFeedbackGlobally = LocalDisableHapticFeedbackGlobally.current

	val stages: List<CycleActionStage>? = currentAction?.cycleActions

	var currentStageIndex by remember { mutableIntStateOf(0) }

	LaunchedEffect(
		currentAction?.id,
		isDragging,
		stages,
		currentAction?.cycleActionsLoopDelayMs
	) {
		// When isDragging goes false, do NOT reset the published stage here: the swipe service reads
		// it on release to decide the launch, and clears it itself once consumed.
		if (!isDragging) return@LaunchedEffect

		// New gesture or hovered point changed: start fresh.
		currentStageIndex = 0
		pointsService.cycleStageAction.value = null
		if (stages.isNullOrEmpty()) return@LaunchedEffect

		val loopDelayMs =
			(
				currentAction.cycleActionsLoopDelayMs
					?: defaultPoint.cycleActionsLoopDelayMs
					?: Point.defaultCycleActionsLoopDelayMs
			).toLong()
				.coerceAtLeast(1L)

		val loopEnabled = loopDelayMs != -1L

		val cumulativeMs = cumulativeTriggerThresholdsMs(stages)
		val tLastEntry = cumulativeMs.last()
		val cycleLen = tLastEntry + loopDelayMs

		val startTime = System.currentTimeMillis()
		var lastCycleCount = -1L
		var lastFiredStageIndex = -1

		while (isActive) {
			val now = System.currentTimeMillis()
			val totalElapsed = now - startTime

			val cycleCount = if (loopEnabled) totalElapsed / cycleLen else 0L
			val eff = if (loopEnabled) totalElapsed % cycleLen else totalElapsed

			if (loopEnabled && cycleCount != lastCycleCount) {
				lastCycleCount = cycleCount
			}

			val newIndex = (cumulativeMs.indexOfLast { eff >= it } + 1).coerceAtMost(stages.size)

			if (newIndex != currentStageIndex) {
				currentStageIndex = newIndex

				// Publish the staged action so the swipe service, which performs the launch on
				// release, can see which stage the finger is resting on.
				pointsService.cycleStageAction.value = stages.getOrNull(newIndex - 1)?.action

				// Haptic feedback for stage entries `1..N` and loop-wrap to Base (0).
				if (!disableHapticFeedbackGlobally && newIndex != lastFiredStageIndex) {
					val haptic =
						when (newIndex) {
							in 1..stages.size -> {
								stages[newIndex - 1].hapticFeedback ?: defaultHapticFeedback()
							}

							0 if loopEnabled && lastFiredStageIndex == stages.size -> {
								// Light haptic when the loop wraps back to the base action.
								defaultHapticFeedback()
							}

							else -> {
								null
							}
						}

					haptic?.perform(ctx)
					lastFiredStageIndex = newIndex
				}
			}

			// If looping is disabled, we stay in the last stage after it is reached.
			if (!loopEnabled && newIndex >= stages.size) break

			delay(16L.milliseconds)
		}
	}

	val clear: () -> Unit = remember { { currentStageIndex = 0 } }

	val safeStageIndex = currentStageIndex.coerceIn(0, stages?.size ?: 0)

	val currentStageAction: Action? =
		when {
			stages.isNullOrEmpty() -> null
			safeStageIndex == 0 -> null
			safeStageIndex in 1..stages.size -> stages[safeStageIndex - 1].action
			else -> null
		}

	return CycleActionsState(
		isActive = isDragging && !stages.isNullOrEmpty(),
		currentStageIndex = safeStageIndex,
		currentStageAction = currentStageAction,
		clear = clear
	)
}
