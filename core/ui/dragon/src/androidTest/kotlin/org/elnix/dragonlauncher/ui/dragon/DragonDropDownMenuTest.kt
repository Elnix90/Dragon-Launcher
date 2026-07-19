package org.elnix.dragonlauncher.ui.dragon

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.elnix.dragonlauncher.ui.dragon.components.DragonDropDownMenu
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [DragonDropDownMenu].
 *
 * [DragonDropDownMenu] wraps Material3's [androidx.compose.material3.DropdownMenu] with transparent
 * background and zero elevation, creating a flat menu appearance that
 * integrates with the dragon UI theme.
 *
 * KEY TESTING PATTERN: Testing visibility states. The menu is shown/hidden
 * via the `expanded` boolean parameter. We test both states to ensure the
 * content is properly shown and hidden.
 */
class DragonDropDownMenuTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun dropDownMenu_showsContentWhenExpanded() {
        composeTestRule.setContent {
            MaterialTheme {
                DragonDropDownMenu(
                    expanded = true,
                    onDismissRequest = {}
                ) {
                    Text("Option 1")
                    Text("Option 2")
                }
            }
        }

        composeTestRule.onNodeWithText("Option 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Option 2").assertIsDisplayed()
    }

    @Test
    fun dropDownMenu_hidesContentWhenCollapsed() {
        composeTestRule.setContent {
            MaterialTheme {
                DragonDropDownMenu(
                    expanded = false,
                    onDismissRequest = {}
                ) {
                    Text("Hidden Option")
                }
            }
        }

        composeTestRule.onNodeWithText("Hidden Option").assertDoesNotExist()
    }

    @Test
    fun dropDownMenu_displaysSingleItem() {
        composeTestRule.setContent {
            MaterialTheme {
                DragonDropDownMenu(
                    expanded = true,
                    onDismissRequest = {}
                ) {
                    Text("Only option")
                }
            }
        }

        composeTestRule.onNodeWithText("Only option").assertIsDisplayed()
    }
}
