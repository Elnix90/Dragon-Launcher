package org.elnix.dragonlauncher.ui.base.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.base.animation.slideInVerticalBouncy
import org.elnix.dragonlauncher.ui.base.animation.slideInVerticalBouncyUp
import org.elnix.dragonlauncher.ui.base.animation.slideOutVerticalBouncy
import org.elnix.dragonlauncher.ui.base.animation.slideOutVerticalBouncyUp

/**
 * A lazy column with an auto-hiding scroll indicator icon.
 *
 * Renders a scrollable list and automatically shows/hides a down arrow icon
 * at the bottom when more content is available to scroll. The indicator uses
 * a bouncy entrance/exit animation.
 *
 * @param T The type of items in the list
 * @param items The list of items to display
 * @param modifier Modifier applied to the LazyColumn
 * @param verticalArrangement The list's vertical arrangement
 * @param content Composable lambda to render each item
 */
@Composable
fun <T> LazyColumnWithScrollIndicator(
    items: List<T>,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable (T) -> Unit
) {
    val state = rememberLazyListState()

    Box {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = verticalArrangement,
            state = state,
        ) {
            items(
                items = items,
                key = { it.hashCode() }
            ) { type ->
                content(type)
            }
        }
        VerticalScrollIndicator(state.canScrollForward)
    }
}

/**
 * A scroll indicator icon that appears at the bottom when content can be scrolled.
 *
 * Shows a down arrow icon with bouncy slide-in/out animations. Only visible
 * when [visible] is true. Must be used within a [BoxScope].
 *
 * @param visible Whether to display the scroll indicator
 */
@Composable
fun BoxScope.VerticalScrollIndicator(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier.align(Alignment.BottomCenter),
        enter = slideInVerticalBouncy,
        exit = slideOutVerticalBouncy
    ) {
        Icon(
            painter = painterResource(R.drawable.arrow_down),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
    }
}

/**
 * A scroll indicator icon that appears at the bottom when content can be scrolled.
 *
 * Shows a down arrow icon with bouncy slide-in/out animations. Only visible
 * when [visible] is true. Must be used within a [BoxScope].
 *
 * @param visible Whether to display the scroll indicator
 * @param isTop only here to differentiate the 2 overloads
 */
@Composable
fun BoxScope.VerticalScrollIndicator(
    visible: Boolean,
    isTop: Boolean
) {
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier.align(Alignment.TopCenter),
        enter = slideInVerticalBouncyUp,
        exit = slideOutVerticalBouncyUp
    ) {
        Icon(
            painter = painterResource(R.drawable.arrow_down),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.graphicsLayer {
                rotationZ = 180f
            }
        )
    }
}