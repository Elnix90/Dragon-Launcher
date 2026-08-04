package org.elnix.dragonlauncher.ktx

@Suppress("NOTHING_TO_INLINE")
public inline infix fun <T> T?.unless(predicate: Boolean): T? {
    return if (!predicate) this else null
}


//@Suppress("NOTHING_TO_INLINE")
//@OptIn(ExperimentalContracts::class)
//public inline fun <T> T?.takeIf(predicate: Boolean): T? {
//    return ifNot(!predicate)
//}
