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
import ru.health.stream.data.vitals.model.measurement.BloodPressure
import ru.health.stream.source.local.room.entity.resource.ResourceEntity
import ru.health.stream.source.local.room.entity.resource.ResourceWithType
import ru.health.stream.source.local.room.entity.resource.asResourceWithType
import ru.health.stream.source.local.room.mapper.asEstimation
import ru.health.stream.source.local.room.mapper.asEstimationEntity
import ru.health.stream.source.local.room.mapper.asNote
import ru.health.stream.source.local.room.mapper.asNoteEntity

@Entity(
    tableName = "bloodPressure",
    indices = [Index(value = ["created_at"], unique = true)],
)
internal data class BloodPressureEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "systolic") val systolic: Float,
    @ColumnInfo(name = "diastolic") val diastolic: Float,
    @ColumnInfo(name = "note_id") val noteId: String?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "resource_id") val resourceId: String,
    @Embedded(prefix = "estimation_") val estimation: EstimationEntity?,
)

internal data class BloodPressureWithMetadata(
    @Embedded
    val bloodPressureEntity: BloodPressureEntity,

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

    fun asBloodPressure(): BloodPressure {
        val noteMetadata = noteEntity?.asNote() ?: EmptyMetadata
        val estimationMetadata = bloodPressureEntity.estimation?.asEstimation() ?: EmptyMetadata

        return with(bloodPressureEntity) {
            BloodPressure(
                id = id,
                systolic = systolic,
                diastolic = diastolic,
                createdAt = createdAt,
                resource = resourceWithType.asResource(),
                metadata = noteMetadata + estimationMetadata,
            )
        }
    }
}

internal fun BloodPressure.asBloodPressureWithMetadata(): BloodPressureWithMetadata {
    val note = metadata[Note]?.asNoteEntity()
    val resourceWithType = resource.asResourceWithType()
    val estimation = metadata[Estimation]?.asEstimationEntity()

    return BloodPressureWithMetadata(
        bloodPressureEntity = BloodPressureEntity(
            id = id,
            noteId = note?.id,
            systolic = systolic,
            diastolic = diastolic,
            createdAt = createdAt,
            estimation = estimation,
            resourceId = resourceWithType.resourceEntity.id,
        ),
        noteEntity = note,
        resourceWithType = resourceWithType,
    )
}
