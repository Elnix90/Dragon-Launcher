package org.elnix.dragonlauncher.base.undoredo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Groups multiple [UndoRedoStack] instances under named keys, keeping them
 * in lockstep - a single [applyChange], [undo], or [redo] call snapshots
 * and restores all registered stacks simultaneously.
 */
class UndoRedoManager(
	val stacks: Array<out UndoRedoStack<*>>,
	scope: CoroutineScope
) {
	val canUndo: StateFlow<Boolean> =
		combine(stacks.map { it.canUndo }) { booleans ->
			booleans.any { it }
		}.stateIn(
			scope = scope,
			started = SharingStarted.Eagerly,
			initialValue = false
		)

	val canRedo: StateFlow<Boolean> =
		combine(stacks.map { it.canRedo }) { booleans ->
			booleans.any { it }
		}.stateIn(
			scope = scope,
			started = SharingStarted.Eagerly,
			initialValue = false
		)

	val undoSize: StateFlow<Int> =
		stacks
			.first()
			.undoStack.flow
			.map {
				it.size
			}.stateIn(
				scope = scope,
				started = SharingStarted.Eagerly,
				initialValue = 0
			)

	val redoSize: StateFlow<Int> =
		stacks
			.first()
			.redoStack.flow
			.map {
				it.size
			}.stateIn(
				scope = scope,
				started = SharingStarted.Eagerly,
				initialValue = 0
			)

	/** Snapshot all stacks, then run the mutation. Clears all redo histories. */
	inline fun applyChange(mutator: () -> Unit) {
		stacks.forEach { stack -> stack.push() }
		mutator()
	}

	fun undo() {
		stacks.forEach { stack ->
			stack.undo()
		}
	}

	fun redo() {
		stacks.forEach { stack ->
			stack.redo()
		}
	}

	fun undoAll() {
		stacks.forEach { stack ->
			stack.undoAll()
		}
	}

	fun redoAll() {
		stacks.forEach { stack ->
			stack.redoAll()
		}
	}
}
