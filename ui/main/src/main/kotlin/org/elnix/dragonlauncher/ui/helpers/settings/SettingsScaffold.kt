package org.elnix.dragonlauncher.ui.helpers.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.components.burger.MoreOptions
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScaffold(
    title: String,
    helpText: String,
    onReset: (() -> Unit)?,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    moreOptions: ((() -> Unit) -> List<MoreOptions>)? = null,
    horizontalPadding: Dp = 16.dp,
    applyPadding: Boolean = true,
    resetTitle: String = stringResource(R.string.reset_default_settings),
    resetText: String?,
    lasyListState: LazyListState? = null,
    scrollState: ScrollState? = null,
    topContent: @Composable (ColumnScope.() -> Unit)? = null,
    bottomContent: @Composable (ColumnScope.() -> Unit)? = null,
    specialSettingsTitle: @Composable (() -> Unit)? = null,
    scrollableContent: Boolean = true,
    imePadding: Boolean = true,
    lazyContent: (LazyListScope.() -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null
) {

    var showHelpDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    requireNotNull(
        content ?: lazyContent
    ) { "Must provide exactly one of content or lazyContent, not both or neither" }


    val navigator = LocalNavigator.current
    val handleBack = onBack ?: {
        navigator.onBack()
    }
    BackHandler(onBack = handleBack)

    Scaffold(
        containerColor = Color.Transparent,
        modifier = modifier
            .fillMaxSize()
            .conditional(imePadding) {
                imePadding()
            },
        contentWindowInsets = WindowInsets.statusBarsIgnoringVisibility.add(WindowInsets(left = horizontalPadding, right = horizontalPadding)),
        bottomBar = {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if (bottomContent != null) {
                    bottomContent()
                    Spacer(5.dp)
                }

                if (specialSettingsTitle != null) {
                    specialSettingsTitle()
                } else {
                    SettingsTitle(
                        title = title,
                        moreOptions = moreOptions,
                        helpIcon = { showHelpDialog = true },
                        resetIcon = if (onReset != null) {
                            { showResetDialog = true }
                        } else null,
                    ) { handleBack() }
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .conditional(applyPadding) {
                    padding(paddingValues)
                        .fillMaxSize()
                }
        ) {
            if (topContent != null) {
                topContent()
            }

            if (lazyContent != null) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize(),
                    state = lasyListState ?: rememberLazyListState()
                ) { lazyContent() }

            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .conditional(scrollableContent) {
                            verticalScroll(scrollState ?: rememberScrollState())
                        }
                ) { content!!() }
            }
        }
    }

    if (showHelpDialog) {
        UserValidation(
            title = "$title ${stringResource(R.string.help)}",
            message = helpText,
            validateText = stringResource(R.string.close),
            titleIcon = R.drawable.help,
            titleColor = MaterialTheme.colorScheme.onSurfaceVariant,
            titleBgColor = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            showHelpDialog = false
        }
    }
    if (showResetDialog && resetText != null && onReset != null) {
        UserValidation(
            title = resetTitle,
            message = resetText,
            onDismiss = { showResetDialog = false }
        ) {
            onReset()
            showResetDialog = false
        }
    }
}
