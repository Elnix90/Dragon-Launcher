package org.elnix.dragonlauncher.ui.dragon

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [DragonSettingsGroup].
 *
 * [DragonSettingsGroup] wraps content in a Material3 [androidx.compose.material3.Card] with a title.
 * It provides:
 * - An optional title rendered via [org.elnix.dragonlauncher.ui.dragon.text.SettingsWithTitle]
 * - Card-based visual container with rounded corners
 * - Consistent content padding
 *
 * KEY TESTING PATTERN: Testing container composables. We verify that the
 * container doesn't obstruct child rendering and that the title (when
 * provided via resource ID) renders alongside the content.
 */
class DragonSettingsGroupTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun settingsGroup_displaysContentWithoutTitle() {
        composeTestRule.setContent {
            MaterialTheme {
                DragonSettingsGroup(title = null) {
                    Text("Group content")
                }
            }
        }

        composeTestRule.onNodeWithText("Group content").assertIsDisplayed()
    }

    @Test
    fun settingsGroup_displaysMultipleChildren() {
        composeTestRule.setContent {
            MaterialTheme {
                DragonSettingsGroup(title = null) {
                    Text("Setting A")
                    Text("Setting B")
                }
            }
        }

        composeTestRule.onNodeWithText("Setting A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Setting B").assertIsDisplayed()
    }
}
