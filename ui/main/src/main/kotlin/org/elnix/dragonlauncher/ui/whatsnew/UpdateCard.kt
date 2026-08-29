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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.models.Update
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.components.BaseVersionChip
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BaseVersionChip(
                    text = update.versionName,
                    color = MaterialTheme.colorScheme.primary
                )
                update.codeName?.let { codeName ->
                    BaseVersionChip(
                        text = codeName,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                BaseVersionChip(
                    text = update.versionCode.toString(),
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer()
                Text(
                    text = dateFormatter.format(update.date),
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            Spacer(10.dp)

            update.note?.takeIf { it.isNotEmpty() }?.let {
                UpdateSection(
                    title = stringResource(R.string.note),
                    items = it
                )
            }

            update.whatsNew?.takeIf { it.isNotEmpty() }?.let {
                UpdateSection(
                    title = stringResource(R.string.new_string),
                    items = it
                )
            }

            update.improved?.takeIf { it.isNotEmpty() }?.let {
                UpdateSection(
                    title = stringResource(R.string.improvements),
                    items = it
                )
            }

            update.fixed?.takeIf { it.isNotEmpty() }?.let {
                UpdateSection(
                    title = stringResource(R.string.fixes),
                    items = it
                )
            }

            update.knownIssues?.takeIf { it.isNotEmpty() }?.let {
                UpdateSection(
                    title = stringResource(R.string.known_issues),
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
        style = MaterialTheme.typography.titleSmallEmphasized,
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
