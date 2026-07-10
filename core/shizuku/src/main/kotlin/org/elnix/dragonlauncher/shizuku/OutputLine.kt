package org.elnix.dragonlauncher.shizuku

/**
 * Represents a single line of output from a shell command.
 *
 * @property text The content of the output line.
 * @property isError Indicates whether this line originated from the error stream.
 */
public class OutputLine(
    public val text: String,
    public val isError: Boolean = false
)