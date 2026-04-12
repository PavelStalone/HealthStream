package ru.health.stream.source.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.health.stream.source.local.room.converter.EmailConverter
import ru.health.stream.source.local.room.converter.InstantConverter
import ru.health.stream.source.local.room.converter.LengthConverter
import ru.health.stream.source.local.room.converter.LocalDateConverter
import ru.health.stream.source.local.room.dao.DeviceDao
import ru.health.stream.source.local.room.dao.HeartRateDao
import ru.health.stream.source.local.room.dao.NoteDao
import ru.health.stream.source.local.room.dao.ResourceDao
import ru.health.stream.source.local.room.dao.UserDao
import ru.health.stream.source.local.room.entity.resource.DeviceEntity
import ru.health.stream.source.local.room.entity.HeartRateEntity
import ru.health.stream.source.local.room.entity.NoteEntity
import ru.health.stream.source.local.room.entity.UserEntity
import ru.health.stream.source.local.room.entity.resource.ResourceEntity

@Database(
    entities = [
        UserEntity::class,
        NoteEntity::class,
        DeviceEntity::class,
        ResourceEntity::class,
        HeartRateEntity::class,
    ],
    version = 1
)
@TypeConverters(
    value = [
        EmailConverter::class,
        LengthConverter::class,
        InstantConverter::class,
        LocalDateConverter::class,
    ]
)
internal abstract class VitalDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun noteDao(): NoteDao
    abstract fun deviceDao(): DeviceDao
    abstract fun resourceDao(): ResourceDao
    abstract fun heartRateDao(): HeartRateDao

    companion object {

        private const val DATABASE_NAME = "vital-database"

        fun buildDatabase(context: Context): VitalDatabase =
            Room.databaseBuilder(context, VitalDatabase::class.java, DATABASE_NAME)
                .build()
    }
}
