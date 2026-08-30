package org.elnix.dragonlauncher.ui.helpers.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton

@Composable
fun AppDrawerSearch(
    modifier: Modifier = Modifier,
    drawerViewModel: DrawerViewModel = activityViewModel(),
    placeholderText: String = stringResource(R.string.search_apps),
    trailingIcon: (@Composable () -> Unit)? = null,
    onEnterPressed: (() -> Unit)? = null,
    onFocusStateChanged: ((Boolean) -> Unit)? = null
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var searchQuery by drawerViewModel.searchQuery

    TextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        modifier =
            modifier
                .fillMaxWidth()
                .padding(5.dp)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .onFocusChanged { focusState ->
                    val focused = focusState.isFocused
                    onFocusStateChanged?.invoke(focused) // Notify parent of focus change
                    if (focused) {
                        keyboardController?.show() // Show keyboard when TextField gains focus
                    }
                    // Keyboard hiding on focus loss is handled by system, IME actions, or explicit calls elsewhere (e.g., scroll logic)
                },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.search),
                contentDescription = stringResource(R.string.search_apps),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = trailingIcon,
        placeholder = {
            Text(
                text = placeholderText,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions =
            KeyboardActions(
                onSearch = onEnterPressed?.let { { it() } }
            ),
        colors = AppObjectsColors.outlinedTextFieldColors()
    )
}

@Composable
fun AppShortcutSearch(
    searchQuery: String,
    onValueChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    TextField(
        value = searchQuery,
        onValueChange = onValueChange,
        modifier =
            Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.search),
                contentDescription = stringResource(R.string.search_shortcuts)
            )
        },
        trailingIcon = {
            DragonIconButton(
                icon = R.drawable.close,
                contentDescription = R.string.close,
                isCancel = true,
                enabled = searchQuery.isNotEmpty()
            ) { onValueChange("") }
        },
        placeholder = {
            Text(
                text = stringResource(R.string.search_shortcuts),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        shape = CircleShape,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions =
            KeyboardActions(
                onSearch = { focusManager.clearFocus(true) }
            ),
        colors = AppObjectsColors.outlinedTextFieldColors()
    )
}
