package org.elnix.dragonlauncher.points

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

public interface NestsNavigationService {
	/**
	 * Current nest id derived from the navigation stack.
	 *
	 * Falls back to root (0) when the stack is empty.
	 */
	public val currentNestId: StateFlow<Int>

	public fun goBack()

	/**
	 * Go to nest [newNestId] and add its id to the stack
	 *
	 * If [clearStack] is `true`, the entire stack will be cleared, which means no go back nest.
	 * This is useful for the nest management sheet which by natural logic shouldn't add to a stack the different nests you visit
	 */
	public fun goToNest(newNestId: Int, clearStack: Boolean = false)

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

	private val _currentNestId = MutableStateFlow(0)
	override val currentNestId: StateFlow<Int> = _currentNestId.asStateFlow()

	override fun goBack() {
		if (nestsStack.isNotEmpty()) {
			nestsStack.removeAt(nestsStack.lastIndex)
			_currentNestId.value = nestsStack.lastOrNull() ?: 0
		}
	}

	override fun goToNest(newNestId: Int, clearStack: Boolean) {
		// Check the presence of that nest before entering it, and create a new one if not
		if (newNestId !in pointsService.nests.value) {
			pointsService.addNest(newNestId)
		}

		if (clearStack) {
			clearStack()
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
