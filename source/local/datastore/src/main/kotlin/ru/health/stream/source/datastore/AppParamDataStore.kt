package ru.health.stream.source.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.health.stream.data.setting.model.AppParam
import javax.inject.Inject

internal class AppParamDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val appParam: Flow<AppParam> = dataStore.data
        .map { preferences ->
            AppParam(
                isFirstStart = preferences[PreferencesKeys.IS_FIRST_START] ?: true
            )
        }

    suspend fun setAppParam(appParam: AppParam) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_START] = appParam.isFirstStart
        }
    }

    private object PreferencesKeys {

        val IS_FIRST_START = booleanPreferencesKey("is_first_start")
    }
}
