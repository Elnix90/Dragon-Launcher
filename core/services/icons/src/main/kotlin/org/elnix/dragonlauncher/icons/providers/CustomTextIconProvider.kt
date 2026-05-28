package org.elnix.dragonlauncher.icons.providers

import org.elnix.dragonlauncher.base.icons.ColorLayer
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TextLayer
import org.elnix.dragonlauncher.common.search.Application
import org.elnix.dragonlauncher.common.serializables.CustomTextIcon

class CustomTextIconProvider(
    private val customIcon: CustomTextIcon,
): IconProvider {
    override suspend fun getIcon(
        application: Application,
        size: Int
    ): LauncherIcon {
        return StaticLauncherIcon(
            foregroundLayer = TextLayer(
                text = customIcon.text,
                color = customIcon.color,
            ),
            backgroundLayer = ColorLayer(
                color = customIcon.color,
            ),
        )
    }
}



//package org.elnix.dragonlauncher.icons.providers
//
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.toArgb
//import org.elnix.dragonlauncher.base.icons.ColorLayer
//import org.elnix.dragonlauncher.base.icons.LauncherIcon
//import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
//import org.elnix.dragonlauncher.base.icons.TextLayer
//import org.elnix.dragonlauncher.base.search.Application
//import org.elnix.dragonlauncher.common.serializables.CustomIconSerializable
//import org.elnix.dragonlauncher.common.serializables.IconType
//
//class CustomTextIconProvider(
//    private val customIcon: CustomIconSerializable
//): IconProvider {
//    override suspend fun getIcon(
//        application: Application,
//        size: Int
//    ): LauncherIcon? {
//        if (customIcon.type != IconType.TEXT) return null
//        return StaticLauncherIcon(
//            foregroundLayer = TextLayer(
//                text = customIcon.source ?: "?",
//                color = customIcon.tint ?: Color.White.toArgb()
//            ),
//            backgroundLayer = ColorLayer(
//                color = 0
//            )
//        )
//    }
//}