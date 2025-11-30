package org.elnix.dragonlauncher.ui.theme

import androidx.compose.ui.graphics.Color
import org.elnix.dragonlauncher.utils.colors.blendWith

object AmoledDefault : ThemeColors {
    override val Primary = Color(0xFF6650a4)
    override val OnPrimary = Color(0xFFCECECE)
    override val Secondary = Color(0xFF934EB2)
    override val OnSecondary = OnPrimary
    override val Tertiary = Color(0xFFCB84EC)
    override val OnTertiary = OnPrimary
    override val Background = Color.Black
    override val OnBackground = OnPrimary
    override val Surface = Primary.blendWith(Background, 0.7f)
    override val OnSurface = OnBackground
    override val Error = Color.Red
    override val OnError = OnPrimary
    override val Outline = OnPrimary
    override val AngleLineColor = Color.Red
    override val CircleColor = Color(0xFF960000)
//    override val Complete = Color(0xFF388E3C)
//    override val Select = Color(0xFF0F7AC5)
//
//    override val NoteTypeText = Color(0xFF05609D)
//    override val NoteTypeChecklist = Color(0xFF6318A4)
//    override val NoteTypeDrawing = Color(0xFFFD0990)
}

object DarkDefault : ThemeColors {
    override val Primary = Color(0xFF9842B7)
    override val OnPrimary = Color.Black

    override val Secondary = Color(0xFFA06CB7)
    override val OnSecondary = OnPrimary

    override val Tertiary = Color(0xFFB784C9)
    override val OnTertiary = OnPrimary

    override val Background = Color(0xFF1A1624)
    override val OnBackground = Color(0xFFE6E6E6)

    override val Surface = Primary.blendWith(Background, 0.75f)
    override val OnSurface = OnBackground

    override val Error = Color.Red
    override val OnError = OnPrimary

    override val Outline = Color.White.copy(alpha = 0.8f)

    override val AngleLineColor = Color.Red
    override val CircleColor = Color(0xFF960000)
//    override val Complete = Color(0xFF81C784)
//    override val Select = Color(0xFF0F7AC5)
//
//    override val NoteTypeText = Color(0xFF05609D)
//    override val NoteTypeChecklist = Color(0xFF6318A4)
//    override val NoteTypeDrawing = Color(0xFFFD0990)
}


object LightDefault : ThemeColors {
    override val Primary = Color(0xFFA351E7)
    override val OnPrimary = Color.Black
    override val Secondary = Color(0xFF772C93)
    override val OnSecondary = OnPrimary
    override val Tertiary = Color(0xFF530D72)
    override val OnTertiary = OnPrimary
    override val Background = Color.White
    override val OnBackground = OnPrimary
    override val Surface = Primary.blendWith(Background, 0.7f)
    override val OnSurface = OnPrimary
    override val Error = Color.Red
    override val OnError = OnPrimary
    override val Outline = Color.Black.copy(alpha = 0.8f)
    override val AngleLineColor = Color.Red
    override val CircleColor = Color(0xFF960000)
//    override val Complete = Color(0xFF388E3C)
//    override val Select = Color(0xFF0F7AC5)
//
//    override val NoteTypeText = Color(0xFF05609D)
//    override val NoteTypeChecklist = Color(0xFF6318A4)
//    override val NoteTypeDrawing = Color(0xFFFD0990)
}
