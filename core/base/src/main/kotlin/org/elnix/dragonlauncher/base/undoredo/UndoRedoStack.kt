package org.elnix.dragonlauncher.base.undoredo

/**
 * Manages multiple independent undo/redo stacks, each identified by a key.
 *
 * @param T The snapshot type stored across all stacks.
 */
class UndoRedoStack<T> {
    private var undoStack: List<T> = emptyList()
    private var redoStack: List<T> = emptyList()

    val canUndo get() = undoStack.isNotEmpty()
    val canRedo get() = redoStack.isNotEmpty()

    /** Push current snapshot before a mutation. Clears redo. */
    fun push(snapshot: T) {
        undoStack = undoStack + snapshot
        redoStack = emptyList()
    }

    /** Pop undo, push current to redo. Returns the state to restore, or null. */
    fun undo(current: T): T? {
        if (!canUndo) return null
        redoStack = redoStack + current
        val last = undoStack.last()
        undoStack = undoStack.dropLast(1)
        return last
    }

    /** Pop redo, push current to undo. Returns the state to restore, or null. */
    fun redo(current: T): T? {
        if (!canRedo) return null
        undoStack = undoStack + current
        val last = redoStack.last()
        redoStack = redoStack.dropLast(1)
        return last
    }

    /** Jump to the oldest undo entry. */
    fun undoAll(current: T): T? {
        if (!canUndo) return null
        redoStack = redoStack + current
        val first = undoStack.first()
        undoStack = emptyList()
        return first
    }

    /** Jump to the newest redo entry. */
    fun redoAll(current: T): T? {
        if (!canRedo) return null
        undoStack = undoStack + current
        val first = redoStack.first()
        redoStack = emptyList()
        return first
    }

//    fun clearAll() {
//        undoStack = emptyList()
//        redoStack = emptyList()
//    }
}