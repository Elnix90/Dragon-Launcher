package org.elnix.dragonlauncher.base.undoredo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.base.SettingFlow

/**
 * Manages a single undo/redo stack pair for recording and navigating
 * a linear history of snapshots.
 *
 * Both stacks are ordered oldest-first with the top at the end.
 * New entries are appended via `+=`, and [undo]/[redo] pop via `.last()`.
 *
 * @param T The snapshot type stored across all stacks.
 * @param snapshot Lazily captures the current state before a mutation.
 * @param restore Applies a previously captured snapshot to rebuild state.
 *
 * @see UndoRedoManager for multi-stack management.
 */
public class UndoRedoStack<T>(
    private val snapshot: () -> T,
    private val restore: (T) -> Unit
) {

    internal val undoStack: SettingFlow<List<T>> = SettingFlow(emptyList())
    internal val redoStack: SettingFlow<List<T>> = SettingFlow(emptyList())

    /**
     * Emits `true` whenever there is at least one entry on the undo stack,
     * indicating that [undo] or [undoAll] would have effect.
     */
    public val canUndo: Flow<Boolean> = undoStack.flow.map { it.isNotEmpty() }

    private inline val _canUndo: Boolean get() = undoStack.value.isNotEmpty()

    /**
     * Emits `true` whenever there is at least one entry on the redo stack,
     * indicating that [redo] or [redoAll] would have effect.
     */
    public val canRedo: Flow<Boolean> = redoStack.flow.map { it.isNotEmpty() }

    private inline val _canRedo: Boolean get() = redoStack.value.isNotEmpty()

    /**
     * Records the current state as an undo point.
     *
     * This must be called **before** every mutation that should be undoable.
     * Pushing a new snapshot clears the redo stack, since a new mutation
     * diverges from any previously undone path.
     */
    public fun push() {
        undoStack.value += snapshot()
        redoStack.value = emptyList()
    }

    /**
     * Undoes the most recent operation by one step.
     *
     * Saves the current state to the redo stack, restores the top entry
     * of the undo stack, and removes that entry. Does nothing when the
     * undo stack is empty.
     */
    public fun undo() {
        if (!_canUndo) return
        redoStack.value += snapshot()
        restore(undoStack.value.last())
        undoStack.value = undoStack.value.dropLast(1)
    }

    /**
     * Redoes the most recently undone operation by one step.
     *
     * Saves the current state to the undo stack, restores the top entry
     * of the redo stack, and removes that entry. Does nothing when the
     * redo stack is empty.
     */
    public fun redo() {
        if (!_canRedo) return
        undoStack.value += snapshot()
        restore(redoStack.value.last())
        redoStack.value = redoStack.value.dropLast(1)
    }

    /**
     * Jumps directly to the oldest recorded state.
     *
     * Builds the redo stack so that subsequent [redo] calls walk forward
     * through every intermediate state and end at the current snapshot:
     *
     * ```
     * redoStack = existingRedo + [currentSnapshot] + undoStack.drop(1).reversed()
     * ```
     *
     * Then restores the first (oldest) undo entry and clears the undo stack.
     * Does nothing when the undo stack is empty.
     */
    public fun undoAll() {
        if (!_canUndo) return
        val target = undoStack.value.first()
        redoStack.value = redoStack.value + listOf(snapshot()) + undoStack.value.drop(1).asReversed()
        restore(target)
        undoStack.value = emptyList()
    }

    /**
     * Jumps directly to the newest available redo state.
     *
     * Builds the undo stack so that subsequent [undo] calls walk backward
     * through every intermediate state and end at the current snapshot:
     *
     * ```
     * undoStack = existingUndo + [currentSnapshot] + redoStack.drop(1).reversed()
     * ```
     *
     * Then restores the first (newest) redo entry and clears the redo stack.
     * Does nothing when the redo stack is empty.
     */
    public fun redoAll() {
        if (!_canRedo) return
        val target = redoStack.value.first()
        undoStack.value = undoStack.value + listOf(snapshot()) + redoStack.value.drop(1).asReversed()
        restore(target)
        redoStack.value = emptyList()
    }
}
