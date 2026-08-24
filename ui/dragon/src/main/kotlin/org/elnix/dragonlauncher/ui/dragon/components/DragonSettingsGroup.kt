package org.elnix.dragonlauncher.ui.dragon.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.core.objects.SettingObject
import org.elnix.dragonlauncher.ktx.semiTransparentIfDisabled
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.dragon.text.SettingsWithTitle

sealed class DragonSettingGroupItems {
    data class Setting(val setting: SettingObject<*, *>) : DragonSettingGroupItems()
    data class Item(val title: String) : DragonSettingGroupItems()
}


private val bigRounding = 24.dp
private val smallRounding = 6.dp

private val firstShape = RoundedCornerShape(topStart = bigRounding, topEnd = bigRounding, bottomStart = smallRounding, bottomEnd = smallRounding)
private val lastShape = RoundedCornerShape(topStart = smallRounding, topEnd = smallRounding, bottomStart = bigRounding, bottomEnd = bigRounding)
private val middleShape = RoundedCornerShape(smallRounding)
private val singleShape = RoundedCornerShape(bigRounding)


class DragonGroupScope
internal constructor(
    columnScope: ColumnScope
) : ColumnScope by columnScope { // OMG I DISCOVERED THIS SYNTAX TODAY AND ITS WAY TOO COOOOL

//    private val itemList: MutableList<DragonSettingGroupItems> = mutableListOf()

//    private fun new(item: DragonSettingGroupItems) {
//        logWtf { "Registering: $item" }
//        val indexOfPrevious = itemList.indexOf(item)
//
//        if (indexOfPrevious != -1) {
//            val failingSetting = itemList[indexOfPrevious]
//            throw IllegalStateException("You added twice the same setting in this block: $failingSetting")
//        }
//
//        itemList.add(item)
//    }
//
//    @Composable
//    fun Register(setting: SettingObject<*, *>, content: @Composable () -> Unit) {
//        LaunchedEffect(true) {
//            val item = DragonSettingGroupItems.Setting(setting)
//            new(item)
//        }
//
//        content()
//    }
//
//    @Composable
//    fun Register(title: String, content: @Composable () -> Unit) {
//        LaunchedEffect(true) {
//            val item = DragonSettingGroupItems.Item(title)
//            new(item)
//        }
//
//        content()
//    }

//
//    private fun Modifier.getShapeFromIndex(index: Int): Modifier {
//
//        logWtf { "Index = $index, size = ${itemList.size}" }
//        val shape = when {
//            itemList.size == 1 -> singleShape
//            index == 0 -> firstShape
//            index == itemList.lastIndex -> lastShape
//            else -> middleShape
//        }
//        return this.clip(shape)
//    }
//
//    /**
//     * Apply the correct shape to the element based on its position in the column of [DragonSettingsGroup]
//     *
//     */
//    fun Modifier.shaped(setting: SettingObject<*, *>): Modifier {
//        val index = itemList.indexOf(DragonSettingGroupItems.Setting(setting))
//        return getShapeFromIndex(index)
//    }
//
//    /**
//     * Apply the correct shape to the element based on its position in the column of [DragonSettingsGroup]
//     *
//     */
//    fun Modifier.shaped(title: String): Modifier {
//        val index = itemList.indexOf(DragonSettingGroupItems.Item(title))
//        return getShapeFromIndex(index)
//    }

    @SuppressLint("UnnecessaryComposedModifier")
    fun Modifier.dragonSettingGroup(
        enabled: Boolean = true,
        clickModifier: (Modifier.() -> Modifier)? = null
    ): Modifier = composed {
        this
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.semiTransparentIfDisabled(enabled))
            .conditional(clickModifier) { it() }
            .padding(10.dp)
    }
}



@Composable
fun DragonSettingsGroup(
    title: Int? = null,
    trailingIcon: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable DragonGroupScope.() -> Unit
) {
   DragonSettingsGroup(
       title = title?.let{ stringResource(title) },
       trailingIcon = trailingIcon,
       content = content
   )
}


@Composable
fun DragonSettingsGroup(
    title: String?,
    trailingIcon: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable DragonGroupScope.() -> Unit
) {
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        SettingsWithTitle(title, trailingIcon) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.largeIncreased)
            ) {
                val dragonGroupScope = remember {
                    DragonGroupScope(this)
                }
                content(dragonGroupScope)
            }
        }
    }
}