@file:Suppress("DEPRECATION")

package org.elnix.dragonlauncher.ui.settings.customization

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.WallpaperHelper
import org.elnix.dragonlauncher.base.model.models.WallpaperTarget
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.alphaMultiplier
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.generic.ActionSelector
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.statusbar.StatusBar

@SuppressLint("LocalContextResourcesRead", "LocalContextGetResourceValueCall")
@Composable
fun WallpaperTab() {
    val ctx = LocalContext.current
    val window = LocalWindowInfo.current
    val screenWidthPx = window.containerSize.width.toFloat()
    val screenHeightPx = window.containerSize.height.toFloat()

    val scope = rememberCoroutineScope()

    val wallpaperHelper = remember { WallpaperHelper(ctx) }

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showTargetDialog by remember { mutableStateOf(false) }

    val bgColor = MaterialTheme.colorScheme.background
    var plainColor by remember { mutableStateOf(bgColor) }


    val wallpaperDimMainScreen by UiSettingsStore.wallpaperDimMainScreen.asState()
    val wallpaperDimDrawerScreen by UiSettingsStore.wallpaperDimDrawerScreen.asState()


    fun applyWallpaper(target: WallpaperTarget) {
        val bitmap = wallpaperHelper.createPlainWallpaperBitmap(ctx, plainColor)
        scope.launch {
            wallpaperHelper.setWallpaper(bitmap, target.flags)

            ctx.showToast("Wallpaper applied")
            showTargetDialog = false
        }
    }

    val mainTriangle = remember(screenHeightPx, screenWidthPx) {
        Path().apply {
            lineTo(screenWidthPx, 0f)
            lineTo(0f, screenHeightPx)
            close()
        }
    }
    val mainColor = MaterialTheme.colorScheme.background.alphaMultiplier(wallpaperDimMainScreen)

    val drawerTriangle = remember(screenHeightPx, screenWidthPx) {
        Path().apply {
            moveTo(screenWidthPx, 0f)
            lineTo(screenWidthPx, screenHeightPx)
            lineTo(0f, screenHeightPx)
            close()
        }
    }
    val drawerColor = MaterialTheme.colorScheme.background.alphaMultiplier(wallpaperDimDrawerScreen)

    Canvas(Modifier.fillMaxSize()) {
        drawPath(
            path = mainTriangle,
            color = mainColor,
            style = Fill
        )
        drawPath(
            path = drawerTriangle,
            color = drawerColor,
            style = Fill
        )
    }

    SettingsScaffold(
        title = stringResource(R.string.wallpaper),
        helpText = stringResource(R.string.wallpaper_help),
        onReset = null,
        resetText = null
    ) {
        DragonSettingsGroup(R.string.custom_wallpaper) {
            DragonButton(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
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
        }

        DragonSettingsGroup(R.string.plain_wallpaper) {
            DragonButton(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
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
                title = stringResource(R.string.plain_wallpaper_color),
                description = null,
                currentColor = plainColor,
                defaultColor = null
            ) {
                if (it != null) plainColor = it
            }
        }

        DragonSettingsGroup(R.string.wallpaper_dim) {
            Setting(UiSettingsStore.wallpaperDimMainScreen)
            Setting(UiSettingsStore.wallpaperDimDrawerScreen)
        }

        DragonSettingsGroup {
            Setting(UiSettingsStore.pointsScreensTransparency)
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
