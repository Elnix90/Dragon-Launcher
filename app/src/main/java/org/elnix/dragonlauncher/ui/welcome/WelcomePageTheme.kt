package org.elnix.dragonlauncher.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.DataStoreName
import org.elnix.dragonlauncher.data.helpers.ThemeObject
import org.elnix.dragonlauncher.utils.SettingsBackupManager
import org.elnix.dragonlauncher.utils.colors.AppObjectsColors
import org.elnix.dragonlauncher.utils.loadThemes

@Composable
fun WelcomePageTheme() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var themes by remember { mutableStateOf<List<ThemeObject>?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        themes = loadThemes(ctx)
        loading = false
    }

    WelcomePagerHeader(
        title = stringResource(R.string.theme_selector),
    icon = Icons.Default.ColorLens
    ) {
        if (loading) {
            Text("Loading themes...")
        } else if (themes != null) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(themes!!) { theme ->
                    Card(
                        colors = AppObjectsColors.cardColors(),
                        modifier = Modifier
                            .clickable {
                                scope.launch {
                                    SettingsBackupManager.importSettingsFromJson(
                                        ctx,
                                        theme.json,
                                        DataStoreName.entries
                                    )
                                }
                            }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Image(
                                painter = painterResource(theme.imageResId),
                                contentDescription = theme.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )

                            Text(
                                text = theme.name,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
