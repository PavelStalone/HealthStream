package ru.health.stream.core.store.datastore

import kotlinx.serialization.Serializable

@Serializable
internal data class UserEntity(
    val email: String,
    val height: Double,
    val gender: Boolean,
    val lastName: String,
    val firstName: String,
    val birthday: String,
)
