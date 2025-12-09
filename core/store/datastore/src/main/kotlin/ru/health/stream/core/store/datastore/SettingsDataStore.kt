package ru.health.stream.core.store.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import javax.inject.Inject

internal class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

}
