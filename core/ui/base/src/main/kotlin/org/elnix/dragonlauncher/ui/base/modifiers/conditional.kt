package org.elnix.dragonlauncher.ui.base.modifiers

import androidx.compose.ui.Modifier

/**
 * Conditionally transforms this [Modifier] using [block] when [condition] is true.
 *
 * If [condition] is false, this modifier is returned unchanged.
 *
 * The [block] is executed with this modifier as the receiver,
 * allowing additional modifiers to be appended fluently.
 *
 * Example:
 * ```
 * Modifier
 *     .fillMaxWidth()
 *     .conditional(isSelected) {
 *         background(MaterialTheme.colorScheme.primaryContainer)
 *             .padding(8.dp)
 *     }
 * ```
 *
 * In the example above, the background and padding are only applied
 * when `isSelected` is true.
 */
public inline fun Modifier.conditional(
    condition: Boolean,
    block: Modifier.() -> Modifier
): Modifier =
    if (condition) this.block() else this

/**
 * Conditionally appends either [block] or [fallback] to this [Modifier].
 *
 * - If [condition] is true, [block] is appended.
 * - If false, [fallback] is appended.
 *
 * Order is preserved:
 * `this then selectedModifier`
 */
public inline fun Modifier.conditional(
    condition: Boolean,
    fallback: Modifier.() -> Modifier,
    block: Modifier.() -> Modifier
): Modifier =
   if (condition) this.block() else this.fallback()


/**
 * Conditionally transforms this [Modifier] if [value] is not null.
 *
 * If [value] is null, this modifier is returned unchanged.
 * Inside [block], [value] is smart-cast to non-null.
 *
 * Example:
 * ```
 * Modifier
 *     .fillMaxWidth()
 *     .conditional(onClick) {
 *         clickable(onClick = it)
 *     }
 * ```
 */
public inline fun <T> Modifier.conditional(
    value: T?,
    block: Modifier.(T) -> Modifier
): Modifier =
    value?.let { this.block(it) } ?: this