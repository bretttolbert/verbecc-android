package io.github.bretttolbert.verbecc

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConjugationScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun conjugatesFromTheUiInEveryLanguage() {
        val cases = listOf(
            Triple("Catalan", "ser", "jo sóc"),
            Triple("Spanish", "tener", "yo tengo"),
            Triple("French", "manger", "je mange"),
            Triple("Italian", "avere", "io ho"),
            Triple("Portuguese", "ter", "eu tenho"),
            Triple("Romanian", "face", "eu fac")
        )
        var selected = "French"
        cases.forEach { (language, verb, expected) ->
            compose.onNodeWithTag("conjugation_list").performScrollToNode(hasText("Language"))
            compose.onNodeWithText(selected).performClick()
            compose.onNodeWithText(language).performClick()
            selected = language
            compose.onNodeWithText("Infinitive verb").performTextReplacement(verb)
            compose.onNodeWithText("Conjugate").performClick()
            compose.waitUntil(30_000) {
                compose.onAllNodes(hasText("$verb ($language)")).fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("conjugation_list").performScrollToNode(hasText(expected))
            compose.onNodeWithText(expected).assertIsDisplayed()
        }
    }

    @Test
    fun validatesInputAndRecoversFromAnUnknownVerb() {
        compose.onNodeWithText("Conjugate").performClick()
        compose.onNodeWithText("Enter an infinitive verb.").assertIsDisplayed()
        compose.onNodeWithText("Infinitive verb").performTextReplacement("zzzzzz")
        compose.onNodeWithText("Conjugate").performClick()
        val unknown = "Verb not found in this language. Check the infinitive and selected language."
        compose.waitUntil(30_000) {
            compose.onAllNodes(hasText(unknown)).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(unknown).assertIsDisplayed()
        compose.onNodeWithText("Infinitive verb").performTextReplacement("  MANGER  ")
        compose.onNodeWithText(unknown).assertDoesNotExist()
        compose.onNodeWithText("Conjugate").performClick()
        compose.waitUntil(30_000) {
            compose.onAllNodes(hasText("manger (French)")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("conjugation_list").performScrollToNode(hasText("je mange"))
        compose.onNodeWithText("je mange").assertIsDisplayed()
        compose.onNodeWithTag("conjugation_list").performScrollToNode(hasText("Infinitive verb"))
        compose.onNodeWithText("Infinitive verb").performTextReplacement("payer")
        compose.onNodeWithText("manger (French)").assertDoesNotExist()
    }

    @Test
    fun keepsInputAndLanguageWhenActivityIsRecreated() {
        compose.onNodeWithText("French").performClick()
        compose.onNodeWithText("Spanish").performClick()
        compose.onNodeWithText("Infinitive verb").performTextReplacement("tener")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Spanish").assertIsDisplayed()
        compose.onNodeWithText("Infinitive verb").assertTextContains("tener")
    }
}
