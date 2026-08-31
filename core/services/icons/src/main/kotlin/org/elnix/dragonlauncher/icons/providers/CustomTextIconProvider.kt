package org.elnix.dragonlauncher.icons.providers

import org.elnix.dragonlauncher.base.icons.ColorLayer
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TextLayer
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.CustomTextIcon

internal class CustomTextIconProvider(
    private val customIcon: CustomTextIcon
) : IconProvider {
    override suspend fun getIcon(
        action: Action,
        size: Int
    ): LauncherIcon =
        StaticLauncherIcon(
            foregroundLayer =
                TextLayer(
                    text = customIcon.text,
                    tint = customIcon.color
                ),
            backgroundLayer =
                ColorLayer(
                    tint = customIcon.color
                )
        )
}
