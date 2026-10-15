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

    private val _selectedTag = MutableStateFlow(LocaleHelper.getCurrentLanguageTag(context))
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    fun changeLanguage(tag: String?) {
        LocaleHelper.setAppLocale(context, tag)
        _selectedTag.value = tag
    }
}
