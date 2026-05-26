package ru.health.stream.source.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.health.stream.source.datastore.di.InternalStore
import ru.health.stream.source.local.KeyValueSource

internal class InternalDataStore @Inject constructor(
    @InternalStore private val dataStore: DataStore<Preferences>
) : KeyValueSource {
    
    override suspend fun <T> getValue(key: String): T? {
        return dataStore.data.map { preferences ->
            @Suppress("UNCHECKED_CAST")
            preferences.asMap().entries.find { it.key.name == key }?.value as? T
        }.first()
    }
    
    override suspend fun <T> saveValue(key: String, value: T) {
        dataStore.edit { preferences ->
            when (value) {
                is String -> preferences[stringPreferencesKey(key)] = value
                is Int -> preferences[intPreferencesKey(key)] = value
                is Long -> preferences[longPreferencesKey(key)] = value
                is Double -> preferences[doublePreferencesKey(key)] = value
                is Float -> preferences[floatPreferencesKey(key)] = value
                is Boolean -> preferences[booleanPreferencesKey(key)] = value
                is Set<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    preferences[stringSetPreferencesKey(key)] = value as Set<String>
                }
                else -> throw IllegalArgumentException("Unsupported type: ${value?.let { it::class.java } ?: "null"}")
            }
        }
    }
}
