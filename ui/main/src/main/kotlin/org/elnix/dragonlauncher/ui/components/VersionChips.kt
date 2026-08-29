package org.elnix.dragonlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
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
fun BaseVersionChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = contentColorFor(color),
        style = MaterialTheme.typography.labelMediumEmphasized,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier
            .wrapContentSize()
            .clip(MaterialTheme.shapes.small)
            .background(color)
            .then(modifier)
            .padding(6.dp)
    )
}

@Composable
fun VersionNumberChip(modifier: Modifier = Modifier) {
    val versionNumber = LocalContext.current.getVersionNumber()
    BaseVersionChip(
        text = versionNumber,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

@Composable
fun CodeNameChip(modifier: Modifier = Modifier) {
    val codeName = LocalContext.current.getCodeName()
    BaseVersionChip(
        text = codeName,
        color = MaterialTheme.colorScheme.secondary,
        modifier = modifier
    )
}


@Composable
fun VersionCodeChip(modifier: Modifier = Modifier) {
    val versionCode = LocalContext.current.getVersionCode()
    BaseVersionChip(
        text = versionCode.toString(),
        color = MaterialTheme.colorScheme.tertiary,
        modifier = modifier
    )
}


@Composable
fun BuildTypeChip(modifier: Modifier = Modifier) {
    val buildType = LocalContext.current.getBuildType()
    BaseVersionChip(
        text = buildType,
        color = MaterialTheme.colorScheme.tertiaryFixed,
        modifier = modifier
    )
}