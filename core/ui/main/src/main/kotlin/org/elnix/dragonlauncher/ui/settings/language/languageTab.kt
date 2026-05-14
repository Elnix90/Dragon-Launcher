package org.elnix.dragonlauncher.ui.settings.language


import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxWidth
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
                applyLocale(ctx, null)
            }
        },
        lazyContent = {
            items(availableLanguages) { (tag, name) ->
                DragonRow(
                    onClick = {
                        scope.launch {
                            LanguageSettingsStore.keyLang.set(ctx, tag)
                            applyLocale(ctx, tag)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = name,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    RadioButton(
                        selected = tag == selectedTag,
                        onClick = {
                            scope.launch {
                                LanguageSettingsStore.keyLang.set(ctx, tag)
                                applyLocale(ctx, tag)
                            }
                        },
                        colors = AppObjectsColors.radioButtonColors()
                    )
                }
            }
        }
    )
}


private fun applyLocale(ctx: Context, tag: String?) {
    val localeList = if (tag == null) {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(tag)
    }
    AppCompatDelegate.setApplicationLocales(localeList)

    // Force the activity to recreate to pick up new locale
    if (ctx is android.app.Activity) {
        ctx.recreate()
    }
}