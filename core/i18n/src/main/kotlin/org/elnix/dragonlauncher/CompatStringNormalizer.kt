package org.elnix.dragonlauncher

import java.text.Normalizer
import java.util.Locale

/**
 * Pre Android 10 StringNormalizer. Only strips accents from latin characters
 */
internal class CompatStringNormalizer : StringNormalizer {
    override val id: String = "null"

    override fun normalize(input: String): String {
        val nfd = Normalizer.normalize(input.lowercase(Locale.getDefault()), Normalizer.Form.NFD)

        // strip accents (keep Japanese voiced mark and semi-voicing mark)
        val stripped = nfd.replace(Regex("[\\p{M}&&[^\\u3099\\u309A]]"), "")
        // The result is similar to StringUtils.stripAccents
        val nfc = Normalizer.normalize(stripped, Normalizer.Form.NFC)

        return nfc
            .replace("æ", "ae")
            .replace("œ", "oe")
            .replace("ß", "ss")
    }
}
