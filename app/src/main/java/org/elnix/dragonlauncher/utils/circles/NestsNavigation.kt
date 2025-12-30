package org.elnix.dragonlauncher.utils.circles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.elnix.dragonlauncher.data.CircleNest

data class NestNavigationState(
    val nestId: Int,
    val currentNest: CircleNest,
    val goBack: () -> Unit,
    val goToNest: (Int) -> Unit
)


/**
 * Holds and manages CircleNest navigation state.
 */
@Composable
fun rememberNestNavigation(
    nests: List<CircleNest>,
    initialNestId: Int = 0
): NestNavigationState {
    var nestId by remember { mutableIntStateOf(initialNestId) }

    val currentNest = remember(nestId, nests) {
        nests.find { it.id == nestId }
            ?: CircleNest(id = nestId, parentId = 0)
    }

    return NestNavigationState(
        nestId = nestId,
        currentNest = currentNest,
        goBack = {
            if (currentNest.parentId != nestId) {
                nestId = currentNest.parentId
            }
        },
        goToNest = { newNestId ->
            if (newNestId != nestId) {
                nestId = newNestId
            }
        }
    )
}
