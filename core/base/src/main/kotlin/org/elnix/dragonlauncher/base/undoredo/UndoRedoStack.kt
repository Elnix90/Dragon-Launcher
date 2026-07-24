package org.elnix.dragonlauncher.base.undoredo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.base.SettingFlow

/**
 * Manages a single independent block of undo/redo stacks
 *
 * @param T The snapshot type stored across all stacks
 *
 * @see UndoRedoManager for the multi stack management.
 */
public class UndoRedoStack<T>(
    private val snapshot: () -> T,
    private val restore: (T) -> Unit
) {

    internal val undoStack: SettingFlow<List<T>> = SettingFlow(emptyList())
    internal val redoStack: SettingFlow<List<T>> = SettingFlow(emptyList())



    /** Public flow to let compose react to changes */
    public val canUndo: Flow<Boolean> = undoStack.flow.map { it.isNotEmpty() }

    /** Internal value, computed on demand and that uses the raw value, no flow */
    private inline val _canUndo: Boolean get() = undoStack.value.isNotEmpty()

    /** Public flow to let compose react to changes */
    public val canRedo: Flow<Boolean> = redoStack.flow.map { it.isNotEmpty() }

    /** Internal value, computed on demand and that uses the raw value, no flow */
    private inline val _canRedo: Boolean get() = redoStack.value.isNotEmpty()


    /** Push current snapshot before a mutation. Clears redo. */
    public fun push() {
        undoStack.value += snapshot()
        redoStack.value = emptyList()
    }

    public fun undo() {
        if (!_canUndo) return
        redoStack.value += snapshot()
        restore(undoStack.value.last())
        undoStack.value = undoStack.value.dropLast(1)
    }

    public fun redo() {
        if (!_canRedo) return
        undoStack.value += snapshot()
        restore(redoStack.value.last())
        redoStack.value = redoStack.value.dropLast(1)
    }

    public fun undoAll() {
        if (!_canUndo) return
        val target = undoStack.value.first()
        redoStack.value = undoStack.value.asReversed().drop(1) + redoStack.value
        restore(target)
        undoStack.value = emptyList()
    }

    public fun redoAll() {
        if (!_canRedo) return
        val target = redoStack.value.first()
        undoStack.value = redoStack.value.asReversed().drop(1) + undoStack.value
        restore(target)
        redoStack.value = emptyList()
    }
}