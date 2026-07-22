package org.elnix.dragonlauncher.ui.settings.customization

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import io.github.elnix90.logging.FONT_PROVIDER
import io.github.elnix90.logging.logD
import io.github.elnix90.logging.logE
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.Constants
import org.elnix.dragonlauncher.fonts.fontNameToFont
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.services.ExtensionManager
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import java.io.File
import java.io.FileOutputStream
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
public fun FontTab(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val globalFontName by UiSettingsStore.globalFont.asState()

    var refreshTrigger by remember { mutableIntStateOf(0) }

    val isExtensionInstalled = remember(refreshTrigger) {
        ExtensionManager.isExtensionInstalled(ctx, Constants.Extensions.FONT_EXTENSION_PKG)
    }

    var extensionSearchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    val remoteFonts = remember { mutableStateMapOf<String, String>() }
    var isFetchingRemote by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf<Triple<Int, Int, String>?>(null) }
    var showProgress by remember { mutableStateOf(false) }
    val selectedFontsToDelete = remember { mutableStateListOf<String>() }
    var isDeleteMode by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    "org.elnix.dragonlauncher.ACTION_FONTS_RESULT" -> {
                        val fontName = intent.getStringExtra("FONT_NAME") ?: "unknown"
                        val fontUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra("FONT_URI", Uri::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra("FONT_URI")
                        }
                        if (fontUri != null) {
                            scope.launch {
                                try {
                                    val destDir = File(ctx.getExternalFilesDir(null), "fonts")
                                    if (!destDir.exists()) destDir.mkdirs()
                                    val destFile = File(destDir, "$fontName.ttf")
                                    ctx.contentResolver.openInputStream(fontUri)?.use { input ->
                                        FileOutputStream(destFile).use { output ->
                                            input.copyTo(
                                                output
                                            )
                                        }
                                    }
                                    refreshTrigger = (refreshTrigger + 1) % 1000
                                    UiSettingsStore.globalFont.set(ctx, fontName)
                                    ctx.showToast("Font $fontName applied!")
                                } catch (e: Exception) {
                                    logE(FONT_PROVIDER, e) { "Import failed for $fontName" }
                                }
                            }
                        }
                    }

                    "org.elnix.dragonlauncher.ACTION_DOWNLOAD_PROGRESS" -> {
                        val current = intent.getIntExtra("CURRENT", 0)
                        val total = intent.getIntExtra("TOTAL", 1)
                        val fontName = intent.getStringExtra("FONT_NAME") ?: ""

                        if (intent.hasExtra("FONT_URI")) {
                            val fontUri =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    intent.getParcelableExtra("FONT_URI", Uri::class.java)
                                } else {
                                    @Suppress("DEPRECATION")
                                    intent.getParcelableExtra("FONT_URI")
                                }
                            if (fontUri != null) {
                                scope.launch {
                                    try {
                                        val destDir = File(ctx.getExternalFilesDir(null), "fonts")
                                        if (!destDir.exists()) destDir.mkdirs()
                                        val destFile = File(destDir, "$fontName.ttf")
                                        ctx.contentResolver.openInputStream(fontUri)?.use { input ->
                                            FileOutputStream(destFile).use { output ->
                                                input.copyTo(
                                                    output
                                                )
                                            }
                                        }
                                        refreshTrigger = (refreshTrigger + 1) % 1000
                                    } catch (e: Exception) {
                                        logE(
                                            FONT_PROVIDER,
                                            e
                                        ) { "Auto-import failed for $fontName" }
                                    }
                                }
                            }
                        }

                        downloadProgress = Triple(current, total, fontName)
                        showProgress = true
                    }

                    "org.elnix.dragonlauncher.ACTION_DOWNLOAD_COMPLETE" -> {
                        val downloaded = intent.getIntExtra("DOWNLOADED", 0)
                        val total = intent.getIntExtra("TOTAL", 0)
                        logD(FONT_PROVIDER) { "Batch Download Complete: $downloaded/$total" }
                        refreshTrigger = (refreshTrigger + 1) % 1000
                        showProgress = false
                        downloadProgress = null
                        ctx.showToast("Download finished: $downloaded/$total fonts saved.")
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction("org.elnix.dragonlauncher.ACTION_FONTS_RESULT")
            addAction("org.elnix.dragonlauncher.ACTION_DOWNLOAD_COMPLETE")
            addAction("org.elnix.dragonlauncher.ACTION_DOWNLOAD_PROGRESS")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ctx.applicationContext.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            ContextCompat.registerReceiver(
                ctx.applicationContext,
                receiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
        }
        onDispose {
            try {
                ctx.applicationContext.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
        }
    }

    val availableFonts = remember(refreshTrigger) {
        val base = listOf("Default", "Serif", "SansSerif", "Monospace", "Cursive")
        val extFonts = try {
            val extDir = File(ctx.getExternalFilesDir(null), "fonts")
            if (extDir.exists()) {
                extDir.listFiles { file -> file.extension == "ttf" || file.extension == "otf" }
                    ?.map { it.nameWithoutExtension }
                    ?.sorted() ?: emptyList()
            } else emptyList()
        } catch (_: Exception) {
            emptyList()
        }
        base + extFonts
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                scope.launch {
                    try {
                        val fileName =
                            ctx.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                                val nameIndex =
                                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                cursor.moveToFirst()
                                cursor.getString(nameIndex)
                            } ?: "custom_font_${System.currentTimeMillis()}.ttf"
                        val destDir = File(ctx.getExternalFilesDir(null), "fonts")
                        if (!destDir.exists()) destDir.mkdirs()
                        val destFile = File(destDir, fileName)
                        ctx.contentResolver.openInputStream(it)?.use { input ->
                            FileOutputStream(destFile).use { output -> input.copyTo(output) }
                        }
                        val fontName = fileName.substringBeforeLast(".")
                        UiSettingsStore.globalFont.set(ctx, fontName)
                        refreshTrigger = (refreshTrigger + 1) % 1000
                        ctx.showToast("Font $fontName added!")
                    } catch (e: Exception) {
                        logE(FONT_PROVIDER, e) { "Error importing font" }
                        ctx.showToast("Error importing font")
                    }
                }
            }
        }
    )

    val categories = remember(remoteFonts.size) {
        listOf("ALL") + remoteFonts.map { it.value }.distinct().sorted()
    }


    fun fetchRemoteFonts(clearExisting: Boolean) {
        scope.launch(Dispatchers.IO) {
            isFetchingRemote = true
            logD(FONT_PROVIDER) { "Fetching remote fonts" }
            try {
                val providerUri =
                    "content://org.elnix.dragonlauncher.fonts.provider/fonts".toUri()
                logD(FONT_PROVIDER) { "Querying URI: $providerUri" }

                val cursor = queryWithRetry(ctx, providerUri)

                logD(FONT_PROVIDER) { "Cursor result: $cursor, count: ${cursor?.count}" }

                if (clearExisting) {
                    remoteFonts.clear()
                }

                cursor?.use {
                    logD(FONT_PROVIDER) { "Cursor count: ${it.count}" }


                    val nameIdx = it.getColumnIndex("name")
                    val catIdx = it.getColumnIndex("category")
                    if (nameIdx != -1) {
                        while (it.moveToNext()) {
                            val name = it.getString(nameIdx)
                            val category =
                                if (catIdx != -1) it.getString(
                                    catIdx
                                ).uppercase() else "UNKNOWN"
                            remoteFonts += name to category
                        }
                    }
                }
                refreshTrigger = (refreshTrigger + 1) % 1000

            } catch (e: SecurityException) {
                logE(FONT_PROVIDER, e) { "Security exception: ${e.message}" }
            } catch (e: Exception) {
                logE(FONT_PROVIDER, e) { "Refresh Error" }
            } finally {
                isFetchingRemote = false
            }
        }
    }


    SettingsScaffold(
        title = stringResource(R.string.font_selector),
        onBack = onBack,
        helpText = stringResource(R.string.font_manage_help),
        onReset = null,
        resetText = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val catResId = when (cat) {
                        "ALL" -> R.string.font_cat_all
                        "SERIF" -> R.string.font_cat_serif
                        "SANS-SERIF" -> R.string.font_cat_sans_serif
                        "MONOSPACE" -> R.string.font_cat_monospace
                        "DISPLAY" -> R.string.font_cat_display
                        "HANDWRITING" -> R.string.font_cat_handwriting
                        else -> -1
                    }
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                if (catResId != -1) stringResource(catResId) else cat,
                                fontSize = 11.sp
                            )
                        },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.5f
                            ),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = null
                    )
                }
            }

            val interactionSources = List(5) { rememberInteractionSource() }

            AnimatedContent(isDeleteMode) {
                ButtonGroup(
                    overflowIndicator = { menuState -> ButtonGroupDefaults.OverflowIndicator(menuState) },
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!it) {
                        customItem(
                            buttonGroupContent = {
                                Button(
                                    onClick = {
                                        val intent =
                                            Intent("org.elnix.dragonlauncher.ACTION_DOWNLOAD_ALL").apply {
                                                setPackage(Constants.Extensions.FONT_EXTENSION_PKG)
                                                putExtra("FORCE_FOREGROUND", true)
                                            }
                                        try {
                                            ctx.startForegroundService(intent)
                                            ctx.showToast(ctx.getString(R.string.font_download_all_started))
                                        } catch (e: Exception) {
                                            logE(FONT_PROVIDER, e) { "Unable to start foreground service" }
                                            ctx.showToast("Unable to start foreground service: ${e.message}")
                                        }
                                    },
                                    interactionSource = interactionSources[0],
                                    modifier = Modifier
                                        .weight(3f)
                                        .animateWidth(interactionSources[0]),
                                    shapes = ButtonDefaults.shapes(),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.download),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(6.dp)
                                    Text(
                                        stringResource(R.string.font_catalog),
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            },
                            menuContent = {}
                        )
                        customItem(
                            buttonGroupContent = {
                                OutlinedButton(
                                    onClick = {
                                        filePickerLauncher.launch(
                                            arrayOf(
                                                "font/ttf",
                                                "font/otf",
                                                "application/x-font-ttf",
                                                "application/x-font-otf",
                                                "application/octet-stream"
                                            )
                                        )
                                    },
                                    interactionSource = interactionSources[1],
                                    modifier = Modifier
                                        .weight(3f)
                                        .animateWidth(interactionSources[1]),
                                    shapes = ButtonDefaults.shapes(),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.add),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(6.dp)
                                    Text(
                                        stringResource(R.string.font_import),
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            },
                            menuContent = {}
                        )

                        customItem(
                            buttonGroupContent = {
                                IconButton(
                                    onClick = { isDeleteMode = true },
                                    interactionSource = interactionSources[2],
                                    modifier = Modifier
                                        .weight(1f)
                                        .animateWidth(interactionSources[2]),
                                    colors = AppObjectsColors.cancelIconButtonColors(),
                                    shapes = IconButtonDefaults.shapes()
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.delete_forever),
                                        contentDescription = null
                                    )
                                }
                            },
                            menuContent = {}
                        )

                    } else {

                        customItem(
                            buttonGroupContent = {
                                Button(
                                    onClick = {
                                        if (selectedFontsToDelete.isNotEmpty()) {
                                            scope.launch {
                                                try {
                                                    val extDir =
                                                        File(ctx.getExternalFilesDir(null), "fonts")
                                                    selectedFontsToDelete.forEach { font ->
                                                        File(extDir, "$font.ttf").delete()
                                                        File(extDir, "$font.otf").delete()
                                                    }
                                                    selectedFontsToDelete.clear()
                                                    isDeleteMode = false
                                                    refreshTrigger = (refreshTrigger + 1) % 1000
                                                    ctx.showToast("Selected fonts deleted")
                                                } catch (e: Exception) {
                                                    logE(
                                                        FONT_PROVIDER,
                                                        e
                                                    ) { "Error deleting fonts" }
                                                    ctx.showToast("Error deleting fonts")
                                                }
                                            }
                                        } else {
                                            isDeleteMode = false
                                        }
                                    },
                                    interactionSource = interactionSources[3],
                                    modifier = Modifier
                                        .weight(4f)
                                        .animateWidth(interactionSources[3]),
                                    contentPadding = PaddingValues(
                                        horizontal = 8.dp,
                                        vertical = 8.dp
                                    ),
                                    shapes = ButtonDefaults.shapes(),
                                    colors = AppObjectsColors.cancelButtonColors()
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = null
                                    )
                                    Spacer(6.dp)
                                    Text(
                                        stringResource(
                                            R.string.font_confirm_delete,
                                            selectedFontsToDelete.size
                                        ), fontSize = 12.sp, maxLines = 1
                                    )
                                }
                            },
                            menuContent = { }
                        )

                        customItem(
                            buttonGroupContent = {
                                IconButton(
                                    onClick = {
                                        isDeleteMode = false
                                        selectedFontsToDelete.clear()
                                    },
                                    interactionSource = interactionSources[4],
                                    modifier = Modifier
                                        .weight(1f)
                                        .animateWidth(interactionSources[4]),
                                    colors = AppObjectsColors.cancelIconButtonColors(),
                                    shapes = IconButtonDefaults.shapes()
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.cancel),
                                        contentDescription = null
                                    )
                                }
                            },
                            menuContent = { }
                        )
                    }
                }
            }

            if (showProgress && downloadProgress != null) {
                val (current, total, name) = downloadProgress!!
                val progressBase =
                    if (total > 0) current.toFloat() / total.toFloat() else 0f
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(R.string.font_downloading, name),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "$current / $total",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(4.dp)
                    LinearProgressIndicator(
                        progress = { progressBase },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }

            OutlinedTextField(
                value = extensionSearchQuery,
                onValueChange = { extensionSearchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                placeholder = {
                    Text(
                        stringResource(R.string.font_search_placeholder),
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.search),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (isFetchingRemote) {
                        LoadingIndicator(
                            modifier = Modifier.size(20.dp)
                        )
                    } else if (isExtensionInstalled) {
                        IconButton(
                            onClick = { fetchRemoteFonts(true) }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.refresh),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )
        }

        val installedFontsSnapshot = remember(refreshTrigger) {
            val extDir = File(ctx.getExternalFilesDir(null), "fonts")
            if (extDir.exists()) {
                extDir.listFiles { file -> file.extension == "ttf" || file.extension == "otf" }
                    ?.map { it.nameWithoutExtension }
                    ?.toSet() ?: emptySet()
            } else emptySet()
        }

        val filteredLocal = availableFonts.filter {
            it.contains(
                extensionSearchQuery,
                ignoreCase = true
            )
        }
        val filteredRemote = remoteFonts.filter { (name, category) ->
            (selectedCategory == "ALL" || category == selectedCategory) &&
                    name.contains(extensionSearchQuery, ignoreCase = true) &&
                    !installedFontsSnapshot.contains(name)
        }

        LaunchedEffect(isExtensionInstalled, refreshTrigger) {

            logD(FONT_PROVIDER) { "isExtensionInstalled: $isExtensionInstalled, refreshTrigger: $refreshTrigger, remoteFonts: $remoteFonts" }

            if (isExtensionInstalled && remoteFonts.isEmpty()) {
                fetchRemoteFonts(clearExisting = false)
            }
        }


        Column {
            if (filteredLocal.isNotEmpty()) {
                Text(
                    stringResource(R.string.font_installed_fonts),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                filteredLocal.forEach { font ->
                    val isDeletable = font !in listOf(
                        "Default",
                        "Serif",
                        "SansSerif",
                        "Monospace",
                        "Cursive"
                    )
                    FontRow(
                        font = font,
                        isSelected = if (isDeleteMode) selectedFontsToDelete.contains(
                            font
                        ) else globalFontName == font,
                        isInstalled = true,
                        showCheckbox = isDeleteMode && isDeletable
                    ) {
                        if (isDeleteMode) {
                            if (isDeletable) {
                                if (selectedFontsToDelete.contains(font)) selectedFontsToDelete.remove(
                                    font
                                )
                                else selectedFontsToDelete.add(font)
                            }
                        } else {
                            scope.launch {
                                UiSettingsStore.globalFont.set(ctx, font)
                            }
                        }
                    }
                }
            }

            if (filteredRemote.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(
                        vertical = 8.dp,
                        horizontal = 16.dp
                    ), thickness = 0.5.dp
                )
                Text(
                    stringResource(
                        R.string.font_available_in_extension,
                        filteredRemote.size
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                filteredRemote.forEach { (name, _) ->
                    FontRow(
                        font = name,
                        isSelected = globalFontName == name,
                        isInstalled = false
                    ) {
                        val i =
                            Intent("org.elnix.dragonlauncher.ACTION_GET_FONTS").apply {
                                putExtra("FONT_NAME", name)
                                setPackage(Constants.Extensions.FONT_EXTENSION_PKG)
                            }
                        ctx.startService(i)
                        ctx.showToast("Downloading $name...")
                    }
                }
            }
        }
    }
}

@Composable
public fun FontRow(
    font: String,
    isSelected: Boolean,
    isInstalled: Boolean,
    showCheckbox: Boolean = false,
    onClick: () -> Unit
) {
    val ctx = LocalContext.current
    val containerColor =
        if (isSelected && !showCheckbox) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(containerColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showCheckbox) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.error)
            )
        } else if (isInstalled) {
            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = AppObjectsColors.radioButtonColors()
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.download),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }
        Spacer(12.dp)
        val fontFamily = try {
            fontNameToFont(font, ctx)
        } catch (_: Exception) {
            FontFamily.Default
        }
        Text(
            text = font,
            fontFamily = fontFamily,
            color = if (isInstalled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.6f
            ),
            style = MaterialTheme.typography.bodyLarge
        )
        if (!isInstalled) {
            Spacer(Modifier.weight(1f))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            ) {
                Text(
                    "GET",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}


private suspend fun queryWithRetry(ctx: Context, uri: Uri, retries: Int = 3): Cursor? {

    // Wake up the extension process first
//    val wakeIntent = Intent("org.elnix.dragonlauncher.ACTION_GET_FONTS").apply {
//        setPackage("org.elnix.dragonlauncher.fonts")
//    }
//    ctx.startService(wakeIntent)

    // Give it a moment to start
//    delay(300)

    // Now query
    repeat(retries) { attempt ->
        val cursor = ctx.contentResolver.query(uri, null, null, null, null)
        if (cursor != null) return cursor
        logD(FONT_PROVIDER) { "Cursor null, retry ${attempt + 1}/$retries" }
        delay((500L * (attempt + 1)).milliseconds)
    }
    return null
}