package org.elnix.dragonlauncher.base.model.models

import org.elnix.dragonlauncher.StringNormalizer

/**
 * Decides which search result (if any) an app-drawer query should auto-launch.
 *
 * Search ranks with a fuzzy score, so unrelated apps pass the threshold ("chronal" also matches "chrome").
 * Auto-launch therefore requires a literal match on the normalized label, checked in nested tiers:
 * exact, prefix, substring. The first tier with a member decides - one member launches it, several mean
 * the query is still ambiguous. A lone result launches regardless, keeping the original single-match behavior.
 */
class AutoLaunchResolver(
	private val normalizer: StringNormalizer
) {
	fun <T> resolve(
		query: String,
		results: List<T>,
		autoOpenSingleMatch: Boolean,
		disableAutoLaunchWhenFirstCharIs: String,
		labelOf: (T) -> String
	): T? {
		if (!autoOpenSingleMatch) return null
		if (query.isEmpty()) return null
		if (disableAutoLaunchWhenFirstCharIs.isNotEmpty() && query.first() == disableAutoLaunchWhenFirstCharIs.first()) return null

		// search() trims the query before scoring; whitespace-only input must not launch
		val normalizedQuery = normalizer.normalize(query.trim())
		if (normalizedQuery.isEmpty()) return null
		if (results.isEmpty()) return null

		// original behavior: a single match (even a fuzzy one) launches
		if (results.size == 1) return results.first()

		val normalizedLabels = results.map { normalizer.normalize(labelOf(it)) }
		val tiers =
			listOf(
				results.filterIndexed { index, _ -> normalizedLabels[index] == normalizedQuery },
				results.filterIndexed { index, _ -> normalizedLabels[index].startsWith(normalizedQuery) },
				results.filterIndexed { index, _ -> normalizedQuery in normalizedLabels[index] }
			)

		// tiers are nested (exact ⊆ prefix ⊆ substring): the first non-empty tier decides
		return tiers.firstOrNull { it.isNotEmpty() }?.singleOrNull()
	}
}
