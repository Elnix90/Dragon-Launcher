package org.elnix.dragonlauncher.ui.compositionslocals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.enumsui.toggle.HorizontalAlignment
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.IconsSettingsStore


data class DrawerSettings(
    val maxIconSize: Dp,
    val showAppIconsInDrawer: Boolean,
    val showAppLabelsInDrawer: Boolean,
    val iconsSpacingVertical: Dp,
    val iconsSpacingHorizontal: Dp,
    val horizontalAlignment: HorizontalAlignment,
    val iconShape: IconShape,
    val renderForeground: Boolean,
    val renderBackground: Boolean
)

val LocalDrawerSettings: ProvidableCompositionLocal<DrawerSettings> = compositionLocalOf { error("No DrawerSettings provided") }

@Composable
fun ProvideDrawerSettings(content: @Composable () -> Unit) {

    val maxIconSize by DrawerSettingsStore.maxIconSize.asState()
    val showAppIconsInDrawer by DrawerSettingsStore.showAppIconsInDrawer.asState()
    val showAppLabelsInDrawer by DrawerSettingsStore.showAppLabelsInDrawer.asState()
    val iconsSpacingVertical by DrawerSettingsStore.iconsSpacingVertical.asState()
    val iconsSpacingHorizontal by DrawerSettingsStore.iconsSpacingHorizontal.asState()
    val horizontalAlignment by DrawerSettingsStore.horizontalAlignment.asState()
    val iconShape by DrawerSettingsStore.iconShape.asState()

    val renderForeground by IconsSettingsStore.renderForeground.asState()
    val renderBackground by IconsSettingsStore.renderBackground.asState()
    CompositionLocalProvider(
        LocalDrawerSettings provides DrawerSettings(
            maxIconSize = maxIconSize,
            showAppIconsInDrawer = showAppIconsInDrawer,
            showAppLabelsInDrawer = showAppLabelsInDrawer,
            iconsSpacingVertical = iconsSpacingVertical,
            iconsSpacingHorizontal = iconsSpacingHorizontal,
            horizontalAlignment = horizontalAlignment,
            iconShape = iconShape,
            renderForeground = renderForeground,
            renderBackground = renderBackground
        ),
        content = content
    )
}