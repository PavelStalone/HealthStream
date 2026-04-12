package ru.health.stream.source.local.room.entity.resource

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import ru.health.stream.data.vitals.model.Device
import ru.health.stream.data.vitals.model.Resource

@Entity(tableName = "resource")
internal data class ResourceEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "type") val type: ResourceType,
    @ColumnInfo(name = "device_id") val deviceId: String?,
    @ColumnInfo(name = "package") val packageName: String?,
) {

    enum class ResourceType {

        APP,
        MANUAL,
        DEVICE,
        ;
    }
}

internal data class ResourceWithType(
    @Embedded
    val resourceEntity: ResourceEntity,

    @Relation(
        parentColumn = "device_id",
        entityColumn = "id",
    )
    val deviceEntity: DeviceEntity?
) {

    fun asResource(): Resource = when (resourceEntity.type) {
        ResourceEntity.ResourceType.MANUAL -> Resource.Manual
        ResourceEntity.ResourceType.DEVICE -> deviceEntity!!.asDevice()
        ResourceEntity.ResourceType.APP -> Resource.App(packageName = resourceEntity.packageName!!)
    }
}

internal fun Resource.asResourceWithType(): ResourceWithType = when (this) {
    is Device -> {
        val deviceEntity = asDeviceEntity()
        ResourceWithType(
            resourceEntity = ResourceEntity(
                id = id,
                type = ResourceEntity.ResourceType.DEVICE,
                deviceId = deviceEntity.id,
                packageName = null
            ),
            deviceEntity = deviceEntity,
        )
    }

    is Resource.App -> {
        ResourceWithType(
            resourceEntity = ResourceEntity(
                id = packageName,
                deviceId = null,
                packageName = packageName,
                type = ResourceEntity.ResourceType.APP,
            ),
            deviceEntity = null,
        )
    }

    Resource.Manual -> ResourceWithType(
        resourceEntity = ResourceEntity(
            id = "Manual",
            deviceId = null,
            packageName = null,
            type = ResourceEntity.ResourceType.MANUAL,
        ),
        deviceEntity = null,
    )
}
