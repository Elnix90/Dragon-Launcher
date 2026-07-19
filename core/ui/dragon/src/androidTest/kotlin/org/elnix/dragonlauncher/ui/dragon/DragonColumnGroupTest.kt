package org.elnix.dragonlauncher.ui.dragon

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.elnix.dragonlauncher.ui.dragon.components.DragonColumn
import org.elnix.dragonlauncher.ui.dragon.components.DragonColumnGroup
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [DragonColumnGroup] and [DragonColumn].
 *
 * Both composables are vertically stacked layout wrappers that provide
 * consistent spacing, alignment, and theming for settings UI groups:
 * - [DragonColumnGroup]: applies a `settingsGroup` modifier for rounded card styling
 * - [DragonColumn]: simpler column with full width and consistent spacing
 *
 * Both set [androidx.compose.material3.LocalContentColor] to `onSurface` from the theme.
 *
 * KEY TESTING PATTERN: Testing layout composables. We verify that child
 * content is rendered correctly within the group, regardless of the
 * container's visual styling.
 */
class DragonColumnGroupTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun dragonColumnGroup_displaysChildren() {
        composeTestRule.setContent {
            MaterialTheme {
                DragonColumnGroup {
                    Text("Item 1")
                    Text("Item 2")
                    Text("Item 3")
                }
            }
        }

        composeTestRule.onNodeWithText("Item 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Item 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Item 3").assertIsDisplayed()
    }

    @Test
    fun dragonColumnGroup_withSingleChild() {
        composeTestRule.setContent {
            MaterialTheme {
                DragonColumnGroup {
                    Text("Only child")
                }
            }
        }

        composeTestRule.onNodeWithText("Only child").assertIsDisplayed()
    }

    @Test
    fun dragonColumn_displaysChildren() {
        composeTestRule.setContent {
            MaterialTheme {
                DragonColumn {
                    Text("Column Item A")
                    Text("Column Item B")
                }
            }
        }

        composeTestRule.onNodeWithText("Column Item A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Column Item B").assertIsDisplayed()
    }

    @Test
    fun dragonColumn_withEmptyContent() {
        composeTestRule.setContent {
            MaterialTheme {
                DragonColumn {
                    // No children
                }
            }
        }

        // Verify no crash and the column renders (empty is valid)
        composeTestRule.onNodeWithText("Nonexistent").assertDoesNotExist()
    }
}
