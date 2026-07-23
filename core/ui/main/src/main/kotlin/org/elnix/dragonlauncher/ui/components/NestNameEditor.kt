package org.elnix.dragonlauncher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.animation.Icon
import org.elnix.dragonlauncher.ui.base.animation.rememberAnimatedIcon

@Composable
public fun NestNameEditor(
    nest: Nest,
    modifier: Modifier = Modifier,
    onEditName: (String?) -> Unit
) {
    val animatedIcon = rememberAnimatedIcon()
    val focusManager = LocalFocusManager.current

    var tempCustomName by remember { mutableStateOf(nest.name ?: "") }
    TextField(
        value = tempCustomName,
        onValueChange = {
            tempCustomName = it
            onEditName(it)
        },
        placeholder = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.edit_rounded),
                    contentDescription = stringResource(R.string.custom_name)
                )
                Text(stringResource(R.string.custom_name))
            }
        },
        trailingIcon = {
            animatedIcon.Icon(
                defaultIcon = R.drawable.reset,
                enabled = nest.name != null
            ) {
                tempCustomName = ""
                onEditName(null)
                animatedIcon.setSuccess()
                focusManager.clearFocus()
            }
        },
        keyboardActions = KeyboardActions(
            onDone = {
                animatedIcon.setSuccess()
                focusManager.clearFocus()
            }
        ),
        colors = AppObjectsColors.outlinedTextFieldColors(removeBorder = true),
        singleLine = true,
        shape = CircleShape,
        modifier = modifier
    )
}
