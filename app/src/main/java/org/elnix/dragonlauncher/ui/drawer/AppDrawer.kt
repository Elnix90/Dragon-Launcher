package org.elnix.dragonlauncher.ui.drawer

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.data.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.utils.AppDrawerViewModel
import org.elnix.dragonlauncher.utils.actions.launchSwipeAction

@Suppress("AssignedValueIsNeverRead")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppDrawerScreen(
    appsViewModel: AppDrawerViewModel,
    showIcons: Boolean,
    searchBarBottom: Boolean,
    onClose: () -> Unit
) {
    val ctx = LocalContext.current
    val apps by appsViewModel.userApps.collectAsState()
    val icons by appsViewModel.icons.collectAsState()

    val autoLaunchSingleMatch by DrawerSettingsStore.getAutoLaunchSingleMatch(ctx)
        .collectAsState(initial = true)


    var query by remember { mutableStateOf(TextFieldValue("")) }
    var dialogApp by remember { mutableStateOf<AppModel?>(null) }

    val filtered = remember(query.text, apps) {
        apps.filter { it.name.contains(query.text, ignoreCase = true) }
    }

    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        awaitFrame()
        focusRequester.requestFocus()
        delay(50)
        keyboard?.show()
    }

    LaunchedEffect(filtered) {
        if (autoLaunchSingleMatch && filtered.size == 1) {
            launchSwipeAction(ctx, filtered.first().action)
            onClose()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets().asPaddingValues())
            .imePadding()
            .padding(15.dp)
    ) {

        if (!searchBarBottom) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .focusable(true),
                placeholder = { Text("Search apps…") },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White
                )
            )

            Spacer(Modifier.height(12.dp))
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, _ -> }  // Consume drag
                }
        ) {
            items(filtered) { app ->
                AppItem(
                    app = app,
                    showIcons = showIcons,
                    icons = icons,
                    onClick = { launchSwipeAction(ctx, app.action); onClose() },
                    onLongClick = { dialogApp = app }
                )
            }
        }

        if (searchBarBottom) {

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .focusable(true),
                placeholder = { Text("Search apps…") },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White
                )
            )

        }

    }

    if (dialogApp != null) {
        val app = dialogApp!!
        AppLongPressDialog(
            app = app,
            onDismiss = {
                dialogApp = null
            },
            onOpen = {
                launchSwipeAction(ctx, app.action)
                onClose()
            },
            onSettings = {
                val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${app.packageName}")
                }
                ctx.startActivity(i)
                onClose()
            },
            onUninstall = {
                val intent = Intent(Intent.ACTION_DELETE).apply {
                    data = Uri.parse("package:${app.packageName}")
                }
                ctx.startActivity(intent)
                onClose()
            }
        )
    }
}
