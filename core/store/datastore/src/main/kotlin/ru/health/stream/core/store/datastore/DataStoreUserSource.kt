package ru.health.stream.core.store.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import ru.health.stream.core.store.datastore.mapper.asUser
import ru.health.stream.core.store.datastore.mapper.asUserEntity
import ru.health.stream.core.store.datastore.model.UserEntity
import ru.health.stream.feature.personal.data.model.User
import ru.health.stream.feature.personal.source.local.LocalUserSource
import javax.inject.Inject

internal class DataStoreUserSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : LocalUserSource {

    override suspend fun getUser(): User? {
        return dataStore.data.map { preferences ->
            val userJson = preferences[USER_KEY] ?: return@map null

            runCatching {
                Json.decodeFromString<UserEntity>(userJson).asUser()
            }.getOrNull()
        }.first()
    }

    override suspend fun saveUser(user: User) {
        dataStore.edit { preferences ->
            preferences[USER_KEY] = Json.encodeToString(user.asUserEntity())
        }
    }

    private companion object {
        val USER_KEY = stringPreferencesKey("user_data")
    }
}
