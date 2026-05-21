package ru.health.stream.data.setting.repository

import kotlinx.coroutines.flow.Flow
import ru.health.stream.data.setting.model.AppParam

interface AppParamRepository {

    val appParamFlow: Flow<AppParam>

    suspend fun setAppParam(appParam: AppParam)
}
