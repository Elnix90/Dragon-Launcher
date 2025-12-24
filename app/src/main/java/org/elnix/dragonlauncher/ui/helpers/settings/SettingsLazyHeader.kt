package org.elnix.dragonlauncher.ui.helpers.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.reorderable
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.ui.helpers.UserValidation

@Suppress("AssignedValueIsNeverRead")
@Composable
fun SettingsLazyHeader(
    title: String,
    onBack: () -> Unit,
    helpText: String,
    onReset: (() -> Unit)?,
    modifier: Modifier = Modifier,
    resetTitle: String = stringResource(R.string.reset_default_settings),
    resetText: String? = stringResource(R.string.reset_settings_in_this_tab),
    reorderState: ReorderableLazyListState? = null,
    banner: @Composable (() -> Unit)? = null,
    titleContent: (LazyListScope.() -> Unit)? = null,
    bottomContent: (LazyListScope.() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null,
    lazyContent: (LazyListScope.() -> Unit)? = null
) {

    var showHelpDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    requireNotNull(
        content ?: lazyContent
    ) { "Must provide exactly one of content or lazyContent, not both or neither" }


    Column(
        modifier = Modifier
            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.background)
    ) {

        if (banner != null) { banner() }

        Column (
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LazyColumn (
                modifier = Modifier
                    .fillMaxWidth(),
//                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                item {
                    SettingsTitle(
                        title,
                        helpIcon = { showHelpDialog = true },
                        resetIcon = if (onReset != null) {
                            { showResetDialog = true }
                        } else null,
                    ) { onBack() }
                }

                if (titleContent != null) titleContent()
            }

            if (lazyContent != null) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(bottom = if (bottomContent != null) 0.dp else 400.dp),
                    modifier = if (reorderState != null) {
                        modifier
                            .reorderable(reorderState)
                            .detectReorderAfterLongPress(reorderState)
                    } else modifier,
                    state = reorderState?.listState ?: rememberLazyListState()
                ) {
                    lazyContent()
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = if (reorderState != null) {
                        modifier
                            .reorderable(reorderState)
                            .detectReorderAfterLongPress(reorderState)
                    } else modifier,
                ) {
                    content!!()
                }
            }

            bottomContent?.let {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    it()
                }
                Spacer(Modifier.height(400.dp))
            }
        }
    }

    if (showHelpDialog) {
        UserValidation(
            title = "$title ${stringResource(R.string.help)}",
            message = helpText,
            cancelText = null,
            onCancel = { showHelpDialog = false },
            titleIcon = Icons.AutoMirrored.Filled.Help,
            titleColor = MaterialTheme.colorScheme.onSurface
        ) {
            showHelpDialog = false
        }
    }
    if (showResetDialog && resetText != null && onReset != null) {
        UserValidation(
            title = resetTitle,
            message = resetText,
            onCancel = { showResetDialog = false }
        ) {
            onReset()
            showResetDialog = false
        }
    }
}
