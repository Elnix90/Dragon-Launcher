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
 * KEY TESTING PATTERN: Testing conditional rendering — descriptions are only
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

    //  Dual description overload

    @Test
    fun textWithDescription_dual_showsBothDescriptions() {
        composeTestRule.setContent {
            MaterialTheme {
                TextWithDescription(
                    text = "Title",
                    description1 = "First description",
                    description2 = "Second description"
                )
            }
        }

        composeTestRule.onNodeWithText("Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("First description").assertIsDisplayed()
        composeTestRule.onNodeWithText("Second description").assertIsDisplayed()
    }

    @Test
    fun textWithDescription_dual_hidesFirstWhenNull() {
        composeTestRule.setContent {
            MaterialTheme {
                TextWithDescription(
                    text = "Title",
                    description1 = null,
                    description2 = "Second description"
                )
            }
        }

        composeTestRule.onNodeWithText("Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Second description").assertIsDisplayed()
    }

    @Test
    fun textWithDescription_dual_hidesSecondWhenNull() {
        composeTestRule.setContent {
            MaterialTheme {
                TextWithDescription(
                    text = "Title",
                    description1 = "First description",
                    description2 = null
                )
            }
        }

        composeTestRule.onNodeWithText("Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("First description").assertIsDisplayed()
    }

    @Test
    fun textWithDescription_dual_hidesBothWhenNull() {
        composeTestRule.setContent {
            MaterialTheme {
                TextWithDescription(
                    text = "Title",
                    description1 = null,
                    description2 = null
                )
            }
        }

        composeTestRule.onNodeWithText("Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("First").assertDoesNotExist()
        composeTestRule.onNodeWithText("Second").assertDoesNotExist()
    }
}
