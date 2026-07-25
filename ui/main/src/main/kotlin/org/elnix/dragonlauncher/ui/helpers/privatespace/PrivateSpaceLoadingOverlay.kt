package org.elnix.dragonlauncher.ui.helpers.privatespace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.base.modifiers.shapedClickable

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PrivateSpaceLoadingOverlay(
    onCancel: () -> Unit,
) {
    // TODO make this down scrollable to dismiss

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .windowInsetsPadding(WindowInsets.safeDrawing.exclude(WindowInsets.ime)),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {

            Icon(
                painter = painterResource(R.drawable.lock),
                contentDescription = stringResource(R.string.private_space_locked),
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )


            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.private_space_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.private_space_authenticating),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            LoadingIndicator(
                modifier = Modifier.size(70.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Icon(
            painter = painterResource(R.drawable.close),
            contentDescription = stringResource(R.string.close),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
                .shapedClickable(onClick = onCancel)
                .padding(30.dp),
            tint = MaterialTheme.colorScheme.error
        )
    }
}
