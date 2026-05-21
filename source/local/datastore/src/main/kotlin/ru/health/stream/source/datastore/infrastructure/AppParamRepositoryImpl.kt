package ru.health.stream.source.datastore.infrastructure

import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import ru.health.stream.data.setting.model.AppParam
import ru.health.stream.data.setting.repository.AppParamRepository
import ru.health.stream.source.datastore.AppParamDataStore

internal class AppParamRepositoryImpl @Inject constructor(
    private val dataStore: AppParamDataStore
) : AppParamRepository {

    override val appParamFlow: Flow<AppParam> = dataStore.appParam

    override suspend fun setAppParam(appParam: AppParam) = dataStore.setAppParam(appParam)
}
