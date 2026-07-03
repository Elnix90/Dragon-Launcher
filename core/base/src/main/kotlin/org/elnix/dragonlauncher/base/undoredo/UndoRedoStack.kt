package org.elnix.dragonlauncher.base.undoredo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.base.SettingFlow

/**
 * Manages multiple independent undo/redo stacks
 *
 * @param T The snapshot type stored across all stacks.
 */
public class UndoRedoStack<T>(
    private val snapshot: () -> T,
    private val restore: (T) -> Unit
) {

    private val undoStack: SettingFlow<List<T>> = SettingFlow(emptyList())
    private val redoStack: SettingFlow<List<T>> = SettingFlow(emptyList())


    /** Public flow to let compose react to changes */
    public val canUndo: Flow<Boolean> = undoStack.flow.map { it.isNotEmpty() }

    /** Internal value, computed on demand and that uses the raw value, no flow */
    private val _canUndo: Boolean get() = undoStack.value.isNotEmpty()

    /** Public flow to let compose react to changes */
    public val canRedo: Flow<Boolean> = redoStack.flow.map { it.isNotEmpty() }
    /** Internal value, computed on demand and that uses the raw value, no flow */
    private val _canRedo: Boolean get() = redoStack.value.isNotEmpty()


    /** Push current snapshot before a mutation. Clears redo. */
    public fun push() {
        undoStack.value += snapshot()
        redoStack.value = emptyList()
    }

    /** Pop undo, push current to redo. Returns the state to restore, or null. */
    public fun undo() {
        if (!_canUndo) return
        redoStack.value += undoStack.value.last()
        val last = undoStack.value.last()
        undoStack.value = undoStack.value.dropLast(1)

        restore(last)
    }

    /** Pop redo, push current to undo. Returns the state to restore, or null. */
    public fun redo() {
        if (!_canRedo) return
        val last = redoStack.value.last()
        undoStack.value += last
        redoStack.value = redoStack.value.dropLast(1)

        restore(last)
    }

    /** Jump to the oldest undo entry. */
    public fun undoAll() {
        if (!_canUndo) return
        val first = undoStack.value.first()
        redoStack.value += first
        undoStack.value = emptyList()
        restore(first)
    }

    /** Jump to the newest redo entry. */
    public fun redoAll() {
        if (!_canRedo) return
        val first = redoStack.value.first()
        undoStack.value += first
        redoStack.value = emptyList()
        restore(first)
    }
}