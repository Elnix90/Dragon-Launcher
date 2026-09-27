@file:Suppress("UnusedReceiverParameter", "NOTHING_TO_INLINE")

package org.elnix.dragonlauncher.ui.base.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
inline fun RowScope.Spacer() {
	Spacer(Modifier.weight(1f))
}

@Composable
inline fun ColumnScope.Spacer() {
	Spacer(Modifier.weight(1f))
}

@Composable
inline fun RowScope.Spacer(width: Dp) {
	Spacer(Modifier.width(width))
}

@Composable
inline fun ColumnScope.Spacer(height: Dp) {
	Spacer(Modifier.height(height))
}
