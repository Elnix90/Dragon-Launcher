package org.elnix.dragonlauncher.base.undoredo

/**
 * Groups multiple [UndoRedoStack] instances under named keys, keeping them
 * in lockstep — a single [applyChange], [undo], or [redo] call snapshots
 * and restores all registered stacks simultaneously.
 */
class UndoRedoManager(
    val stacks: Array<out UndoRedoStack<*>>
) {
    /**
     * Return true if at least 1 stack can undo, used in compose to enable/disable the undo buttons
     */
    val canUndo get() = stacks.any { it.canUndo }

    /**
     * Return true if at least 1 stack can redo, used in compose to enable/disable the undo buttons
     */
    val canRedo get() = stacks.any { it.canRedo }

    /** Snapshot all stacks, then run the mutation. Clears all redo histories. */
    inline fun applyChange(mutator: () -> Unit) {
        stacks.forEach { stack -> stack.push() }
        mutator()
    }

    fun undo() {
        if (!canUndo) return
        stacks.forEach { stack ->
            stack.undo()
        }
    }

    fun redo() {
        if (!canRedo) return
        stacks.forEach { stack ->
            stack.redo()
        }
    }

    fun undoAll() {
        if (!canUndo) return
        stacks.forEach { stack ->
            stack.undoAll()
        }
    }

    fun redoAll() {
        if (!canRedo) return
        stacks.forEach { stack ->
            stack.redoAll()
        }
    }
}