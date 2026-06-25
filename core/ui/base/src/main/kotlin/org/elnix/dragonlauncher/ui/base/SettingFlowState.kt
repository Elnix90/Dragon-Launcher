package org.elnix.dragonlauncher.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.elnix.dragonlauncher.base.SettingFlow


@Composable
fun <T> SettingFlow<T>.asState(): State<T> = this.flow.collectAsStateWithLifecycle()
