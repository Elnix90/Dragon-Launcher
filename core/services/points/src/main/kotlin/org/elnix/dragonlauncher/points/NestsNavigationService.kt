package org.elnix.dragonlauncher.points

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

public interface NestsNavigationService {
    public val currentNestId: StateFlow<Int>

    public fun goBack()
    public fun goToNest(newNestId: Int)
    public fun clearStack()
}

internal class NestsNavigationServiceImpl(
    private val pointsService: PointsService
) : NestsNavigationService {
    /**
     *  Navigation stack holding visited nest ids.
     *  The last element represents the current position.
     */
    private val nestsStack: MutableList<Int> = mutableListOf()

    private val _currentNestId = MutableStateFlow( 0)
    /**
     * Current nest id derived from the navigation stack.
     *
     * Falls back to root (0) when the stack is empty.
     */
    override val currentNestId: StateFlow<Int> = _currentNestId.asStateFlow()

    override fun goBack() {
        if (nestsStack.isNotEmpty()) {
            nestsStack.removeAt(nestsStack.lastIndex)
            _currentNestId.value = nestsStack.lastOrNull() ?: 0
        }
    }

    override fun goToNest(newNestId: Int) {
        // Check the presence of that nest before entering it
        if (newNestId !in pointsService.nests.value) {
            pointsService.addNest(newNestId)
        }

        if (newNestId != _currentNestId.value) {
            nestsStack.remove(newNestId)
            nestsStack.add(newNestId)
            _currentNestId.value = newNestId
        }
    }

    override fun clearStack() {
        nestsStack.clear()
        _currentNestId.value = 0
    }
}