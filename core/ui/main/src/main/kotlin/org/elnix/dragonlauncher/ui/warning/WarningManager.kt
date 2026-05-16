/*
 * Copyright (C) 2026  Shubham Gorai
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.elnix.dragonlauncher.ui.warning

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

// Cloned from https://github.com/shub39/Grit/blob/89c2bb9a8c3b9bc66262ced9e378a8103a6d7a61/app/src/main/java/com/shub39/grit/warning/WarningManager.kt

object WarningManager {
    fun showWarning(): Boolean = (getDaysLeft() >= 0)

    private val _showWarningDialog = MutableStateFlow(showWarning())

    val showWarningDialog = _showWarningDialog.asStateFlow()

    fun updateWarningDialog(newValue: Boolean) {
        _showWarningDialog.update { newValue }
    }

    fun getDaysLeft(): Int {
        return Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .daysUntil(LocalDate(year = 2026, month = 9, day = 1))
    }
}
