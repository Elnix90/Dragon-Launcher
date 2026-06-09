package org.elnix.dragonlauncher.logging

/** Used to force consistency across logs, to avoid Yoan's vibecoding to create logs with hard coded tags */
@JvmInline
value class LogTag internal constructor(
    val tag: String
)
