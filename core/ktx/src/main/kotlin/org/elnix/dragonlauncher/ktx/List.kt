package org.elnix.dragonlauncher.ktx

import java.util.Random

fun <T> List<T>.randomElement(): T {
    if (isEmpty()) throw IndexOutOfBoundsException("List is empty")
    return get(Random().nextInt(size))
}

fun <T> List<T>.randomElementOrNull(): T? {
    if (isEmpty()) return null
    return get(Random().nextInt(size))
}

fun <T> List<T>?.ifNullOrEmpty(block: () -> List<T>): List<T> {
    return if (this.isNullOrEmpty()) block() else this
}

fun <T> List<T>.distinctByEquality(equalityPredicate: (T, T) -> Boolean): List<T> {
    if (size < 2) return this

    val ret = mutableListOf<T>()

    for (item in this) {
        if (ret.none { equalityPredicate(it, item) }) ret.add(item)
    }

    return ret
}


//fun <T> SnapshotStateList<T>.move(from: Int, to: Int) {
//    if (from == to) return
//    if (from in 0 until size && to in 0 until size) {
//        add(to, removeAt(from))
//    }
//}

fun <E> MutableSet<E>.addOrRemove(element: E) {
    if (contains(element)) {
        remove(element)
    } else add(element)
}
