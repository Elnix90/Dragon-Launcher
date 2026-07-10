package org.elnix.dragonlauncher.points

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

public interface NestsNavigationService {
    public val currentNestId: Flow<Int>

    public fun goBack()
    public fun goToNest(newNestId: Int)
    public fun clearStack()
}

internal class NestsNavigationServiceImpl : NestsNavigationService {
    /**
     *  Navigation stack holding visited nest ids.
     *  The last element represents the current position.
     */
    private val nestsStack: MutableList<Int> = mutableListOf()

    /**
     * Current nest id derived from the navigation stack.
     *
     * Falls back to root (0) when the stack is empty.
     */
    private val nestId = nestsStack.lastOrNull() ?: 0

    override val currentNestId: Flow<Int> = flowOf(nestsStack.lastOrNull() ?: 0)

    override fun goBack() {
        if (nestsStack.isNotEmpty()) {
            nestsStack.removeAt(nestsStack.lastIndex)
        }
    }

    override fun goToNest(newNestId: Int) {
        if (newNestId != nestId) {
            nestsStack.removeAt(newNestId)
            nestsStack.add(newNestId)
        }
    }

    override fun clearStack() {
        nestsStack.clear()
    }
}