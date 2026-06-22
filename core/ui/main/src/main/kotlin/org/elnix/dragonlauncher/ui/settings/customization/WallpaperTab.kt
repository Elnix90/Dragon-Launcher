@file:Suppress("AssignedValueIsNeverRead", "DEPRECATION")

package org.elnix.dragonlauncher.ui.settings.customization

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.models.WallpaperTarget
import org.elnix.dragonlauncher.base.util.ColorUtils.alphaMultiplier
import org.elnix.dragonlauncher.common.WallpaperHelper
import org.elnix.dragonlauncher.enumsui.select.WallpaperEditMode
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonColumnGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.generic.ActionSelector
import org.elnix.dragonlauncher.ui.dragon.generic.SingleSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.helpers.wallpaper.WallpaperDim
import org.elnix.dragonlauncher.ui.statusbar.StatusBar

@SuppressLint("LocalContextResourcesRead", "LocalContextGetResourceValueCall")
@Composable
fun WallpaperTab(onBack: () -> Unit) {
    val ctx = LocalContext.current

    val scope = rememberCoroutineScope()

    val wallpaperHelper = remember { WallpaperHelper(ctx) }

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showTargetDialog by remember { mutableStateOf(false) }

    val bgColor = MaterialTheme.colorScheme.background
    var plainColor by remember { mutableStateOf(bgColor) }

    var selectedView by remember { mutableStateOf(WallpaperEditMode.Main) }

    val wallpaperDimMainScreen by UiSettingsStore.wallpaperDimMainScreen.asState()
    val wallpaperDimDrawerScreen by UiSettingsStore.wallpaperDimDrawerScreen.asState()

    val dimAmount = when (selectedView) {
        WallpaperEditMode.Main -> wallpaperDimMainScreen
        WallpaperEditMode.Drawer -> wallpaperDimDrawerScreen
    }

    WallpaperDim(dimAmount)

    fun applyWallpaper(target: WallpaperTarget) {
        val bitmap = wallpaperHelper.createPlainWallpaperBitmap(ctx, plainColor)
        scope.launch {
            wallpaperHelper.setWallpaper(bitmap, target.flags)

            ctx.showToast("Wallpaper applied")
            showTargetDialog = false
        }
    }

    SettingsScaffold(
        title = stringResource(R.string.wallpaper),
        onBack = onBack,
        helpText = stringResource(R.string.wallpaper_help),
        onReset = null
    ) {
        DragonButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val intent = Intent(Intent.ACTION_SET_WALLPAPER)
                ctx.startActivity(
                    Intent.createChooser(
                        intent,
                        ctx.getString(R.string.select_image)
                    )
                )
            }
        ) {
            Text(
                text = stringResource(R.string.set_wallpaper),
                textAlign = TextAlign.Center
            )
        }

        DragonButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                originalBitmap =
                    wallpaperHelper.createPlainWallpaperBitmap(ctx, plainColor)
                showTargetDialog = true
            }
        ) {
            Text(
                stringResource(R.string.set_plain_wallpaper),
                textAlign = TextAlign.Center
            )
        }

        ColorPickerRow(
            label = stringResource(R.string.plain_wallpaper_color),
            currentColor = plainColor
        ) {
            plainColor = it ?: Color.Black
        }


        DragonColumnGroup {
            SingleSelectConnectedButtonRow(
                entries = WallpaperEditMode.entries,
                checked = { selectedView == it },
            ) { selectedView = it }


            SliderWithLabel(
                modifier = Modifier.padding(10.dp),
                label = stringResource(UiSettingsStore.wallpaperDimMainScreen.title!!),
                value = if (selectedView == WallpaperEditMode.Main) wallpaperDimMainScreen else wallpaperDimDrawerScreen,
                valueRange = 0f..1f,
                color = MaterialTheme.colorScheme.primary,
                backgroundColor = MaterialTheme.colorScheme.surface.alphaMultiplier(0.5f),
                onReset = {
                    scope.launch {
                        if (selectedView == WallpaperEditMode.Main) {
                            UiSettingsStore.wallpaperDimMainScreen.reset(ctx)
                        } else {
                            UiSettingsStore.wallpaperDimDrawerScreen.reset(ctx)

                        }
                    }
                },
            ) {
                scope.launch {
                    if (selectedView == WallpaperEditMode.Main) {
                        UiSettingsStore.wallpaperDimMainScreen.set(ctx, it)
                    } else {
                        UiSettingsStore.wallpaperDimDrawerScreen.set(ctx, it)
                    }
                }
            }
        }
    }
    StatusBar(null)


    if (showTargetDialog && originalBitmap != null) {
        ActionSelector(
            label = stringResource(R.string.apply_wallpaper_to),
            options = WallpaperTarget.entries,
            selected = null,
            onSelected = ::applyWallpaper,
            onDismiss = { showTargetDialog = false }
        )
    }
}
