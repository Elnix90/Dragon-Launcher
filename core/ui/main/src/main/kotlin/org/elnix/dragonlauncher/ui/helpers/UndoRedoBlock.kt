package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.runtime.Composable
import org.elnix.dragonlauncher.base.undoredo.UndoRedoManager
import org.elnix.dragonlauncher.enumsui.toggle.UndRedoEditTools
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow

@Composable
fun UndoRedoBlock(undoRedo: UndoRedoManager) {
    val undoButtonEnabled = undoRedo.canUndo
    val redoButtonEnabled = undoRedo.canRedo
    MultiSelectConnectedButtonRow(
        entries = UndRedoEditTools.entries,
        enabled = {
            when (it) {
                UndRedoEditTools.UndoAll -> undoButtonEnabled
                UndRedoEditTools.Undo -> undoButtonEnabled
                UndRedoEditTools.Redo -> redoButtonEnabled
                UndRedoEditTools.RedoAll -> redoButtonEnabled
            }
        },
        checked = {
            when (it) {
                UndRedoEditTools.UndoAll -> undoButtonEnabled
                UndRedoEditTools.Undo -> undoButtonEnabled
                UndRedoEditTools.Redo -> redoButtonEnabled
                UndRedoEditTools.RedoAll -> redoButtonEnabled
            }
        }
    ) { entry ->
        when (entry) {
            UndRedoEditTools.UndoAll -> undoRedo.undoAll()
            UndRedoEditTools.Undo -> undoRedo.undo()
            UndRedoEditTools.Redo -> undoRedo.redo()
            UndRedoEditTools.RedoAll -> undoRedo.redoAll()
        }
    }
}