@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.serializables.AppModel
import org.elnix.dragonlauncher.common.serializables.WorkspaceState
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.composition.LocalAppsViewModel
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppAliasesDialog(
    app: AppModel,
    onDismiss: () -> Unit
) {

    val hapticFeedback = LocalHapticFeedback.current

    val appsViewModel = LocalAppsViewModel.current

    var showAliasEditScreen by remember { mutableStateOf<String?>(null) }
    val cacheKey = app.iconCacheKey

    val state by appsViewModel.enabledState
        .collectAsState(WorkspaceState())

    @Suppress("UselessCallOnNotNull")
    val aliases = state.appAliases.orEmpty()


    AlertDialog(
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.AlternateEmail,
                        contentDescription = stringResource(R.string.app_aliases),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = stringResource(R.string.app_aliases),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                }

                Text(
                    text = app.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        onDismissRequest = onDismiss,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 700.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    contentPadding = PaddingValues(10.dp)
                ) {
                    item(
                        key = "Add tag"
                    ) {
                        Button(
                            modifier = Modifier.animateItem(),
                            onClick = { showAliasEditScreen = "" },
                            shapes = ButtonDefaults.shapes(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = contentColorFor(MaterialTheme.colorScheme.surfaceContainerHigh)
                            )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.add_circle),
                                contentDescription = null
                            )
                        }
                    }
                    val items = aliases[cacheKey]?.toList() ?: emptyList()
                    items(
                        items = items
                    ) { alias ->
                        val interactionSource = rememberInteractionSource()
                        val isPressed by interactionSource.collectIsPressedAsState()


                        var canDelete by remember { mutableStateOf(false) }
                        LaunchedEffect(isPressed) {
                            if (isPressed) {
                                delay(250)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                canDelete = true
                            } else {
                                canDelete = false
                            }
                        }

                        val containerColor by animateColorAsState(
                            if (canDelete) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh
                        )


                        Button(
                            modifier = Modifier.animateItem(),
                            onClick = {
                                if (canDelete) {
                                    appsViewModel.removeAliasFromWorkspace(alias, cacheKey)
                                } else {
                                    showAliasEditScreen = alias
                                }
                            },
                            interactionSource = interactionSource,
                            shapes = ButtonDefaults.shapes(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = containerColor,
                                contentColor = contentColorFor(containerColor)
                            )
                        ) {
                            Text(alias)
                        }
                    }
                }
            }
        },
        confirmButton = {
            ValidateCancelButtons(
                validateText = stringResource(R.string.ok),
                onConfirm = onDismiss
            )
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        shape = DragonShape
    )

    if (showAliasEditScreen != null) {

        val aliasToEdit = showAliasEditScreen!!

        TextEditorDialog(
            title = {
                if (aliasToEdit == "") stringResource(R.string.create_alias)
                else stringResource(R.string.edit_alias)
            },
            placeHolder = { stringResource(R.string.alias) },
            initialText = aliasToEdit,
            onDismiss = { showAliasEditScreen = null }
        ) {
            appsViewModel.removeAliasFromWorkspace(aliasToEdit, cacheKey)
            appsViewModel.addAliasToApp(it, cacheKey)
            showAliasEditScreen = null
        }
    }
}
