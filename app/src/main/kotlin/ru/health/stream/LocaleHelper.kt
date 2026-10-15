package ru.health.stream

import android.app.LocaleManager
import android.content.Context
import android.os.LocaleList

object LocaleHelper {

    /**
     * Set the app's language. Pass null or empty to follow system default.
     *
     * Examples:
     *   setAppLocale(context, "es")     -> Spanish
     *   setAppLocale(context, "hi")     -> Hindi
     *   setAppLocale(context, null)     -> Follow system
     */
    fun setAppLocale(context: Context, languageTag: String?) {
        val localeManager = context.getSystemService(LocaleManager::class.java)

        localeManager.applicationLocales = if (languageTag.isNullOrEmpty()) {
            LocaleList.getEmptyLocaleList()
        } else {
            LocaleList.forLanguageTags(languageTag)
        }
    }

    /**
     * Get the currently active language tag, or null if following system.
     */
    fun getCurrentLanguageTag(context: Context): String? {
        val localeManager = context.getSystemService(LocaleManager::class.java)
        return localeManager.applicationLocales[0]?.toLanguageTag()
    }
}
