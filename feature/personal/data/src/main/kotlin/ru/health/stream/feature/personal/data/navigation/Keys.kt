package ru.health.stream.feature.personal.data.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class UserInputFlow(val destinationKey: NavKey) : NavKey
