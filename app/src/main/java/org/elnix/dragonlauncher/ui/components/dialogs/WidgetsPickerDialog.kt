package org.elnix.dragonlauncher.ui.components.dialogs

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.res.ResourcesCompat
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.utils.models.WidgetsViewModel

@Composable
fun WidgetPickerDialog(
    widgetsViewModel: WidgetsViewModel,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    val appWidgetManager = remember { AppWidgetManager.getInstance(ctx) }
    val appWidgetHost = remember { AppWidgetHost(ctx, R.id.appwidget_host_id) }

    var widgets by remember { mutableStateOf<List<AppWidgetProviderInfo>>(emptyList()) }

    LaunchedEffect(Unit) {
        widgets = appWidgetManager.installedProviders
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
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn {
                    items(widgets) { provider ->
                        WidgetItem(
                            provider = provider,
                            appWidgetHost = appWidgetHost,
                            onAddWidget = {
                                widgetsViewModel.addWidget(it)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetItem(
    provider: AppWidgetProviderInfo,
    appWidgetHost: AppWidgetHost,
    onAddWidget: (appWidgetId: Int) -> Unit
) {
    val ctx = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable{
                val appWidgetId = appWidgetHost.allocateAppWidgetId()
                onAddWidget(appWidgetId)
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
                Text(
                    text = provider.loadLabel(ctx.packageManager),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${provider.minWidth / ctx.resources.displayMetrics.density.toInt()}x${provider.minHeight / ctx.resources.displayMetrics.density.toInt()} cells",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WidgetPreviewImage(
    provider: AppWidgetProviderInfo,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current

    var bitmap by remember(provider.previewImage, provider.provider) {
        mutableStateOf<android.graphics.Bitmap?>(null)
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
            modifier = modifier.clip(RoundedCornerShape(8.dp))
        )
    } else if (hasError) {
        AppIconFallback(provider, ctx, modifier)
    } else {
        Box(
            modifier = modifier.clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
        }
    }
}

private fun loadWidgetPreview(
    provider: AppWidgetProviderInfo,
    context: android.content.Context
): android.graphics.Bitmap? {
    return try {
        val widgetPackage = provider.provider.packageName
        val widgetContext = context.createPackageContext(widgetPackage, 0)
        val widgetResources = widgetContext.resources

        // Try multiple loading methods
        listOf(
            // Method 1: ResourcesCompat
            { ResourcesCompat.getDrawable(widgetResources, provider.previewImage, null) },
            // Method 2: Direct resource lookup
            { widgetResources.getDrawable(provider.previewImage, null) }
        ).firstNotNullOfOrNull { loader ->
            try {
                val drawable = loader()
                if (drawable is BitmapDrawable) drawable.bitmap else null
            } catch (_: Exception) { null }
        } ?: run {
            // Method 3: Raw resource stream
            widgetResources.openRawResource(provider.previewImage).use {
                android.graphics.BitmapFactory.decodeStream(it)
            }
        }
    } catch (_: Exception) {
        null
    }
}

@Composable
private fun AppIconFallback(
    provider: AppWidgetProviderInfo,
    ctx: android.content.Context,
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
            modifier = modifier.clip(RoundedCornerShape(8.dp))
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
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
