package ru.health.stream.source.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.datetime.Instant
import ru.health.stream.data.vitals.model.EmptyMetadata
import ru.health.stream.data.vitals.model.Estimation
import ru.health.stream.data.vitals.model.Note
import ru.health.stream.data.vitals.model.measurement.HeartRate
import ru.health.stream.source.local.room.entity.resource.ResourceEntity
import ru.health.stream.source.local.room.entity.resource.ResourceWithType
import ru.health.stream.source.local.room.entity.resource.asResourceWithType
import ru.health.stream.source.local.room.mapper.asEstimation
import ru.health.stream.source.local.room.mapper.asEstimationEntity
import ru.health.stream.source.local.room.mapper.asNote
import ru.health.stream.source.local.room.mapper.asNoteEntity

@Entity(
    tableName = "heartRate",
    indices = [Index(value = ["created_at"], unique = true)],
)
internal data class HeartRateEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "pulse_rate") val pulse: Int,
    @ColumnInfo(name = "note_id") val noteId: String?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "resource_id") val resourceId: String,
    @ColumnInfo(name = "is_removed") val isRemoved: Boolean = false,
    @Embedded(prefix = "estimation_") val estimation: EstimationEntity?,
)

internal data class HeartRateWithMetadata(
    @Embedded
    val heartRateEntity: HeartRateEntity,

    @Relation(
        entity = ResourceEntity::class,
        parentColumn = "resource_id",
        entityColumn = "id",
    )
    val resourceWithType: ResourceWithType,

    @Relation(
        parentColumn = "note_id",
        entityColumn = "id",
    )
    val noteEntity: NoteEntity?,
) {

    fun asHeartRate(): HeartRate {
        val noteMetadata = noteEntity?.asNote() ?: EmptyMetadata
        val estimationMetadata = heartRateEntity.estimation?.asEstimation() ?: EmptyMetadata

        return HeartRate(
            id = heartRateEntity.id,
            pulse = heartRateEntity.pulse,
            createdAt = heartRateEntity.createdAt,
            resource = resourceWithType.asResource(),
            metadata = noteMetadata + estimationMetadata,
        )
    }
}

internal fun HeartRate.asHeartRateWithMetadata(): HeartRateWithMetadata {
    val note = metadata[Note]?.asNoteEntity()
    val resourceWithType = resource.asResourceWithType()
    val estimation = metadata[Estimation]?.asEstimationEntity()

    return HeartRateWithMetadata(
        heartRateEntity = HeartRateEntity(
            id = id,
            pulse = pulse,
            noteId = note?.id,
            createdAt = createdAt,
            resourceId = resourceWithType.resourceEntity.id,
            estimation = estimation
        ),
        resourceWithType = resourceWithType,
        noteEntity = note
    )
}
