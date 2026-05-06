package org.elnix.dragonlauncher.ui.settings.language


import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.settings.stores.LanguageSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold


@Composable
fun LanguageTab(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Available languages
    val availableLanguages = listOf(
        "en" to stringResource(R.string.language_english),
        "de" to stringResource(R.string.language_german),
        "es" to stringResource(R.string.language_spanish),
        "fr" to stringResource(R.string.language_french),
        "hi" to stringResource(R.string.language_hindi),
        "ko" to stringResource(R.string.language_korean),
        "pt" to stringResource(R.string.language_portuguese),
        null to stringResource(R.string.system_default)
    )

    val selectedTag by LanguageSettingsStore.keyLang.asState()

    SettingsScaffold(
        title = stringResource(R.string.settings_language_title),
        onBack = onBack,
        helpText = stringResource(R.string.choose_your_app_language),
        onReset = {
            scope.launch {
                LanguageSettingsStore.resetAll(ctx)
            }
        },
        lazyContent = {
            items(availableLanguages) { (tag, name) ->
                DragonRow(
                    onClick = {
                        scope.launch {
                            LanguageSettingsStore.keyLang.set(ctx, tag)
                            applyLocale(tag)
                        }
                    }
                ) {
                    RadioButton(
                        selected = tag == selectedTag,
                        onClick = {
                            scope.launch {
                                LanguageSettingsStore.keyLang.set(ctx, tag)
                                applyLocale(tag)
                            }
                        },
                        colors = AppObjectsColors.radioButtonColors()
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = name,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    )
}


private fun applyLocale(tag: String?) {
    val localeList = if (tag == null) {
        AppCompatDelegate.getApplicationLocales().apply {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        }
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(tag)
    }
    AppCompatDelegate.setApplicationLocales(localeList)
}
