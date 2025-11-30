package org.elnix.dragonlauncher.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ExtraColors(
    val angleLine: Color,
    val circle: Color,
//    val complete: Color,
//    val select: Color,
//
//    val noteTypeText : Color,
//    val noteTypeChecklist : Color,
//    val noteTypeDrawing : Color,

)

// default fallback values
val LocalExtraColors = staticCompositionLocalOf {
    ExtraColors(
        angleLine = AmoledDefault.AngleLineColor,
        circle = AmoledDefault.CircleColor,
//        complete = AmoledDefault.Complete,
//        select = AmoledDefault.Select,
//        noteTypeText = AmoledDefault.NoteTypeText,
//        noteTypeChecklist = AmoledDefault.NoteTypeChecklist,
//        noteTypeDrawing = AmoledDefault.NoteTypeDrawing,
    )
}
