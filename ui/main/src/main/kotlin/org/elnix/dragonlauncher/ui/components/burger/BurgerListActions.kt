package org.elnix.dragonlauncher.ui.components.burger

import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.util.fastForEachIndexed


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BurgerListAction(
    actions: List<MoreOptions>,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit
) {
    DropdownMenuPopup(
        expanded = isExpanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        DropdownMenuGroup(
            shapes = MenuDefaults.groupShapes()
        ) {
            actions.fastForEachIndexed { index, option ->
                DropdownMenuItem(
                    onClick = {
                        option.onClick()
                        onDismissRequest()
                    },
                    enabled = option.enabled,
                    shape = when (index) {
                        0 -> MenuDefaults.leadingItemShape
                        actions.lastIndex -> MenuDefaults.trailingItemShape
                        else -> MenuDefaults.middleItemShape
                    },
                    text = {
                        if (option.enabled) {
                            Text(option.text())
                        } else {
                            Text(option.disabledText!!())
                        }
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(option.icon),
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

