package org.elnix.dragonlauncher.ui.whatsnew

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun UpdateCard(update: Update) {
    val dateFormatter = rememberDateFormatter()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = dateFormatter.format(update.date),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Version ${update.versionName} (${update.versionCode})",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

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
private fun UpdateSection(
    title: String,
    items: List<String>
) {
    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onPrimary
    )

    Spacer(modifier = Modifier.height(4.dp))

    items.forEach { item ->
        Text(
            text = "• $item",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun rememberDateFormatter(): SimpleDateFormat =
    SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
