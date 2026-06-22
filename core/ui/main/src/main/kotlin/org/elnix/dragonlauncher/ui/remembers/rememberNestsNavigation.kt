package org.elnix.dragonlauncher.ui.remembers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import org.elnix.dragonlauncher.base.model.serializables.Nest
import io.github.elnix90.logging.NESTS_TAG
import io.github.elnix90.logging.logD
import io.github.elnix90.logging.logW
import org.elnix.dragonlauncher.models.PointViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
/**
 * Remembers and manages navigation state between [Nest]s.
 *
 * Navigation is modeled as a simple stack of nest ids:
 *
 * - The last element in the stack represents the currently visible nest.
 * - If the stack is empty, the root nest (id = 0) is used.
 *
 * The state is fully reactive:
 * - When the navigation stack changes, the current nest id updates.
 * - When either the current nest id or the [nests] list changes,
 *   the resolved [Nest] updates.
 *
 * @param nests A reactive list of available nests. Must be a state-backed list
 *              (e.g., SnapshotStateList) for proper recomposition.
 *
 * @return A [NestNavigationState] exposing the current nest and navigation actions.
 */
@Composable
fun rememberNestNavigation(
    pointsViewModel: PointViewModel = activityViewModel()
): NestNavigationState {

    val nests by pointsViewModel.nests.collectAsState()

    /**
     *  Navigation stack holding visited nest ids.
     *  The last element represents the current position.
     */
    val nestsStack = remember { mutableStateListOf<Int>() }

    /**
     * Current nest id derived from the navigation stack.
     *
     * Falls back to root (0) when the stack is empty.
     */
    val nestId by remember {
        derivedStateOf {
            nestsStack.lastOrNull() ?: 0
        }
    }

    /**
     * Currently active [Nest].
     *
     * Recomputes when:
     * - The navigation stack changes (via [nestId])
     * - The [nests] list content changes
     *
     * If no matching nest is found, a fallback instance is created.
     */
    val currentNest by remember(nests) {
        logD(NESTS_TAG) { "Nest list size: ${nests.size}" }
        derivedStateOf {
            nests.find { it.id == nestId }
                ?: run {
                    logW(NESTS_TAG) { "Current nest (id = $nestId) not found among ${nests.size} nests, in rememberNestNavigation" }
                    Nest(nestId)
                }
        }
    }

    return NestNavigationState(
        currentNest = currentNest,

        /**
         * Navigates one level up in the stack.
         * Does nothing if already at root.
         */
        goBack = {
            if (nestsStack.isNotEmpty()) {
                nestsStack.removeAt(nestsStack.lastIndex)
            }
        },

        /**
         * Navigates to the given nest id.
         * Adds it to the top of the navigation stack
         * if it differs from the current id.
         */
        goToNest = { newNestId ->
            if (newNestId != nestId) {
                nestsStack.add(newNestId)
            }
        },

        /**
         * Clears the entire navigation stack,
         * returning to the root nest.
         */
        clearStack = {
            nestsStack.clear()
        }
    )
}

data class NestNavigationState(
    val currentNest: Nest,
    val goBack: () -> Unit,
    val goToNest: (Int) -> Unit,
    val clearStack: () -> Unit
)
