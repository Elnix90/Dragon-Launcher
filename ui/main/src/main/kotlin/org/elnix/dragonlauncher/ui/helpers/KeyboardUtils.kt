package org.elnix.dragonlauncher.ui.helpers

import android.graphics.Rect
import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * Detects whether the keyboard is visible on screen in Compose.
 */
@Composable
fun isKeyboardVisible(): State<Boolean> {
    val rootView = LocalView.current
    val keyboardVisible = remember { mutableStateOf(false) }

    DisposableEffect(rootView) {
        val listener =
            ViewTreeObserver.OnGlobalLayoutListener {
                val rect = Rect()
                rootView.getWindowVisibleDisplayFrame(rect)
                val screenHeight = rootView.rootView.height
                val keypadHeight = screenHeight - rect.bottom

                keyboardVisible.value = keypadHeight > screenHeight * 0.15
            }

        rootView.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            rootView.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    return keyboardVisible
}
