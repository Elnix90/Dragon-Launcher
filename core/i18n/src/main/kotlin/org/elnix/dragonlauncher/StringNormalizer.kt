package org.elnix.dragonlauncher

public interface StringNormalizer {
    /**
     * A unique identifier for the normalization algorithm. Two normalizers that share the same ID must
     * return the same normalized string for the same input.
     */
    public val id: String

    public fun normalize(input: String): String
}