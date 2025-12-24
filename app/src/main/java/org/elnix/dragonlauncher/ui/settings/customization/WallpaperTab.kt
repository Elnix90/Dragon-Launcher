@file:Suppress("AssignedValueIsNeverRead", "DEPRECATION")

package org.elnix.dragonlauncher.ui.settings.customization

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.helpers.WallpaperTarget
import org.elnix.dragonlauncher.data.stores.WallpaperSettingsStore
import org.elnix.dragonlauncher.ui.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.helpers.ActionSelector
import org.elnix.dragonlauncher.ui.helpers.SliderWithLabel
import org.elnix.dragonlauncher.ui.helpers.SwitchRow
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader
import org.elnix.dragonlauncher.utils.ImageUtils
import org.elnix.dragonlauncher.utils.WallpaperHelper
import org.elnix.dragonlauncher.utils.colors.AppObjectsColors
import org.elnix.dragonlauncher.utils.showToast

@SuppressLint("LocalContextResourcesRead")
@Composable
fun WallpaperTab(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val helper = remember { WallpaperHelper(ctx) }

    var isMainWallpaperSelected by remember { mutableStateOf(true) }

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showTargetDialog by remember { mutableStateOf(false) }

    val bgColor = MaterialTheme.colorScheme.background
    var plainColor by remember { mutableStateOf(bgColor) }

    val useOnMain by WallpaperSettingsStore.getUseOnMain(ctx).collectAsState(false)
    val useOnDrawer by WallpaperSettingsStore.getUseOnDrawer(ctx).collectAsState(false)

    val mainBlurRadius by WallpaperSettingsStore.getMainBlurRadius(ctx).collectAsState(0f)
    val drawerBlurRadius by WallpaperSettingsStore.getDrawerBlurRadius(ctx).collectAsState(0f)
    var tempBlurRadius by remember { mutableFloatStateOf(0f) }


    LaunchedEffect(Unit) {
        if (isMainWallpaperSelected) {
            originalBitmap = WallpaperSettingsStore.loadMainOriginal(ctx)
            previewBitmap = WallpaperSettingsStore.loadMainBlurred(ctx)
            tempBlurRadius = mainBlurRadius
        } else {
            originalBitmap = WallpaperSettingsStore.loadDrawerOriginal(ctx)
            previewBitmap = WallpaperSettingsStore.loadDrawerBlurred(ctx)
            tempBlurRadius = drawerBlurRadius
        }
    }

    LaunchedEffect(isMainWallpaperSelected) {
        if (isMainWallpaperSelected) {
            originalBitmap = WallpaperSettingsStore.loadMainOriginal(ctx)
            previewBitmap = WallpaperSettingsStore.loadMainBlurred(ctx)
            tempBlurRadius = mainBlurRadius
        } else {
            originalBitmap = WallpaperSettingsStore.loadDrawerOriginal(ctx)
            previewBitmap = WallpaperSettingsStore.loadDrawerBlurred(ctx)
            tempBlurRadius = drawerBlurRadius
        }
    }


    fun applyWallpaper(target: WallpaperTarget) {
        val bitmap = originalBitmap ?: return
        scope.launch {
            if (target.flags != -1) helper.setWallpaper(bitmap, target.flags)

            if (isMainWallpaperSelected) {
                WallpaperSettingsStore.saveMainOriginal(ctx, bitmap)
                WallpaperSettingsStore.saveMainBlurred(ctx, bitmap)
                WallpaperSettingsStore.setMainBlurRadius(ctx, 0f) // Reset blur on new image picked
            } else {
                WallpaperSettingsStore.saveDrawerOriginal(ctx, bitmap)
                WallpaperSettingsStore.saveDrawerBlurred(ctx, bitmap)
                WallpaperSettingsStore.setDrawerBlurRadius(ctx, 0f) // Reset blur on new image picked
            }

            ctx.showToast("Wallpaper applied")
            tempBlurRadius = 0f
            showTargetDialog = false
        }
    }

    val cropLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        val uri = result.uriContent ?: return@rememberLauncherForActivityResult
        scope.launch {
            originalBitmap = ImageUtils.loadBitmap(ctx, uri)
            previewBitmap = originalBitmap
            showTargetDialog = true
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val dm = ctx.resources.displayMetrics
        cropLauncher.launch(
            CropImageContractOptions(
                uri,
                cropImageOptions = CropImageOptions(
                    cropShape = CropImageView.CropShape.RECTANGLE,
                    guidelines = CropImageView.Guidelines.ON,
                    aspectRatioX = dm.widthPixels,
                    aspectRatioY = dm.heightPixels,
                    fixAspectRatio = true
                )
            )
        )
    }

    val useWallpaper = if (isMainWallpaperSelected) useOnMain else useOnDrawer

    if (useWallpaper) {
        previewBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxHeight(),
                contentScale = ContentScale.Crop
            )
        }
    }

    SettingsLazyHeader(
        title = stringResource(R.string.wallpaper),
        onBack = onBack,
        helpText = stringResource(R.string.wallpaper_help),
        onReset = {
            scope.launch {
                WallpaperSettingsStore.resetAll(ctx)
                originalBitmap = null
                previewBitmap = null
            }
        }
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                val mainSelected = isMainWallpaperSelected
                val drawerSelected = !isMainWallpaperSelected

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (mainSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surface.copy(0.5f)
                        )
                        .clickable { isMainWallpaperSelected = true },
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (mainSelected) Text("✓", color = MaterialTheme.colorScheme.onPrimary)
                        Text(
                            stringResource(R.string.main_screen),
                            color = if (mainSelected)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Box(
                    Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (drawerSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surface.copy(0.5f)
                        )
                        .clickable { isMainWallpaperSelected = false },
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (drawerSelected) Text("✓", color = MaterialTheme.colorScheme.onPrimary)
                        Text(
                            stringResource(R.string.drawer_screen),
                            color = if (drawerSelected)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item {
            SwitchRow(
                state = useWallpaper,
                text = if (isMainWallpaperSelected)
                    stringResource(R.string.use_main_wallpaper)
                else
                    stringResource(R.string.use_drawer_wallpaper)
            ) {
                scope.launch {
                    if (isMainWallpaperSelected)
                        WallpaperSettingsStore.setUseOnMain(ctx, it)
                    else
                        WallpaperSettingsStore.setUseOnDrawer(ctx, it)
                }
            }
        }

        item {
            ColorPickerRow(
                label = stringResource(R.string.plain_wallpaper_color),
                defaultColor = MaterialTheme.colorScheme.background,
                currentColor = plainColor
            ) {
                plainColor = it
                originalBitmap = helper.createPlainWallpaperBitmap(ctx, it)
                previewBitmap = originalBitmap
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    onClick = { imagePicker.launch(arrayOf("image/*")) },
                    colors = AppObjectsColors.buttonColors()
                ) {
                    Text(stringResource(R.string.select_image), textAlign = TextAlign.Center)
                }

                Button(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    onClick = {
                        originalBitmap = helper.createPlainWallpaperBitmap(ctx, plainColor)
                        previewBitmap = originalBitmap
                        showTargetDialog = true
                    },
                    colors = AppObjectsColors.buttonColors()
                ) {
                    Text(stringResource(R.string.set_plain_wallpaper), textAlign = TextAlign.Center)
                }
            }
        }

        item {
            Card(
                colors = AppObjectsColors.cardColors(),
                modifier = Modifier.fillMaxWidth()
            ){
                SliderWithLabel(
                    modifier = Modifier.padding(12.dp),
                    label = if (isMainWallpaperSelected)
                        stringResource(R.string.main_blur_amount)
                    else
                        stringResource(R.string.drawer_blur_amount),
                    value = tempBlurRadius,
                    color = MaterialTheme.colorScheme.primary,
                    showValue = true,
                    onChange = { radius ->
                        tempBlurRadius = radius
                        originalBitmap?.let { orig ->
                            previewBitmap = ImageUtils.blurBitmap(ctx, orig, radius * 25f)
                        }
                    },
                    onDragStateChange = { dragging ->
                        if (!dragging) {
                            scope.launch {
                                if (isMainWallpaperSelected) {
                                    WallpaperSettingsStore.setMainBlurRadius(ctx, tempBlurRadius)
                                    previewBitmap?.let {
                                        WallpaperSettingsStore.saveMainBlurred(ctx, it)
                                    }
                                } else {
                                    WallpaperSettingsStore.setDrawerBlurRadius(ctx, tempBlurRadius)
                                    previewBitmap?.let {
                                        WallpaperSettingsStore.saveDrawerBlurred(ctx, it)
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    ActionSelector(
        visible = showTargetDialog && originalBitmap != null,
        label = stringResource(R.string.apply_wallpaper_to),
        options = WallpaperTarget.entries,
        selected = null,
        onSelected = ::applyWallpaper,
        onDismiss = { showTargetDialog = false }
    )
}
