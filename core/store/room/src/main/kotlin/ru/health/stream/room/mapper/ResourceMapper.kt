package ru.health.stream.room.mapper

import ru.health.stream.feature.vitals.data.model.Resource
import ru.health.stream.room.entity.ResourceEntity

internal fun ResourceEntity.asSource(): Resource = when (this) {
    ResourceEntity.Manual -> Resource.Manual
    is ResourceEntity.FromApp -> Resource.FromApp(packageName = packageName)
    is ResourceEntity.WeightScale -> Resource.WithManufacturer.WeightScale(manufacturer = manufacturer)
    is ResourceEntity.BloodPressure -> Resource.WithManufacturer.BloodPressure(manufacturer = manufacturer)
    is ResourceEntity.PulseOximeter -> Resource.WithManufacturer.PulseOximeter(manufacturer = manufacturer)
}

internal fun Resource.asEntity(): ResourceEntity = when (this) {
    Resource.Manual -> ResourceEntity.Manual
    is Resource.FromApp -> ResourceEntity.FromApp(packageName = packageName)
    is Resource.WithManufacturer.WeightScale -> ResourceEntity.WeightScale(manufacturer = manufacturer)
    is Resource.WithManufacturer.BloodPressure -> ResourceEntity.BloodPressure(manufacturer = manufacturer)
    is Resource.WithManufacturer.PulseOximeter -> ResourceEntity.PulseOximeter(manufacturer = manufacturer)
}
