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
public class UndoRedoManager(
    public val stacks: Array<out UndoRedoStack<*>>,
    scope: CoroutineScope
) {
    public val canUndo: StateFlow<Boolean> =
        combine(stacks.map { it.canUndo }) { booleans ->
            booleans.any { it }
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    public val canRedo: StateFlow<Boolean> =
        combine(stacks.map { it.canRedo }) { booleans ->
            booleans.any { it }
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    public val undoSize: StateFlow<Int> =
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

    public val redoSize: StateFlow<Int> =
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
    public inline fun applyChange(mutator: () -> Unit) {
        stacks.forEach { stack -> stack.push() }
        mutator()
    }

    public fun undo() {
        stacks.forEach { stack ->
            stack.undo()
        }
    }

    public fun redo() {
        stacks.forEach { stack ->
            stack.redo()
        }
    }

    public fun undoAll() {
        stacks.forEach { stack ->
            stack.undoAll()
        }
    }

    public fun redoAll() {
        stacks.forEach { stack ->
            stack.redoAll()
        }
    }
}
