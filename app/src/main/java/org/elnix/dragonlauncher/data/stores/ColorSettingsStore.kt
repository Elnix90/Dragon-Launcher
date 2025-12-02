package org.elnix.dragonlauncher.data.stores


import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.data.ColorCustomisationMode
import org.elnix.dragonlauncher.data.DefaultThemes
import org.elnix.dragonlauncher.data.colorDatastore
import org.elnix.dragonlauncher.data.defaultThemeName
import org.elnix.dragonlauncher.data.getDefaultColorScheme
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setAngleLineColor
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setBackground
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setCircleColor
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setError
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setOnBackground
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setOnError
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setOnPrimary
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setOnSecondary
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setOnSurface
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setOnTertiary
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setOutline
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setPrimary
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setSecondary
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setSurface
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore.setTertiary
import org.elnix.dragonlauncher.ui.theme.AmoledDefault
import org.elnix.dragonlauncher.ui.theme.ThemeColors
import org.elnix.dragonlauncher.utils.colors.randomColor

object ColorSettingsStore {

    private val PRIMARY_COLOR = intPreferencesKey("primary_color")
    private val ON_PRIMARY_COLOR = intPreferencesKey("on_primary_color")
    private val SECONDARY_COLOR = intPreferencesKey("secondary_color")
    private val ON_SECONDARY_COLOR = intPreferencesKey("on_secondary_color")
    private val TERTIARY_COLOR = intPreferencesKey("tertiary_color")
    private val ON_TERTIARY_COLOR = intPreferencesKey("on_tertiary_color")
    private val BACKGROUND_COLOR = intPreferencesKey("background_color")
    private val ON_BACKGROUND_COLOR = intPreferencesKey("on_background_color")

    private val SURFACE_COLOR = intPreferencesKey("surface_color")
    private val ON_SURFACE_COLOR = intPreferencesKey("on_surface_color")
    private val ERROR_COLOR = intPreferencesKey("error_color")
    private val ON_ERROR_COLOR = intPreferencesKey("on_error_color")

    private val OUTLINE_COLOR = intPreferencesKey("outline_color")

    private val ANGLE_LINE_COLOR = intPreferencesKey("delete_color")
    private val CIRCLE_COLOR = intPreferencesKey("circle_color")
//    private val COMPLETE_COLOR = intPreferencesKey("complete_color")
//    private val SELECT_COLOR = intPreferencesKey("select_color")
//    private val NOTE_TYPE_CHECKLIST= intPreferencesKey("note_type_checklist")
//    private val NOTE_TYPE_TEXT = intPreferencesKey("note_type_text")
//    private val NOTE_TYPE_DRAWING = intPreferencesKey("note_type_drawing")



    // ------------------------------------------
    //            NORMAL COLORS
    // ------------------------------------------

    fun getPrimary(ctx: Context) =
        ctx.colorDatastore.data.map { it[PRIMARY_COLOR]?.let { color -> Color(color) } }

    suspend fun setPrimary(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[PRIMARY_COLOR] = color.toArgb() }
    }

    fun getOnPrimary(ctx: Context) =
        ctx.colorDatastore.data.map { it[ON_PRIMARY_COLOR]?.let { color -> Color(color) } }

    suspend fun setOnPrimary(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[ON_PRIMARY_COLOR] = color.toArgb() }
    }

    fun getSecondary(ctx: Context) =
        ctx.colorDatastore.data.map { it[SECONDARY_COLOR]?.let { color -> Color(color) } }

    suspend fun setSecondary(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[SECONDARY_COLOR] = color.toArgb() }
    }

    fun getOnSecondary(ctx: Context) =
        ctx.colorDatastore.data.map { it[ON_SECONDARY_COLOR]?.let { color -> Color(color) } }

    suspend fun setOnSecondary(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[ON_SECONDARY_COLOR] = color.toArgb() }
    }

    fun getTertiary(ctx: Context) =
        ctx.colorDatastore.data.map { it[TERTIARY_COLOR]?.let { color -> Color(color) } }

    suspend fun setTertiary(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[TERTIARY_COLOR] = color.toArgb() }
    }

    fun getOnTertiary(ctx: Context) =
        ctx.colorDatastore.data.map { it[ON_TERTIARY_COLOR]?.let { color -> Color(color) } }

    suspend fun setOnTertiary(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[ON_TERTIARY_COLOR] = color.toArgb() }
    }

    fun getBackground(ctx: Context) =
        ctx.colorDatastore.data.map { it[BACKGROUND_COLOR]?.let { color -> Color(color) } }

    suspend fun setBackground(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[BACKGROUND_COLOR] = color.toArgb() }
    }

    fun getOnBackground(ctx: Context) =
        ctx.colorDatastore.data.map { it[ON_BACKGROUND_COLOR]?.let { color -> Color(color) } }

    suspend fun setOnBackground(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[ON_BACKGROUND_COLOR] = color.toArgb() }
    }

    fun getSurface(ctx: Context) =
        ctx.colorDatastore.data.map { it[SURFACE_COLOR]?.let { color -> Color(color) } }

    suspend fun setSurface(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[SURFACE_COLOR] = color.toArgb() }
    }

    fun getOnSurface(ctx: Context) =
        ctx.colorDatastore.data.map { it[ON_SURFACE_COLOR]?.let { color -> Color(color) } }

    suspend fun setOnSurface(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[ON_SURFACE_COLOR] = color.toArgb() }
    }

    fun getError(ctx: Context) =
        ctx.colorDatastore.data.map { it[ERROR_COLOR]?.let { color -> Color(color) } }

    suspend fun setError(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[ERROR_COLOR] = color.toArgb() }
    }

    fun getOnError(ctx: Context) =
        ctx.colorDatastore.data.map { it[ON_ERROR_COLOR]?.let { color -> Color(color) } }

    suspend fun setOnError(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[ON_ERROR_COLOR] = color.toArgb() }
    }

    fun getOutline(ctx: Context) =
        ctx.colorDatastore.data.map { it[OUTLINE_COLOR]?.let { color -> Color(color) } }

    suspend fun setOutline(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[OUTLINE_COLOR] = color.toArgb() }
    }

    // ------------------------------------------
    //            CUSTOM COLORS
    // ------------------------------------------

    fun getAngleLineColor(ctx: Context) =
        ctx.colorDatastore.data.map { it[ANGLE_LINE_COLOR]?.let { color -> Color(color) } }

    suspend fun setAngleLineColor(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[ANGLE_LINE_COLOR] = color.toArgb() }
    }

    fun getCircleColor(ctx: Context) =
        ctx.colorDatastore.data.map { it[CIRCLE_COLOR]?.let { color -> Color(color) } }

    suspend fun setCircleColor(ctx: Context, color: Color) {
        ctx.colorDatastore.edit { it[CIRCLE_COLOR] = color.toArgb() }
    }
//
//    fun getComplete(ctx: Context) =
//        ctx.colorDatastore.data.map { it[COMPLETE_COLOR]?.let { color -> Color(color) } }
//
//    suspend fun setComplete(ctx: Context, color: Color) {
//        ctx.colorDatastore.edit { it[COMPLETE_COLOR] = color.toArgb() }
//    }
//
//    fun getSelect(ctx: Context) =
//        ctx.colorDatastore.data.map { it[SELECT_COLOR]?.let { color -> Color(color) } }
//
//    suspend fun setSelect(ctx: Context, color: Color) {
//        ctx.colorDatastore.edit { it[SELECT_COLOR] = color.toArgb() }
//    }

//    fun getNoteTypeText(ctx: Context) =
//        ctx.colorDatastore.data.map { it[NOTE_TYPE_TEXT]?.let { color -> Color(color) } }
//
//    suspend fun setNoteTypeText(ctx: Context, color: Color) {
//        ctx.colorDatastore.edit { it[NOTE_TYPE_TEXT] = color.toArgb() }
//    }
//
//    fun getNoteTypeChecklist(ctx: Context) =
//        ctx.colorDatastore.data.map { it[NOTE_TYPE_CHECKLIST]?.let { color -> Color(color) } }
//
//    suspend fun setNoteTypeChecklist(ctx: Context, color: Color) {
//        ctx.colorDatastore.edit { it[NOTE_TYPE_CHECKLIST] = color.toArgb() }
//    }
//
//    fun getNoteTypeDrawing(ctx: Context) =
//        ctx.colorDatastore.data.map { it[NOTE_TYPE_DRAWING]?.let { color -> Color(color) } }
//
//    suspend fun setNoteTypeDrawing(ctx: Context, color: Color) {
//        ctx.colorDatastore.edit { it[NOTE_TYPE_DRAWING] = color.toArgb() }
//    }



    suspend fun resetColors(
        ctx: Context,
        selectedColorCustomisationMode: ColorCustomisationMode,
        selectedMode: DefaultThemes
    ) {

        val themeColors: ThemeColors = when (selectedColorCustomisationMode) {
            ColorCustomisationMode.DEFAULT -> getDefaultColorScheme(ctx, selectedMode)
            ColorCustomisationMode.NORMAL, ColorCustomisationMode.ALL -> AmoledDefault
        }

        applyThemeColors(ctx, themeColors)
    }


    suspend fun setAllRandomColors(ctx: Context) {
        val random = { randomColor() }

        setPrimary(ctx, random())
        setOnPrimary(ctx, random())
        setSecondary(ctx, random())
        setOnSecondary(ctx, random())
        setTertiary(ctx, random())
        setOnTertiary(ctx, random())
        setBackground(ctx, random())
        setOnBackground(ctx, random())
        setSurface(ctx, random())
        setOnSurface(ctx, random())
        setError(ctx, random())
        setOnError(ctx, random())
        setOutline(ctx, random())
        setAngleLineColor(ctx, random())
//        setEdit(ctx, random())
//        setComplete(ctx, random())
//        setNoteTypeText(ctx, random())
//        setNoteTypeChecklist(ctx, random())
//        setNoteTypeDrawing(ctx, random())
    }
    suspend fun resetAll(ctx: Context) {
        ctx.colorDatastore.edit { prefs ->
            prefs.remove(PRIMARY_COLOR)
            prefs.remove(ON_PRIMARY_COLOR)
            prefs.remove(SECONDARY_COLOR)
            prefs.remove(ON_SECONDARY_COLOR)
            prefs.remove(TERTIARY_COLOR)
            prefs.remove(ON_TERTIARY_COLOR)
            prefs.remove(BACKGROUND_COLOR)
            prefs.remove(ON_BACKGROUND_COLOR)
            prefs.remove(SURFACE_COLOR)
            prefs.remove(ON_SURFACE_COLOR)
            prefs.remove(ERROR_COLOR)
            prefs.remove(ON_ERROR_COLOR)
            prefs.remove(OUTLINE_COLOR)
            prefs.remove(ANGLE_LINE_COLOR)
            prefs.remove(CIRCLE_COLOR)
//            prefs.remove(EDIT_COLOR)
//            prefs.remove(COMPLETE_COLOR)
//            prefs.remove(SELECT_COLOR)
//            prefs.remove(NOTE_TYPE_CHECKLIST)
//            prefs.remove(NOTE_TYPE_TEXT)
//            prefs.remove(NOTE_TYPE_DRAWING)
        }
    }
    suspend fun getAll(ctx: Context): Map<String, Int> {
        val prefs = ctx.colorDatastore.data.first()
        val colorMode = ColorModesSettingsStore.getColorCustomisationMode(ctx).first()
        val defaultTheme = ColorModesSettingsStore.getDefaultTheme(ctx).first()

        val default = if (colorMode == ColorCustomisationMode.DEFAULT) getDefaultColorScheme(ctx, defaultTheme)
                     else AmoledDefault
        return buildMap {
            fun putIfNonDefault(key: String, value: Int?, default: Color) {
                if (value != null && value != default.toArgb()) {
                    put(key, value)
                }
            }

            putIfNonDefault(PRIMARY_COLOR.name,        prefs[PRIMARY_COLOR],        default.Primary)
            putIfNonDefault(ON_PRIMARY_COLOR.name,     prefs[ON_PRIMARY_COLOR],     default.OnPrimary)
            putIfNonDefault(SECONDARY_COLOR.name,      prefs[SECONDARY_COLOR],      default.Secondary)
            putIfNonDefault(ON_SECONDARY_COLOR.name,   prefs[ON_SECONDARY_COLOR],   default.OnSecondary)
            putIfNonDefault(TERTIARY_COLOR.name,       prefs[TERTIARY_COLOR],       default.Tertiary)
            putIfNonDefault(ON_TERTIARY_COLOR.name,    prefs[ON_TERTIARY_COLOR],    default.OnTertiary)
            putIfNonDefault(BACKGROUND_COLOR.name,     prefs[BACKGROUND_COLOR],     default.Background)
            putIfNonDefault(ON_BACKGROUND_COLOR.name,  prefs[ON_BACKGROUND_COLOR],  default.OnBackground)
            putIfNonDefault(SURFACE_COLOR.name,        prefs[SURFACE_COLOR],        default.Surface)
            putIfNonDefault(ON_SURFACE_COLOR.name,     prefs[ON_SURFACE_COLOR],     default.OnSurface)
            putIfNonDefault(ERROR_COLOR.name,          prefs[ERROR_COLOR],          default.Error)
            putIfNonDefault(ON_ERROR_COLOR.name,       prefs[ON_ERROR_COLOR],       default.OnError)
            putIfNonDefault(OUTLINE_COLOR.name,        prefs[OUTLINE_COLOR],        default.Outline)
            putIfNonDefault(ANGLE_LINE_COLOR.name,     prefs[ANGLE_LINE_COLOR],     default.AngleLineColor)
            putIfNonDefault(CIRCLE_COLOR.name,         prefs[CIRCLE_COLOR],         default.CircleColor)
        }
    }

    suspend fun setAll(ctx: Context, data: Map<String, Int>) {
        ctx.colorDatastore.edit { prefs ->
            data[PRIMARY_COLOR.name]?.let { prefs[PRIMARY_COLOR] = it }
            data[ON_PRIMARY_COLOR.name]?.let { prefs[ON_PRIMARY_COLOR] = it }
            data[SECONDARY_COLOR.name]?.let { prefs[SECONDARY_COLOR] = it }
            data[ON_SECONDARY_COLOR.name]?.let { prefs[ON_SECONDARY_COLOR] = it }
            data[TERTIARY_COLOR.name]?.let { prefs[TERTIARY_COLOR] = it }
            data[ON_TERTIARY_COLOR.name]?.let { prefs[ON_TERTIARY_COLOR] = it }
            data[BACKGROUND_COLOR.name]?.let { prefs[BACKGROUND_COLOR] = it }
            data[ON_BACKGROUND_COLOR.name]?.let { prefs[ON_BACKGROUND_COLOR] = it }
            data[SURFACE_COLOR.name]?.let { prefs[SURFACE_COLOR] = it }
            data[ON_SURFACE_COLOR.name]?.let { prefs[ON_SURFACE_COLOR] = it }
            data[ERROR_COLOR.name]?.let { prefs[ERROR_COLOR] = it }
            data[ON_ERROR_COLOR.name]?.let { prefs[ON_ERROR_COLOR] = it }
            data[OUTLINE_COLOR.name]?.let { prefs[OUTLINE_COLOR] = it }
            data[ANGLE_LINE_COLOR.name]?.let { prefs[ANGLE_LINE_COLOR] = it }
            data[CIRCLE_COLOR.name]?.let { prefs[CIRCLE_COLOR] = it }
//            data[EDIT_COLOR.name]?.let { prefs[EDIT_COLOR] = it }
//            data[COMPLETE_COLOR.name]?.let { prefs[COMPLETE_COLOR] = it }
//            data[SELECT_COLOR.name]?.let { prefs[SELECT_COLOR] = it }
//            data[NOTE_TYPE_TEXT.name]?.let { prefs[NOTE_TYPE_TEXT] = it }
//            data[NOTE_TYPE_CHECKLIST.name]?.let { prefs[NOTE_TYPE_CHECKLIST] = it }
//            data[NOTE_TYPE_DRAWING.name]?.let { prefs[NOTE_TYPE_DRAWING] = it }
        }
    }
}


private suspend fun applyThemeColors(ctx: Context, colors: ThemeColors) {
    setPrimary(ctx, colors.Primary)
    setOnPrimary(ctx, colors.OnPrimary)
    setSecondary(ctx, colors.Secondary)
    setOnSecondary(ctx, colors.OnSecondary)
    setTertiary(ctx, colors.Tertiary)
    setOnTertiary(ctx, colors.OnTertiary)
    setBackground(ctx, colors.Background)
    setOnBackground(ctx, colors.OnBackground)
    setSurface(ctx, colors.Surface)
    setOnSurface(ctx, colors.OnSurface)
    setError(ctx, colors.Error)
    setOnError(ctx, colors.OnError)
    setOutline(ctx, colors.Outline)
    setAngleLineColor(ctx, colors.AngleLineColor)
    setCircleColor(ctx, colors.CircleColor)
//    setEdit(ctx, colors.Edit)
//    setComplete(ctx, colors.Complete)
//    setNoteTypeText(ctx,colors.NoteTypeText)
//    setNoteTypeChecklist(ctx,colors.NoteTypeChecklist)
//    setNoteTypeDrawing(ctx,colors.NoteTypeDrawing)
}
