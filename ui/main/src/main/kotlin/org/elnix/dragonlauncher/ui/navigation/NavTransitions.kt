package org.elnix.dragonlauncher.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.metadata
import androidx.navigation3.ui.NavDisplay
import org.elnix.dragonlauncher.ui.base.animation.navigationBouncySpec

val verticalMetadata: Map<String, Any> = NavDisplay.transitionSpec {
    slideInVertically(navigationBouncySpec) { it } + fadeIn() togetherWith fadeOut()
}

val horizontalMetadata: Map<String, Any> = NavDisplay.transitionSpec {
    slideInHorizontally(navigationBouncySpec) { it } + fadeIn() togetherWith fadeOut()
}
val drawerMetadata: Map<String, Any> =
    metadata {
        put(NavDisplay.TransitionKey) {
            // Slide new content up, keeping the old content in place underneath
            slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(250)
            ) togetherWith ExitTransition.KeepUntilTransitionsFinished
        }
        put(NavDisplay.PopTransitionKey) {
            // Slide old content down, revealing the new content in place underneath
            EnterTransition.None togetherWith
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(250)
                    )
        }
        put(NavDisplay.PredictivePopTransitionKey) {
            // Slide old content down, revealing the new content in place underneath
            EnterTransition.None togetherWith
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(250)
                    )
        }
    }
