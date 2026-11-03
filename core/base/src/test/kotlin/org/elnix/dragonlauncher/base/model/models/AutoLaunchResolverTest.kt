package org.elnix.dragonlauncher.base.model.models

import org.elnix.dragonlauncher.StringNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AutoLaunchResolverTest {
	private val resolver =
		AutoLaunchResolver(
			object : StringNormalizer {
				override val id: String = "test"

				override fun normalize(input: String): String = input.lowercase()
			}
		)

	private fun resolve(
		query: String,
		results: List<String>,
		autoOpenSingleMatch: Boolean = true,
		disableAutoLaunchWhenFirstCharIs: String = ""
	): String? = resolver.resolve(query, results, autoOpenSingleMatch, disableAutoLaunchWhenFirstCharIs) { it }

	@Test
	fun `exact match launches even when fuzzy matches exist`() {
		assertEquals("Chronal", resolve("chronal", listOf("Chrome", "Chronal", "Chronus")))
	}

	@Test
	fun `exact match is case insensitive`() {
		assertEquals("Chronal", resolve("CHRONAL", listOf("Chrome", "Chronal")))
	}

	@Test
	fun `exact match wins over prefix twins`() {
		assertEquals("Chrome", resolve("chrome", listOf("Chrome Beta", "Chrome")))
	}

	@Test
	fun `unique prefix match launches`() {
		assertEquals("Calculator", resolve("calcu", listOf("Calendar", "Calculator")))
	}

	@Test
	fun `unique substring match launches`() {
		assertEquals("Clock", resolve("lock", listOf("Chrome", "Clock")))
	}

	@Test
	fun `ambiguous prefix does not launch`() {
		assertNull(resolve("chron", listOf("Chronal", "Chronus")))
	}

	@Test
	fun `single fuzzy match still launches`() {
		assertEquals("YouTube", resolve("yt", listOf("YouTube")))
	}

	@Test
	fun `blank query does not launch`() {
		assertNull(resolve("", listOf("Chronal")))
	}

	@Test
	fun `whitespace only query does not launch`() {
		assertNull(resolve("   ", listOf("Chronal")))
	}

	@Test
	fun `launch is skipped when disabled by setting`() {
		assertNull(resolve("chronal", listOf("Chronal"), autoOpenSingleMatch = false))
	}

	@Test
	fun `launch is skipped when query starts with disabled char`() {
		assertNull(resolve("chronal", listOf("Chronal"), disableAutoLaunchWhenFirstCharIs = "c"))
	}

	@Test
	fun `duplicate exact labels do not launch`() {
		assertNull(resolve("chronal", listOf("Chronal", "Chronal")))
	}

	@Test
	fun `no results do not launch`() {
		assertNull(resolve("chronal", emptyList()))
	}
}
