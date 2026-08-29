package org.elnix.dragonlauncher.ui.whatsnew

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.models.Update
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.components.CodeNameChip
import org.elnix.dragonlauncher.ui.components.VersionCodeChip
import org.elnix.dragonlauncher.ui.components.VersionNumberChip
import java.text.SimpleDateFormat

@Composable
fun UpdateCard(
    update: Update,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val dateFormatter = rememberDateFormatter()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = dateFormatter.format(update.date),
                style = MaterialTheme.typography.labelMedium,
            )

            Spacer(4.dp)

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VersionNumberChip()
                CodeNameChip()
                VersionCodeChip()
            }

            Text(
                text = "Version ${update.versionName} (${update.versionCode})",
                style = MaterialTheme.typography.headlineSmallEmphasized
            )

            Spacer(12.dp)

            update.note?.takeIf { it.isNotEmpty() }?.let {
                UpdateSection(
                    title = "Note",
                    items = it
                )
            }

            update.whatsNew?.takeIf { it.isNotEmpty() }?.let {
                UpdateSection(
                    title = "What’s new",
                    items = it
                )
            }

            update.improved?.takeIf { it.isNotEmpty() }?.let {
                UpdateSection(
                    title = "Improvements",
                    items = it
                )
            }

            update.fixed?.takeIf { it.isNotEmpty() }?.let {
                UpdateSection(
                    title = "Fixes",
                    items = it
                )
            }

            update.knownIssues?.takeIf { it.isNotEmpty() }?.let {
                UpdateSection(
                    title = "Known issues",
                    items = it
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.UpdateSection(
    title: String,
    items: List<String>
) {
    Spacer(8.dp)

    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        textDecoration = TextDecoration.Underline
    )

    Spacer(4.dp)

    items.forEach { item ->
        Text(
            text = "• $item",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun rememberDateFormatter(): SimpleDateFormat =
    SimpleDateFormat("MMMM d, yyyy", LocalLocale.current.platformLocale)
