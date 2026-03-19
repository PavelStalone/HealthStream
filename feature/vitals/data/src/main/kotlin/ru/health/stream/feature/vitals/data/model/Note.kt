package ru.health.stream.feature.vitals.data.model

import kotlinx.datetime.Instant
import kotlin.uuid.Uuid

data class Note(
    val id: String,
    val createdAt: Instant,
    val description: String,
    val title: String? = null,
) : Metadata.Element {

    init {
        Uuid.parse(id) // Check uuid format
        require(description.isNotBlank()) { "Note description cannot be blank" }
        require(title?.isNotBlank() ?: true) { "Note title cannot be blank" }
    }

    override val key: Metadata.Key<*> = Key

    companion object Key : Metadata.Key<Note>
}
