package org.elnix.dragonlauncher.common.messyfolder

import java.util.Date

data class Update(
    val versionCode: Int,
    val versionName: String,
    val date: Date,
    val note: List<String>?,
    val knownIssues: List<String>?,
    val whatsNew: List<String>?,
    val fixed: List<String>?,
    val improved: List<String>?,
)
