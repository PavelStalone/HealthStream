package ru.health.stream.core.store.datastore.mapper

import io.github.jacksever.automapper.annotation.AutoMapper
import io.github.jacksever.automapper.annotation.AutoMapperModule
import ru.health.stream.core.store.datastore.model.UserEntity
import ru.health.stream.feature.personal.data.model.User

@AutoMapperModule(
    converters = [
        EmailConverter::class,
        LengthConverter::class,
        LocalDateConverter::class,
    ]
)
internal interface MapperModule {

    @AutoMapper
    fun userMapper(user: User): UserEntity
}
