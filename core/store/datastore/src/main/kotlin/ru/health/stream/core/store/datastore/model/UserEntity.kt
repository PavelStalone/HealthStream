package ru.health.stream.core.store.datastore.model

import kotlinx.serialization.Serializable

@Serializable
internal data class UserEntity(
    val email: String,
    val height: Double,
    val gender: Boolean,
    val lastName: String,
    val birthday: String,
    val firstName: String,
)