package io.github.bretttolbert.verbecc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.bretttolbert.verbecc.ui.theme.VerbeccTheme

@Composable
@Suppress("FunctionName")
fun ConjugationScreen(
    modifier: Modifier = Modifier,
    model: ConjugationViewModel = viewModel()
) {
    ConjugationContent(
        state = model.state,
        onInfinitiveChange = model::setInfinitive,
        onLanguageChange = model::setLanguage,
        onConjugate = model::conjugate,
        modifier = modifier
    )
}

@Composable
@Suppress("FunctionName")
fun ConjugationContent(
    state: ConjugationUiState,
    onInfinitiveChange: (String) -> Unit,
    onLanguageChange: (Language) -> Unit,
    onConjugate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val submit = {
        keyboard?.hide()
        onConjugate()
    }
    Scaffold(modifier = modifier) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).imePadding().testTag("conjugation_list"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
                    Text(stringResource(R.string.conjugation_description))
                    LanguageSelector(state.language, !state.loading, onLanguageChange)
                    OutlinedTextField(
                        value = state.infinitive,
                        onValueChange = onInfinitiveChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.loading,
                        singleLine = true,
                        label = { Text(stringResource(R.string.infinitive_label)) },
                        placeholder = { Text(stringResource(R.string.infinitive_hint)) },
                        isError = state.error != null,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { submit() })
                    )
                    state.error?.let {
                        Text(
                            stringResource(it),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                        )
                    }
                    Button(
                        onClick = submit,
                        enabled = !state.loading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (state.loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            Text(stringResource(if (state.loading) R.string.conjugating else R.string.conjugate))
                        }
                    }
                    if (state.result == null && state.error == null && !state.loading) {
                        Text(
                            stringResource(R.string.empty_results),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            state.result?.let { result ->
                item {
                    Text(
                        stringResource(
                            R.string.result_heading,
                            if (result.reflexive()) state.infinitive.trim() else result.verb().infinitive(),
                            stringResource(languageLabel(result.language()))
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.semantics { heading() }
                    )
                }
                result.moods().forEach { (mood, tenses) ->
                    item {
                        Text(
                            displayName(mood),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.semantics { heading() }
                        )
                    }
                    items(tenses.entries.toList()) { (tense, conjugations) ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(displayName(tense), style = MaterialTheme.typography.titleMedium)
                                if (conjugations.isEmpty()) {
                                    Text(stringResource(R.string.no_forms))
                                }
                                SelectionContainer {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        conjugations.forEach { conjugation ->
                                            Text(conjugation.forms().joinToString(" / "))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("FunctionName")
private fun LanguageSelector(
    language: Language,
    enabled: Boolean,
    onLanguageChange: (Language) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = stringResource(languageLabel(language)),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(stringResource(R.string.language_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Language.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(languageLabel(option))) },
                    onClick = {
                        onLanguageChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun languageLabel(language: Language): Int = when (language) {
    Language.CA -> R.string.language_catalan
    Language.ES -> R.string.language_spanish
    Language.FR -> R.string.language_french
    Language.IT -> R.string.language_italian
    Language.PT -> R.string.language_portuguese
    Language.RO -> R.string.language_romanian
}

private fun displayName(value: String): String =
    value.replace('-', ' ').replaceFirstChar { it.titlecase() }

@Preview(showBackground = true)
@Composable
@Suppress("FunctionName")
private fun ConjugationPreview() {
    VerbeccTheme {
        ConjugationContent(ConjugationUiState(), {}, {}, {})
    }
}
