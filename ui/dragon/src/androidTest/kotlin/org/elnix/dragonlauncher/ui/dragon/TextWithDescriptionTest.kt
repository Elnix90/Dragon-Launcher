package org.elnix.dragonlauncher.ui.dragon

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [TextWithDescription].
 *
 * [TextWithDescription] displays a title text and optionally one or two
 * description lines beneath it. The title uses `labelMedium` style and
 * descriptions use `labelSmall`.
 *
 * Two overloads exist:
 * - Single description: `TextWithDescription(text, description)`
 * - Dual description: `TextWithDescription(text, description1, description2)`
 *
 * KEY TESTING PATTERN: Testing conditional rendering - descriptions are only
 * shown when non-null. We verify both the presence and absence of text nodes.
 */
class TextWithDescriptionTest {
	@get:Rule
	val composeTestRule = createAndroidComposeRule<ComponentActivity>()

	//  Single description overload

	@Test
	fun textWithDescription_showsTitle() {
		composeTestRule.setContent {
			MaterialTheme {
				TextWithDescription(text = "Title", description = null)
			}
		}

		composeTestRule.onNodeWithText("Title").assertIsDisplayed()
	}

	@Test
	fun textWithDescription_showsDescriptionWhenProvided() {
		composeTestRule.setContent {
			MaterialTheme {
				TextWithDescription(text = "Title", description = "Description text")
			}
		}

		composeTestRule.onNodeWithText("Title").assertIsDisplayed()
		composeTestRule.onNodeWithText("Description text").assertIsDisplayed()
	}

	@Test
	fun textWithDescription_hidesDescriptionWhenNull() {
		composeTestRule.setContent {
			MaterialTheme {
				TextWithDescription(text = "Title", description = null)
			}
		}

		composeTestRule.onNodeWithText("Title").assertIsDisplayed()
		composeTestRule.onNodeWithText("Some description").assertDoesNotExist()
	}
}
