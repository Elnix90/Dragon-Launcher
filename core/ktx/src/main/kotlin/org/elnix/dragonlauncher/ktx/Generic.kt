package org.elnix.dragonlauncher.ktx

import kotlin.contracts.ExperimentalContracts

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalContracts::class)
public inline fun <T> T?.takeIfNot(predicate: Boolean): T? {
    return if (!predicate) this else null
}


@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalContracts::class)
public inline fun <T> T?.takeIf(predicate: Boolean): T? {
    return takeIfNot(!predicate)
}
