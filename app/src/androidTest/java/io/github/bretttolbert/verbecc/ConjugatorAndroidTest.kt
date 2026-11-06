package io.github.bretttolbert.verbecc

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConjugatorAndroidTest {
    @Test
    fun conjugatesAllSixLanguagesOnAndroid() {
        val cases = listOf(
            listOf("ca", "ser", "indicatiu", "present", "jo sóc"),
            listOf("es", "tener", "indicativo", "presente", "yo tengo"),
            listOf("fr", "manger", "indicatif", "présent", "je mange"),
            listOf("it", "avere", "indicativo", "presente", "io ho"),
            listOf("pt", "ter", "indicativo", "presente", "eu tenho"),
            listOf("ro", "face", "indicativ", "prezent", "eu fac")
        )
        assertEquals(Language.values().size, cases.size)
        cases.forEach { (code, infinitive, mood, tense, expected) ->
            val language = Language.fromCode(code)
            val result = CompleteConjugator(language).conjugate(infinitive)
            assertEquals(expected, result.moods().getValue(mood).getValue(tense)[0].forms()[0])
            assertTrue(language.code(), result.moods().isNotEmpty())
            assertTrue(language.code(), result.moods().values.flatMap { it.values }
                .flatten().all { it.forms().isNotEmpty() })
        }

    }

    @Test
    fun preservesAlternateAndCompoundForms() {
        val french = CompleteConjugator(Language.FR)
        assertEquals(listOf("je paie", "je paye"),
            french.conjugateMoodTense("payer", "indicatif", "présent")[0].forms())
        assertEquals("j'ai mangé",
            french.conjugateMoodTense("manger", "indicatif", "passé-composé")[0].forms()[0])
    }
}
