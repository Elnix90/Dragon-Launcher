package org.elnix.dragonlauncher.ui.dragon.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun TextWithDescription(
    text: String,
    description: String?,
    modifier: Modifier = Modifier
) = Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(5.dp)
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium
    )
    if (description != null) {
        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall
        )
    }
}



@Composable
fun TextWithDescription(
    text: String,
    description1: String?,
    description2: String?,
    modifier: Modifier = Modifier
) = Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(5.dp)
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium
    )
    if (description1 != null) {
        Text(
            text = description1,
            style = MaterialTheme.typography.labelSmall
        )
    }

    if (description2 != null) {
        Text(
            text = description2,
            style = MaterialTheme.typography.labelSmall
        )
    }
}