package org.elnix.dragonlauncher.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.elnix.dragonlauncher.base.SettingFlow


@Composable
public fun <T> SettingFlow<T>.asState(): State<T> = this.flow.collectAsStateWithLifecycle()

@Composable
public fun <T> SettingFlow<T>.asMutableState(): MutableState<T> {
    val state by this.flow.collectAsStateWithLifecycle()

    return remember(state) {
        object : MutableState<T> {
            override var value: T
                get() = state
                set(value) { this@asMutableState.value = value }

            override fun component1() = value
            override fun component2(): (T) -> Unit = { value = it }
        }
    }
}

