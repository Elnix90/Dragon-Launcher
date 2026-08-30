package org.elnix.dragonlauncher.base.model.models

import androidx.compose.runtime.Immutable
import java.util.Date

@Immutable
public data class Update(
    val versionCode: Int,
    val versionName: String,
    val codeName: String?,
    val date: Date,
    val note: List<String>?,
    val knownIssues: List<String>?,
    val whatsNew: List<String>?,
    val fixed: List<String>?,
    val improved: List<String>?
)
