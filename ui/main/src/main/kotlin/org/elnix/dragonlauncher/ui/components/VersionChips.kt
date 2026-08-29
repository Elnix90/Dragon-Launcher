package org.elnix.dragonlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.utils.VersionsUtils.getBuildType
import org.elnix.dragonlauncher.base.utils.VersionsUtils.getCodeName
import org.elnix.dragonlauncher.base.utils.VersionsUtils.getVersionCode
import org.elnix.dragonlauncher.base.utils.VersionsUtils.getVersionNumber


@Composable
private fun VersionChipInternal(
    text: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier
) {
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.labelMediumEmphasized,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier
            .wrapContentSize()
            .clip(MaterialTheme.shapes.small)
            .background(bgColor)
            .then(modifier)
            .padding(6.dp)
    )
}

@Composable
fun VersionNumberChip(modifier: Modifier = Modifier) {
    val versionNumber = LocalContext.current.getVersionNumber()
    VersionChipInternal(
        text = versionNumber,
        color = MaterialTheme.colorScheme.onPrimary,
        bgColor = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

@Composable
fun CodeNameChip(modifier: Modifier = Modifier) {
    val codeName = LocalContext.current.getCodeName()
    VersionChipInternal(
        text = codeName,
        color = MaterialTheme.colorScheme.onSecondary,
        bgColor = MaterialTheme.colorScheme.secondary,
        modifier = modifier
    )
}


@Composable
fun VersionCodeChip(modifier: Modifier = Modifier) {
    val versionCode = LocalContext.current.getVersionCode()
    VersionChipInternal(
        text = versionCode.toString(),
        color = MaterialTheme.colorScheme.onTertiary,
        bgColor = MaterialTheme.colorScheme.tertiary,
        modifier = modifier
    )
}


@Composable
fun BuildTypeChip(modifier: Modifier = Modifier) {
    val buildType = LocalContext.current.getBuildType()
    VersionChipInternal(
        text = buildType,
        color = MaterialTheme.colorScheme.onTertiary,
        bgColor = MaterialTheme.colorScheme.tertiary,
        modifier = modifier
    )
}