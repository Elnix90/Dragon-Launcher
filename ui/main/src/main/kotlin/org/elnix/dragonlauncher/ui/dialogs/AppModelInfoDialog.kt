package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.getInstallSource
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription

@Composable
fun ApplicationInfoDialog(
    app: Application,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current

    val installerPackage = remember { ctx.getInstallSource(app.packageName) }

    AlertDialog(
        text = {
            Column {
                Text(stringResource(R.string.app_info_name, app.label)) { ctx.copyToClipboard(app.label) }
                Text(stringResource(R.string.app_info_package_name, app.packageName)) { ctx.copyToClipboard(app.packageName) }
                Text(stringResource(R.string.profile, app.profile.toString()))
                Text(stringResource(R.string.app_info_is_system, app.isSystem.toString()))
                Text(stringResource(R.string.app_info_is_work_profile, app.isWork.toString()))
                Text(stringResource(R.string.app_info_is_private_profile, app.isPrivate.toString()))
                Text(stringResource(R.string.app_info_is_launchable, app.isLaunchable.toString()))
                Text(stringResource(R.string.app_info_cache_key, app.key.cacheKey)) { ctx.copyToClipboard(app.key.cacheKey) }
                TextWithDescription(
                    text = stringResource(R.string.app_info_installer_package, installerPackage.installingPackageName ?: "<unknown>"),
                    description1 = stringResource(R.string.app_info_installer_package, installerPackage.initiatingPackageName ?: "<unknown>"),
                    description2 = stringResource(R.string.app_info_installer_package, installerPackage.originatingPackageName ?: "<unknown>")
                )
            }
        },
        dismissButton = {},
        confirmButton = {},
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun Text(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .clickable(onClick = onClick)
    )
}
