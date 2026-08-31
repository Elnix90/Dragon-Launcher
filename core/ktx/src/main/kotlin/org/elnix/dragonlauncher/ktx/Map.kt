package org.elnix.dragonlauncher.ktx

import android.util.ArraySet

/**
 * Groups elements of the original collection by the key returned by the given [keySelector] function
 * applied to each element and puts to the [destination] map each group key associated with a set of corresponding elements.
 *
 * @return The [destination] map.
 **/
public inline fun <T, K, M : MutableMap<in K, MutableSet<T>>> Iterable<T>.groupByTo(
    destination: M,
    keySelector: (T) -> K
): M {
    for (element in this) {
        val key = keySelector(element)
        val set = destination.getOrPut(key) { ArraySet() }
        set.add(element)
    }
    return destination
}
