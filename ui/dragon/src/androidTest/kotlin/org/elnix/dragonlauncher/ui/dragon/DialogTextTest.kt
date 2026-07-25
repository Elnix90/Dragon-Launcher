package org.elnix.dragonlauncher.ui.dragon

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.elnix.dragonlauncher.ui.dragon.text.DialogDescription
import org.elnix.dragonlauncher.ui.dragon.text.DialogSubTitle
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for dialog text composables: [DialogTitle], [DialogSubTitle],
 * and [DialogDescription].
 *
 * Each composable renders text with a specific Material3 typography style:
 * - [DialogTitle]: headlineSmall (large dialog headers)
 * - [DialogSubTitle]: titleLarge (secondary headers)
 * - [DialogDescription]: bodyLarge (body text in dialogs)
 *
 * All three use onSurface color from the theme.
 *
 * KEY TESTING PATTERN: Testing that multiple composables render independently
 * and don't interfere with each other when placed side by side.
 */
class DialogTextTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun dialogTitle_displaysText() {
        composeTestRule.setContent {
            MaterialTheme {
                DialogTitle(text = "Warning")
            }
        }

        composeTestRule.onNodeWithText("Warning").assertIsDisplayed()
    }

    @Test
    fun dialogSubTitle_displaysText() {
        composeTestRule.setContent {
            MaterialTheme {
                DialogSubTitle(text = "Subtitle")
            }
        }

        composeTestRule.onNodeWithText("Subtitle").assertIsDisplayed()
    }

    @Test
    fun dialogDescription_displaysText() {
        composeTestRule.setContent {
            MaterialTheme {
                DialogDescription(text = "This is a description.")
            }
        }

        composeTestRule.onNodeWithText("This is a description.").assertIsDisplayed()
    }

    @Test
    fun allDialogTexts_coexistWithoutConflict() {
        composeTestRule.setContent {
            MaterialTheme {
                DialogTitle(text = "Title")
                DialogSubTitle(text = "Subtitle")
                DialogDescription(text = "Description")
            }
        }

        composeTestRule.onNodeWithText("Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Subtitle").assertIsDisplayed()
        composeTestRule.onNodeWithText("Description").assertIsDisplayed()
    }

    @Test
    fun dialogTitle_displaysLongText() {
        val longTitle = "Are you sure you want to delete this workspace? This action cannot be undone."
        composeTestRule.setContent {
            MaterialTheme {
                DialogTitle(text = longTitle)
            }
        }

        composeTestRule.onNodeWithText(longTitle).assertIsDisplayed()
    }

    @Test
    fun dialogDescription_displaysMultiLineText() {
        val multiline = "Line 1\nLine 2\nLine 3"
        composeTestRule.setContent {
            MaterialTheme {
                DialogDescription(text = multiline)
            }
        }

        composeTestRule.onNodeWithText(multiline).assertIsDisplayed()
    }
}
