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
import ru.health.stream.data.vitals.model.kg
import ru.health.stream.data.vitals.model.measurement.BodyWeight
import ru.health.stream.source.local.room.entity.resource.ResourceEntity
import ru.health.stream.source.local.room.entity.resource.ResourceWithType
import ru.health.stream.source.local.room.entity.resource.asResourceWithType
import ru.health.stream.source.local.room.mapper.asEstimation
import ru.health.stream.source.local.room.mapper.asEstimationEntity
import ru.health.stream.source.local.room.mapper.asNote
import ru.health.stream.source.local.room.mapper.asNoteEntity

@Entity(
    tableName = "bodyWeight",
    indices = [Index(value = ["created_at"], unique = true)],
)
internal data class BodyWeightEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "weight_in_kg") val weightInKg: Float,
    @ColumnInfo(name = "note_id") val noteId: String?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "resource_id") val resourceId: String,
    @Embedded(prefix = "estimation_") val estimation: EstimationEntity?,
)

internal data class BodyWeightWithMetadata(
    @Embedded
    val bodyWeightEntity: BodyWeightEntity,

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

    fun asBodyWeight(): BodyWeight {
        val noteMetadata = noteEntity?.asNote() ?: EmptyMetadata
        val estimationMetadata = bodyWeightEntity.estimation?.asEstimation() ?: EmptyMetadata

        return with(bodyWeightEntity) {
            BodyWeight(
                id = id,
                createdAt = createdAt,
                weight = weightInKg.kg,
                resource = resourceWithType.asResource(),
                metadata = noteMetadata + estimationMetadata,
            )
        }
    }
}

internal fun BodyWeight.asBodyWeightWithMetadata(): BodyWeightWithMetadata {
    val note = metadata[Note]?.asNoteEntity()
    val resourceWithType = resource.asResourceWithType()
    val estimation = metadata[Estimation]?.asEstimationEntity()

    return BodyWeightWithMetadata(
        bodyWeightEntity = BodyWeightEntity(
            id = id,
            noteId = note?.id,
            createdAt = createdAt,
            weightInKg = weight.kg,
            estimation = estimation,
            resourceId = resourceWithType.resourceEntity.id,
        ),
        noteEntity = note,
        resourceWithType = resourceWithType,
    )
}
