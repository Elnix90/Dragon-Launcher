package org.elnix.dragonlauncher.ui.dialogs

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.common.serializables.AppModel
import org.elnix.dragonlauncher.models.AppsViewModel
import org.elnix.dragonlauncher.ui.activityViewModel
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.widgets.LauncherWidgetHolder

@Composable
fun WidgetPickerDialog(
    appsViewModel: AppsViewModel = activityViewModel(),
    onBindCustomWidget: (Int, ComponentName) -> Unit,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    val apps by appsViewModel.allApps.collectAsState()

    val appWidgetManager = remember { AppWidgetManager.getInstance(ctx) }
    val launcherWidgetHolder = remember(ctx) { LauncherWidgetHolder.getInstance(ctx) }

    var widgets by remember { mutableStateOf<List<AppWidgetProviderInfo>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        widgets = appWidgetManager.installedProviders
    }

    val filteredWidgets = remember(searchQuery, widgets) {
        val pm = ctx.packageManager
        if (searchQuery.isEmpty()) {
            widgets
        } else {
            widgets.filter { provider ->
                val widgetLabel = provider.loadLabel(pm)
                val appLabel = try {
                    apps.find { it.packageName == provider.provider.packageName }?.name ?: ""
                } catch (_: Exception) {
                    ""
                }
                widgetLabel.contains(searchQuery, ignoreCase = true) ||
                        appLabel.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(
        onDismissRequest = {  onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.add_widget),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    placeholder = { Text(stringResource(R.string.search_widgets)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.search),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    painter = painterResource(R.drawable.close),
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = DragonShape
                )

                if (filteredWidgets.isEmpty() && searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.no_search_match),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn {
                        items(filteredWidgets) { provider ->
                            WidgetItem(
                                provider = provider,
                                launcherWidgetHolder = launcherWidgetHolder,
                                apps = apps,
                                onBindCustomWidget = onBindCustomWidget,
                                onDismiss = onDismiss
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetItem(
    provider: AppWidgetProviderInfo,
    launcherWidgetHolder: LauncherWidgetHolder,
    apps: List<AppModel>,
    onBindCustomWidget: (Int, ComponentName) -> Unit,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    val density = LocalDensity.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                val widgetId = launcherWidgetHolder.allocateAppWidgetId()

                onBindCustomWidget(widgetId, provider.provider)
                onDismiss()
            },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WidgetPreviewImage(
                provider = provider,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))
            Column {
                val appLabel = try {
                    apps.find { it.packageName == provider.provider.packageName }?.name ?: ""
                } catch (_: Exception) {
                    ""
                }

                Text(
                    text = provider.loadLabel(ctx.packageManager),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                if (appLabel.isNotEmpty()) {
                    Text(
                        text = appLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "${provider.minWidth / density.density}x${provider.minHeight / density.density} cells",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WidgetPreviewImage(
    provider: AppWidgetProviderInfo,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current

    var bitmap by remember(provider.previewImage, provider.provider) {
        mutableStateOf<Bitmap?>(null)
    }
    var hasError by remember { mutableStateOf(false) }

    LaunchedEffect(provider.previewImage, provider.provider) {
        bitmap = loadWidgetPreview(provider, ctx)
        if (bitmap == null) hasError = true
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = null,
            modifier = modifier.clip(DragonShape)
        )
    } else if (hasError) {
        AppIconFallback(provider, ctx, modifier)
    } else {
        Box(
            modifier = modifier.clip(DragonShape),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator(modifier = Modifier.size(20.dp))
        }
    }
}

@SuppressLint("UseCompatLoadingForDrawables")
fun loadWidgetPreview(
    provider: AppWidgetProviderInfo,
    ctx: Context
): Bitmap? {
    try {
        val widgetPackage = provider.provider.packageName
        
        // CRITICAL: Block any system resources that might crash on certain ROMs (MIUI/HyperOS)
        // The log showscom.android.systemui:drawable/android15_patch_adaptive specifically failing.
        if (widgetPackage == "com.android.systemui" || 
            widgetPackage == "com.android.settings" ||
            widgetPackage == "android"
        ) {
             return null
        }

        if (provider.previewImage == 0) return null

        val widgetContext = ctx.createPackageContext(widgetPackage, 0)
        val widgetResources = widgetContext.resources

        // Try to load via direct access first (fastest)
        try {
            val drawable = widgetResources.getDrawable(provider.previewImage, null)
            if (drawable is BitmapDrawable) return drawable.bitmap
        } catch (_: Exception) { }

        // Fallback: Open stream directly if possible
        return widgetResources.openRawResource(provider.previewImage).use {
            BitmapFactory.decodeStream(it)
        }
    } catch (_: Exception) {
        return null
    }
}

@Composable
private fun AppIconFallback(
    provider: AppWidgetProviderInfo,
    ctx: Context,
    modifier: Modifier = Modifier
) {
    val appIconBitmap = remember(provider.provider.packageName) {
        try {
            val pm = ctx.packageManager
            val appInfo = pm.getApplicationInfo(provider.provider.packageName, 0)
            val iconDrawable = pm.getApplicationIcon(appInfo)
            (iconDrawable as? BitmapDrawable)?.bitmap
        } catch (_: Exception) {
            null
        }
    }

    val fallbackText = remember(provider) {
        try {
            provider.loadLabel(ctx.packageManager).toString().take(2).uppercase()
        } catch (_: Exception) {
            "?"
        }
    }

    if (appIconBitmap != null) {
        Image(
            bitmap = appIconBitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier.clip(DragonShape)
        )
    } else {
        LetterFallback(text = fallbackText, modifier = modifier)
    }
}



@Composable
private fun LetterFallback(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary)
            .clip(DragonShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
