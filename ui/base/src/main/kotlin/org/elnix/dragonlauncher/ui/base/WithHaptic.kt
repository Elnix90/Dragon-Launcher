package org.elnix.dragonlauncher.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.ui.base.compositionlocals.LocalDisableHapticFeedbackGlobally

/**
 * Returns a stable `() -> Unit` lambda that performs haptic feedback before invoking [block].
 *
 * Haptic feedback is skipped if the user has disabled it globally via [BehaviorSettingsStore].
 * The returned lambda is stable across recompositions as long as [type], the haptic feedback
 * handler, and the global haptic setting remain unchanged.
 *
 * @param type The [HapticFeedbackType] to perform on invocation. Defaults to [HapticFeedbackType.ContextClick].
 * @param block The action to execute after haptic feedback.
 * @return A stable `() -> Unit` lambda wrapping [block] with haptic feedback.
 */
@Composable
fun withHaptic(
    type: HapticFeedbackType = HapticFeedbackType.ContextClick,
    block: () -> Unit
): () -> Unit {
    val haptic = LocalHapticFeedback.current
    val disableHapticFeedbackGlobally = LocalDisableHapticFeedbackGlobally.current
    val latestBlock = rememberUpdatedState(block)

    return retain(type, haptic, disableHapticFeedbackGlobally) {
        {
            if (!disableHapticFeedbackGlobally) haptic.performHapticFeedback(type)
            latestBlock.value()
        }
    }
}

/**
 * Returns a stable `(T) -> Unit` lambda that performs haptic feedback before invoking [block],
 * discarding the parameter passed by the caller.
 *
 * Useful when an API requires a single-argument callback (e.g. `onCheckedChange: (Boolean) -> Unit`)
 * but the argument value is irrelevant to the action being performed.
 *
 * Haptic feedback is skipped if the user has disabled it globally via [BehaviorSettingsStore].
 * The returned lambda is stable across recompositions as long as [type] and the global haptic
 * setting remain unchanged.
 *
 * Example:
 * ```
 * onCheckedChange = withHapticParam {
 *     onCheck(entry)
 * }
 * ```
 *
 * @param T The type of the ignored parameter.
 * @param type The [HapticFeedbackType] to perform on invocation. Defaults to [HapticFeedbackType.ContextClick].
 * @param block The action to execute after haptic feedback. The caller's parameter is discarded.
 * @return A stable `(T) -> Unit` lambda wrapping [block] with haptic feedback.
 */
@Composable
fun <T> withHapticParam(
    type: HapticFeedbackType = HapticFeedbackType.ContextClick,
    block: () -> Unit
): (T) -> Unit {
    val inner = withHaptic(type, block)
    return retain(inner) {
        { _: T -> inner() }
    }
}
