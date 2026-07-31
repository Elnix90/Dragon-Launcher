package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.alphaMultiplier
import org.elnix.dragonlauncher.models.BackupViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.animation.barsContentTransform
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.dialogs.FullScreenOverlay
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation
import org.elnix.dragonlauncher.ui.svg.vectors.UndrawAlgorithmExecution
import org.elnix.dragonlauncher.ui.svg.vectors.UndrawFilesMissing
import kotlin.random.Random

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LegacyMigrationDialog(
    legacyJsonString: String,
    backupViewModel: BackupViewModel = activityViewModel(),
    onDismiss: () -> Unit
) {
    val migrationResult by backupViewModel.migrationResult.asState()
    var showMigrationAcceptDialog by remember { mutableStateOf(true) }
    var showMigrationResult by remember { mutableStateOf(false) }

    fun triggerMigration() {
        showMigrationAcceptDialog = false

        if (legacyJsonString.isNotBlank()) {
            backupViewModel.migrateFromLegacyBackup(legacyJsonString)
        }
    }

    val progressAnimatable = remember { Animatable(0f) }

    LaunchedEffect(migrationResult) {
        if (migrationResult != null && !showMigrationAcceptDialog) {
            val randomDuration = (2..5).random()
            progressAnimatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(randomDuration * 1000, 200)
            )
            showMigrationResult = true
        }
    }

    when {
        showMigrationAcceptDialog -> {
            UserValidation(
                title = stringResource(R.string.migrate_from_322),
                message = stringResource(R.string.migrate_from_322_desc),
                titleIcon = R.drawable.database_upload,
                titleColor = MaterialTheme.colorScheme.onTertiary,
                titleBgColor = MaterialTheme.colorScheme.tertiary,
                onDismiss = onDismiss,
                onValidate = ::triggerMigration
            )
        }

        migrationResult != null && showMigrationResult -> {
            val result = migrationResult!!
            UserValidation(
                title = stringResource(if (result.success) R.string.migration_complete else R.string.migration_had_issues),
                message = result.message,
                titleIcon = if (result.success) R.drawable.check else R.drawable.warning,
                titleColor = if (result.success) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.error,
                titleBgColor = if (result.success) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                onValidate = {
                    backupViewModel.migrationResult.value = null
                    onDismiss()
                }
            )
        }

        else -> {
            FullScreenOverlay(
                onDismissRequest = {},
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer()

                    var random by remember { mutableStateOf(Random.nextBoolean()) }
                    AnimatedContent(
                        targetState = random,
                        modifier = Modifier
                            .height(200.dp)
                            .clickable(interactionSource = null, indication = null) {
                                random = !random
                            },
                        transitionSpec = { barsContentTransform }
                    ) {
                        Image(
                            imageVector = if (it) UndrawFilesMissing else UndrawAlgorithmExecution,
                            contentDescription = null,
                            modifier = Modifier
                        )
                    }

                    Text(
                        text = stringResource(R.string.migrating_settings),
                        style = MaterialTheme.typography.headlineSmallEmphasized,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .wrapContentWidth()
                            .clip(MaterialTheme.shapes.large)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(12.dp)
                    )

                    Spacer(24.dp)

                    Text(
                        text = stringResource(R.string.migrating_settings_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .wrapContentWidth()
                            .clip(MaterialTheme.shapes.largeIncreased)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(15.dp)
                    )

                    Spacer(30.dp)

                    CircularWavyProgressIndicator(
                        progress = { progressAnimatable.value },
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.secondary.alphaMultiplier(0.2f)
                    )

                    Spacer()

                    Text(
                        text = stringResource(R.string.migrating_settings_desc_kidding),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .wrapContentWidth()
                            .clip(MaterialTheme.shapes.large)
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .padding(10.dp)
                    )
                }
            }
        }
    }
}
