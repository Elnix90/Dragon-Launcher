package org.elnix.dragonlauncher.base.undoredo

/**
 * Manages multiple independent undo/redo stacks, each identified by a key.
 *
 * @param T The snapshot type stored across all stacks.
 */
public class UndoRedoStack<T>(
    private val snapshot: () -> T,
    private val restore: (T) -> Unit
) {
    private var undoStack: List<T> = emptyList()
    private var redoStack: List<T> = emptyList()

    public val canUndo: Boolean get() = undoStack.isNotEmpty()
    public val canRedo: Boolean get() = redoStack.isNotEmpty()

    /** Push current snapshot before a mutation. Clears redo. */
    public fun push() {
        undoStack = undoStack + snapshot()
        redoStack = emptyList()
    }

    /** Pop undo, push current to redo. Returns the state to restore, or null. */
    public fun undo() {
        if (!canUndo) return
        redoStack = redoStack + undoStack.last()
        val last = undoStack.last()
        undoStack = undoStack.dropLast(1)

        restore(last)
    }

    /** Pop redo, push current to undo. Returns the state to restore, or null. */
    public fun redo() {
        if (!canRedo) return
        undoStack = undoStack + redoStack.last()
        val last = redoStack.last()
        redoStack = redoStack.dropLast(1)

        restore(last)
    }

    /** Jump to the oldest undo entry. */
    public fun undoAll() {
        if (!canUndo) return
        val first = undoStack.first()
        redoStack = redoStack + first
        undoStack = emptyList()

        restore(first)
    }

    /** Jump to the newest redo entry. */
    public fun redoAll() {
        if (!canRedo) return
        val first = redoStack.first()
        undoStack = undoStack + first
        redoStack = emptyList()

        restore(first)
    }
}