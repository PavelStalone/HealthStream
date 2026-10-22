package ru.health.stream.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.health.stream.LocaleHelper
import javax.inject.Inject

data class Language(val tag: String?, val label: String)

@HiltViewModel
class LanguageViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    val supportedLanguages = listOf(
        Language(null, "System"),
        Language("en", "English"),
        Language("ru", "Русский"),
    )

    private val _currentLanguage = MutableStateFlow(
        value = getLanguageByTag(tag = LocaleHelper.getCurrentLanguageTag(context))
    )
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    fun changeLanguage(tag: String?) {
        LocaleHelper.setAppLocale(context, tag)
        _currentLanguage.value = getLanguageByTag(tag = tag)
    }

    private fun getLanguageByTag(tag: String?): Language = supportedLanguages
        .firstOrNull { it.tag == tag }
        ?: supportedLanguages.first()
}
