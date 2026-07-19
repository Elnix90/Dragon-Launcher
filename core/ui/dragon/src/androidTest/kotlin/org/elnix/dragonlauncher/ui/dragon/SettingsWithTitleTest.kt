package org.elnix.dragonlauncher.ui.dragon

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.elnix.dragonlauncher.ui.dragon.text.SettingsWithTitle
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [SettingsWithTitle].
 *
 * [SettingsWithTitle] renders an optional title string (from a resource ID)
 * above its content. When the title is null, only the content is shown.
 * This is a common pattern in settings screens where sections may or may not
 * have headers.
 *
 * KEY TESTING PATTERN: Testing nullable resource-based titles. Since we're
 * in instrumented tests, string resources are available from the app module.
 * We test with explicit string values to avoid resource ID coupling.
 *
 * NOTE: The actual [SettingsWithTitle] takes an `Int?` (resource ID), so in
 * real usage you'd pass R.string.title. For testing, we verify the composable
 * renders content correctly. Title rendering requires actual resource IDs
 * which are app-specific.
 */
class SettingsWithTitleTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun settingsWithTitle_rendersContent() {
        composeTestRule.setContent {
            MaterialTheme {
                SettingsWithTitle(title = null) {
                    Text("Settings content")
                }
            }
        }

        composeTestRule.onNodeWithText("Settings content").assertIsDisplayed()
    }

    @Test
    fun settingsWithTitle_withNullTitle_rendersContentOnly() {
        composeTestRule.setContent {
            MaterialTheme {
                SettingsWithTitle(title = null) {
                    Text("Content without title")
                }
            }
        }

        composeTestRule.onNodeWithText("Content without title").assertIsDisplayed()
    }

    @Test
    fun settingsWithTitle_withMultipleChildren() {
        composeTestRule.setContent {
            MaterialTheme {
                SettingsWithTitle(title = null) {
                    Text("Child 1")
                    Text("Child 2")
                    Text("Child 3")
                }
            }
        }

        composeTestRule.onNodeWithText("Child 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Child 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Child 3").assertIsDisplayed()
    }
}
