package org.elnix.dragonlauncher.ui.warning

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import org.elnix.dragonlauncher.base.SettingFlow
import kotlin.time.Clock

// Cloned from https://github.com/shub39/Grit/blob/89c2bb9a8c3b9bc66262ced9e378a8103a6d7a61/app/src/main/java/com/shub39/grit/warning/WarningManager.kt

object WarningManager {
    fun showWarning(): Boolean = (getDaysLeft() >= 0)


    val showWarningDialog: SettingFlow<Boolean> = SettingFlow(showWarning())

    fun updateWarningDialog(newValue: Boolean) {
        showWarningDialog.update { newValue }
    }

    fun getDaysLeft(): Int {
        return Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .daysUntil(LocalDate(year = 2027, month = 1, day = 1))
    }
}
