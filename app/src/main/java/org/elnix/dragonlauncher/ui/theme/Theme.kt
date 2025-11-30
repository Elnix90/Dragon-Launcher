package org.elnix.dragonlauncher.ui.theme


import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color


fun generateColorScheme(
    primary: Color,
    onPrimary: Color,
    secondary: Color,
    onSecondary: Color,
    tertiary: Color,
    onTertiary: Color,
    background: Color,
    onBackground: Color,
    surface: Color,
    onSurface: Color,
    error: Color,
    onError: Color,
    outline: Color
): ColorScheme {

    val base = darkColorScheme()

    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        tertiary = tertiary,
        onTertiary = onTertiary,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        error = error,
        onError = onError,
        outline = outline
    )
}

@Composable
fun DragonLauncherTheme(
    customPrimary: Color? = null,
    customOnPrimary: Color? = null,
    customSecondary: Color? = null,
    customOnSecondary: Color? = null,
    customTertiary: Color? = null,
    customOnTertiary: Color? = null,
    customBackground: Color? = null,
    customOnBackground: Color? = null,
    customSurface: Color? = null,
    customOnSurface: Color? = null,
    customError: Color? = null,
    customOnError: Color? = null,
    customOutline: Color? = null,
    customAngleLineColor: Color? = null,
    customCircleColor: Color? = null,
//    customComplete: Color? = null,
//    customSelect: Color? = null,
//    customNoteTypeText: Color? = null,
//    customNoteTypeCheckList: Color? = null,
//    customNoteTypeDrawing: Color? = null,
    content: @Composable () -> Unit
) {
    val primary = customPrimary ?: AmoledDefault.Primary
    val onPrimary = customOnPrimary ?: AmoledDefault.OnPrimary

    val secondary = customSecondary ?: AmoledDefault.Secondary
    val onSecondary = customOnSecondary ?: AmoledDefault.OnSecondary

    val tertiary = customTertiary ?: AmoledDefault.Tertiary
    val onTertiary = customOnTertiary ?: AmoledDefault.OnTertiary

    val background = customBackground ?: AmoledDefault.Background
    val onBackground = customOnBackground ?: AmoledDefault.OnBackground

    val surface = customSurface ?: AmoledDefault.Surface
    val onSurface = customOnSurface ?: AmoledDefault.OnSurface

    val error = customError ?: AmoledDefault.Error
    val onError = customOnError ?: AmoledDefault.OnError

    val outline = customOutline ?: AmoledDefault.Outline

    val angleLine = customAngleLineColor ?: AmoledDefault.AngleLineColor
    val circle = customCircleColor ?: AmoledDefault.CircleColor
//    val complete = customComplete ?: AmoledDefault.Complete
//    val select = customSelect ?: AmoledDefault.Select
//
//    val noteTypeText = customNoteTypeText ?: AmoledDefault.NoteTypeText
//    val noteTypeChecklist = customNoteTypeCheckList ?: AmoledDefault.NoteTypeChecklist
//    val noteTypeDrawing = customNoteTypeDrawing ?: AmoledDefault.NoteTypeDrawing

    val extraColors = ExtraColors(
        angleLine,
        circle,
//        complete,
//        select,
//        noteTypeText,
//        noteTypeChecklist,
//        noteTypeDrawing
    )

    val colorScheme = generateColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        tertiary = tertiary,
        onTertiary = onTertiary,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        error = error,
        onError = onError,
        outline = outline,
    )

    CompositionLocalProvider(LocalExtraColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
