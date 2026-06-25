package org.elnix.dragonlauncher.base.undoredo

/**
 * Groups multiple [UndoRedoStack] instances under named keys, keeping them
 * in lockstep — a single [applyChange], [undo], or [redo] call snapshots
 * and restores all registered stacks simultaneously.
 */
public class UndoRedoManager(
    public val stacks: Array<out UndoRedoStack<*>>
) {
    /**
     * Return true if at least 1 stack can undo, used in compose to enable/disable the undo buttons
     */
    public val canUndo: Boolean get() = stacks.any { it.canUndo }

    /**
     * Return true if at least 1 stack can redo, used in compose to enable/disable the undo buttons
     */
    public val canRedo: Boolean get() = stacks.any { it.canRedo }

    /** Snapshot all stacks, then run the mutation. Clears all redo histories. */
    public inline fun applyChange(mutator: () -> Unit) {
        stacks.forEach { stack -> stack.push() }
        mutator()
    }

    public fun undo() {
        if (!canUndo) return
        stacks.forEach { stack ->
            stack.undo()
        }
    }

    public fun redo() {
        if (!canRedo) return
        stacks.forEach { stack ->
            stack.redo()
        }
    }

    public fun undoAll() {
        if (!canUndo) return
        stacks.forEach { stack ->
            stack.undoAll()
        }
    }

    public fun redoAll() {
        if (!canRedo) return
        stacks.forEach { stack ->
            stack.redoAll()
        }
    }
}