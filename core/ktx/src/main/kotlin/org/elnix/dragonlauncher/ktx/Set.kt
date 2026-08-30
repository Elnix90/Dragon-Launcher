package org.elnix.dragonlauncher.ktx

/**
 * Get the next minimal positive [Int] id among a list of existing ones.
 * Iterates through all ids and returns the first that isn't in the list
 */
public fun Set<Int>.getNextId(): Int {
    var newId = 0
    while (newId in this) {
        newId++
    }
    return newId
}
