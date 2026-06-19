package org.elnix.dragonlauncher.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.elnix.dragonlauncher.models.utils.StateFlowWrapper
import org.elnix.dragonlauncher.settings.bases.objects.SettingObject

/**
 * Collects the current value of this setting as a Compose [State], using a default value if none is set.
 *
 * This is useful for observing the setting in Composables and triggering recompositions automatically
 * when the value changes.
 *
 * @param default Optional default value to use before the first emission. If null, the setting's own default is used.
 * @return A [State] holding the current value of the setting.
 *
 */
@Composable
fun <T, R> SettingObject<T, R>.asState(default: T? = null): State<T> {
    val ctx = LocalContext.current
    return flow(ctx).collectAsStateWithLifecycle(initialValue = default ?: this.default)
}

/**
 * Collects the current value of this setting as a Compose [State] that allows null values.
 *
 * Unlike [asState], this version always starts with `null` and can represent an unset state explicitly.
 * Useful when `null` has semantic meaning in your UI.
 *
 * @return A [State] holding the current value of the setting, or null if not yet set.
 */
@Composable
fun <T, R> SettingObject<T, R>.asStateNull(): State<T?> {
    val ctx = LocalContext.current
    return flow(ctx).collectAsStateWithLifecycle(initialValue = null)
}


@Composable
fun <T> StateFlowWrapper<T>.asState(): State<T> = this.flow.collectAsStateWithLifecycle()
