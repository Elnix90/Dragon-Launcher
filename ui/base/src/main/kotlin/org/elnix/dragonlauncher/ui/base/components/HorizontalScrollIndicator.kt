package org.elnix.dragonlauncher.ui.base.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.base.animation.slideInHorizontalBouncy
import org.elnix.dragonlauncher.ui.base.animation.slideOutHorizontalBouncy

/**
 * A lazy row with an auto-hiding scroll indicator icon.
 *
 * Renders a scrollable list and automatically shows/hides a right arrow icon
 * at the bottom when more content is available to scroll. The indicator uses
 * a bouncy entrance/exit animation.
 *
 * @param T The type of items in the list
 * @param items The list of items to display
 * @param modifier Modifier applied to the LazyRow
 * @param content Composable lambda to render each item
 */
@Composable
fun <T> LazyRowWithScrollIndicator(
    items: List<T>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(5.dp),
    content: @Composable (T) -> Unit
) {
    val state = rememberLazyListState()

    Box {
        LazyRow(
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier,
            state = state
        ) {
            items(
                items = items,
                key = { it.hashCode() }
            ) { type ->
                content(type)
            }
        }
        HorizontalScrollIndicator(state.canScrollForward)
    }
}


@Composable
fun RowWithScrollIndicator(
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable RowScope.() -> Unit
) {

    Box {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            content()
        }
        HorizontalScrollIndicator(scrollState.canScrollForward)
    }
}

/**
 * A scroll indicator icon that appears at the right when content can be scrolled.
 *
 * Shows a right arrow icon with bouncy slide-in/out animations. Only visible
 * when [visible] is true. Must be used within a [BoxScope].
 *
 * @param visible Whether to display the scroll indicator
 */
@Composable
fun BoxScope.HorizontalScrollIndicator(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier.align(Alignment.CenterEnd),
        enter = slideInHorizontalBouncy,
        exit = slideOutHorizontalBouncy
    ) {
        Icon(
            painter = painterResource(R.drawable.arrow_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
    }
}
