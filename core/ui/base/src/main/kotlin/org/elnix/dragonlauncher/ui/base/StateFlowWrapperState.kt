package org.elnix.dragonlauncher.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.elnix.dragonlauncher.models.utils.StateFlowWrapper


@Composable
fun <T> StateFlowWrapper<T>.asState(): State<T> = this.flow.collectAsStateWithLifecycle()
