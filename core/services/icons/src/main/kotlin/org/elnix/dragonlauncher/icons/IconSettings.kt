package org.elnix.dragonlauncher.icons

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.elnix.dragonlauncher.settings.stores.IconsSettingsStore

class IconSettingsRepository(
    ctx: Context
) {
    private val selectedIconPack: Flow<String> = IconsSettingsStore.selectedIconPack.flow(ctx)
    private val iconPackTint: Flow<Color> = IconsSettingsStore.iconPackTint.flow(ctx)
    private val themedIcons: Flow<Boolean> = IconsSettingsStore.themedIcons.flow(ctx)
    private val forceThemed: Flow<Boolean> = IconsSettingsStore.forceThemed.flow(ctx)
    private val adaptify: Flow<Boolean> = IconsSettingsStore.adaptify.flow(ctx)

    val settings: Flow<IconSettings> = combine(
        selectedIconPack,
        iconPackTint,
        themedIcons,
        forceThemed,
        adaptify
    ) { pack, tint, themed, force, adapt ->
        IconSettings(
            iconPack = pack,
            iconPackTint = tint.toArgb(),
            themedIcons = themed,
            forceThemed = force,
            adaptify = adapt
        )
    }
}

data class IconSettings(
    val iconPack: String? = null,
    val iconPackTint: Int = 0,
    val themedIcons: Boolean = false,
    val forceThemed: Boolean = false,
    val adaptify: Boolean = false,
)