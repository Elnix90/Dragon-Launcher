package org.elnix.dragonlauncher.icons

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.elnix.dragonlauncher.settings.stores.map.IconsSettingsStore

public class IconSettingsRepository(
    ctx: Context
) {
    private val selectedIconPack: Flow<String> = IconsSettingsStore.selectedIconPack.flow(ctx)
    private val useIconTint: Flow<Boolean> = IconsSettingsStore.useIconTint.flow(ctx)
    private val iconTint: Flow<Color> = IconsSettingsStore.iconsTint.flow(ctx)
    private val onlyTintIconPack: Flow<Boolean> = IconsSettingsStore.onlyTintIconPack.flow(ctx)
    private val themedIcons: Flow<Boolean> = IconsSettingsStore.themedIcons.flow(ctx)
    private val forceThemed: Flow<Boolean> = IconsSettingsStore.forceThemed.flow(ctx)
    private val adaptify: Flow<Boolean> = IconsSettingsStore.adaptify.flow(ctx)

    private val tintFlow: Flow<Color?> = combine(useIconTint, iconTint) { use, tint ->
        if (use) {
            tint
        } else null
    }

    public val settings: Flow<IconSettings> = combine(
        selectedIconPack,
        tintFlow,
        themedIcons,
        forceThemed,
        adaptify,
        onlyTintIconPack
    ) { flows ->
        IconSettings(
            iconPack = flows[0] as String?,
            iconsTint = (flows[1] as Color?)?.toArgb(),
            themedIcons = flows[2] as Boolean,
            forceThemed = flows[3] as Boolean,
            adaptify = flows[4] as Boolean,
            onlyTintIconPacks = flows[5] as Boolean
        )
    }
}

public data class IconSettings(
    val iconPack: String? = null,
    val iconsTint: Int? = null,
    val themedIcons: Boolean = false,
    val forceThemed: Boolean = false,
    val adaptify: Boolean = false,
    val onlyTintIconPacks: Boolean = true
)