package org.elnix.dragonlauncher.ui.dragon

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [DragonRow].
 *
 * [DragonRow] is a clickable row wrapper used throughout the settings UI.
 * It has two overloads:
 * - Clickable: accepts an `onClick` lambda, `enabled`  flag, and optional `onLongClick`
 * - Non-clickable: just a styled Row for layout purposes
 *
 * KEY TESTING PATTERN: Testing interaction callbacks. We verify that clicking
 * a composable triggers the expected lambda. This is the foundation of
 * interaction testing in Compose.
 *
 * NOTE: DragonRow depends on custom modifiers (`shapedClickable`) and theme
 * objects from other modules. These must be on the classpath for the tests
 * to compile.
 */
class DragonRowTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun clickableRow_displaysContent() {
        composeTestRule.setContent {
            MaterialTheme {
                DragonRow(onClick = {}) {
                    Text("Row Content")
                }
            }
        }

        composeTestRule.onNodeWithText("Row Content").assertIsDisplayed()
    }

    @Test
    fun clickableRow_clickTriggersCallback() {
        var clicked = false

        composeTestRule.setContent {
            MaterialTheme {
                DragonRow(onClick = { clicked = true }) {
                    Text("Click me")
                }
            }
        }

        composeTestRule.onNodeWithText("Click me").performClick()
        assertTrue("onClick should have been called", clicked)
    }

    @Test
    fun clickableRow_multipleClicksTriggerMultipleCallbacks() {
        var clickCount = 0

        composeTestRule.setContent {
            MaterialTheme {
                DragonRow(onClick = { clickCount++ }) {
                    Text("Tap")
                }
            }
        }

        val node = composeTestRule.onNodeWithText("Tap")
        node.performClick()
        node.performClick()
        node.performClick()
        assertTrue("onClick should have been called 3 times", clickCount == 3)
    }

    @Test
    fun nonClickableRow_displaysContent() {
        composeTestRule.setContent {
            MaterialTheme {
                DragonRow({}) {
                    Text("Static Row")
                }
            }
        }

        composeTestRule.onNodeWithText("Static Row").assertIsDisplayed()
    }
}
