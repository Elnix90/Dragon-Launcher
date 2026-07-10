package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.elnix.dragonlauncher.base.undoredo.UndoRedoManager
import org.elnix.dragonlauncher.enumsui.toggle.UndRedoEditTools
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow

@Composable
public fun UndoRedoBlock(undoRedo: UndoRedoManager) {

    val undoButtonEnabled by undoRedo.canUndo.collectAsState()
    val redoButtonEnabled by undoRedo.canRedo.collectAsState()

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