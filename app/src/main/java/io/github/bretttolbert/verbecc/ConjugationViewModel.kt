package io.github.bretttolbert.verbecc

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ConjugationUiState(
    val infinitive: String = "",
    val language: Language = Language.FR,
    val loading: Boolean = false,
    val result: CompleteConjugation? = null,
    @get:StringRes val error: Int? = null
)

class ConjugationViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    var state by mutableStateOf(
        ConjugationUiState(
            infinitive = savedState["infinitive"] ?: "",
            language = Language.fromCode(savedState["language"] ?: "fr")
        )
    )
        private set

    fun setInfinitive(value: String) {
        if (state.loading) return
        savedState["infinitive"] = value
        state = state.copy(infinitive = value, result = null, error = null)
    }

    fun setLanguage(value: Language) {
        if (state.loading) return
        savedState["language"] = value.code()
        state = state.copy(language = value, result = null, error = null)
    }

    fun conjugate() {
        if (state.loading) return
        val infinitive = state.infinitive.trim()
        if (infinitive.isBlank()) {
            state = state.copy(result = null, error = R.string.error_blank_verb)
            return
        }
        val language = state.language
        state = state.copy(loading = true, result = null, error = null)
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.Default) {
                    CompleteConjugator(language).conjugate(infinitive)
                }
                state = state.copy(loading = false, result = result)
            } catch (error: VerbNotFoundException) {
                state = state.copy(loading = false, error = R.string.error_unknown_verb)
            } catch (error: IllegalArgumentException) {
                Log.w("Verbecc", "Invalid conjugation request for ${language.code()}", error)
                state = state.copy(loading = false, error = R.string.error_invalid_verb)
            } catch (error: IllegalStateException) {
                Log.e("Verbecc", "Conjugation data failure for ${language.code()}", error)
                state = state.copy(loading = false, error = R.string.error_conjugation_data)
            }
        }
    }
}
