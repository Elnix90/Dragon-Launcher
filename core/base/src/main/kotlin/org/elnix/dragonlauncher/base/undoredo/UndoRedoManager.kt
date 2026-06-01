package org.elnix.dragonlauncher.base.undoredo

/**
 * Groups multiple [UndoRedoStack] instances under named keys, keeping them
 * in lockstep — a single [applyChange], [undo], or [redo] call snapshots
 * and restores all registered stacks simultaneously.
 */
class UndoRedoManager {
    private val stacks = mutableMapOf<String, UndoRedoStack<Any?>>()
    private val snapshots = mutableMapOf<String, () -> Any?>()
    private val restores = mutableMapOf<String, (Any?) -> Unit>()

    val canUndo get() = stacks.values.any { it.canUndo }
    val canRedo get() = stacks.values.any { it.canRedo }

    /**
     * Register a stack with a snapshot provider and a restore callback.
     *
     * @param key Unique identifier for this stack.
     * @param snapshot Lambda returning the current state to snapshot.
     * @param restore Lambda applying a restored state.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> register(key: String, snapshot: () -> T, restore: (T) -> Unit) {
        stacks[key] = UndoRedoStack()
        snapshots[key] = snapshot as () -> Any?
        restores[key] = { restore(it as T) }
    }

    /** Snapshot all stacks, then run the mutation. Clears all redo histories. */
    fun applyChange(mutator: () -> Unit) {
        stacks.forEach { (key, stack) -> stack.push(snapshots[key]!!()) }
        mutator()
    }

    fun undo() {
        if (!canUndo) return
        stacks.forEach { (key, stack) ->
            stack.undo(snapshots[key]!!())?.let { restores[key]!!(it) }
        }
    }

    fun redo() {
        if (!canRedo) return
        stacks.forEach { (key, stack) ->
            stack.redo(snapshots[key]!!())?.let { restores[key]!!(it) }
        }
    }

    fun undoAll() {
        if (!canUndo) return
        stacks.forEach { (key, stack) ->
            stack.undoAll(snapshots[key]!!())?.let { restores[key]!!(it) }
        }
    }

    fun redoAll() {
        if (!canRedo) return
        stacks.forEach { (key, stack) ->
            stack.redoAll(snapshots[key]!!())?.let { restores[key]!!(it) }
        }
    }
}