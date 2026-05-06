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
import ru.health.stream.data.vitals.model.measurement.OxygenSaturation
import ru.health.stream.source.local.room.entity.resource.ResourceEntity
import ru.health.stream.source.local.room.entity.resource.ResourceWithType
import ru.health.stream.source.local.room.entity.resource.asResourceWithType
import ru.health.stream.source.local.room.mapper.asEstimation
import ru.health.stream.source.local.room.mapper.asEstimationEntity
import ru.health.stream.source.local.room.mapper.asNote
import ru.health.stream.source.local.room.mapper.asNoteEntity

@Entity(
    tableName = "oxygenSaturation",
    indices = [Index(value = ["created_at"], unique = true)],
)
internal data class OxygenSaturationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "saturation") val saturation: Float,
    @ColumnInfo(name = "note_id") val noteId: String?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "resource_id") val resourceId: String,
    @ColumnInfo(name = "is_removed") val isRemoved: Boolean = false,
    @Embedded(prefix = "estimation_") val estimation: EstimationEntity?,
)

internal data class OxygenSaturationWithMetadata(
    @Embedded val oxygenSaturationEntity: OxygenSaturationEntity,

    @Relation(
        entity = ResourceEntity::class,
        parentColumn = "resource_id",
        entityColumn = "id",
    ) val resourceWithType: ResourceWithType,

    @Relation(
        parentColumn = "note_id",
        entityColumn = "id",
    ) val noteEntity: NoteEntity?,
) {

    fun asOxygenSaturation(): OxygenSaturation {
        val noteMetadata = noteEntity?.asNote() ?: EmptyMetadata
        val estimationMetadata = oxygenSaturationEntity.estimation?.asEstimation() ?: EmptyMetadata

        return with(oxygenSaturationEntity) {
            OxygenSaturation(
                id = id,
                createdAt = createdAt,
                saturation = saturation,
                resource = resourceWithType.asResource(),
                metadata = noteMetadata + estimationMetadata,
            )
        }
    }
}

internal fun OxygenSaturation.asOxygenSaturationWithMetadata(): OxygenSaturationWithMetadata {
    val note = metadata[Note]?.asNoteEntity()
    val resourceWithType = resource.asResourceWithType()
    val estimation = metadata[Estimation]?.asEstimationEntity()

    return OxygenSaturationWithMetadata(
        oxygenSaturationEntity = OxygenSaturationEntity(
            id = id,
            noteId = note?.id,
            createdAt = createdAt,
            saturation = saturation,
            estimation = estimation,
            resourceId = resourceWithType.resourceEntity.id,
        ),
        noteEntity = note,
        resourceWithType = resourceWithType,
    )
}
