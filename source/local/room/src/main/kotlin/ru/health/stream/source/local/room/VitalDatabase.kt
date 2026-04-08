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
import ru.health.stream.source.local.room.converter.ResourceConverter
import ru.health.stream.source.local.room.converter.StatusConverter
import ru.health.stream.source.local.room.dao.DeviceDao
import ru.health.stream.source.local.room.dao.HeartRateDao
import ru.health.stream.source.local.room.dao.UserDao
import ru.health.stream.source.local.room.entity.DeviceEntity
import ru.health.stream.source.local.room.entity.HeartRateEntity
import ru.health.stream.source.local.room.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        DeviceEntity::class,
        HeartRateEntity::class,
    ],
    version = 1
)
@TypeConverters(
    value = [
        EmailConverter::class,
        LengthConverter::class,
        StatusConverter::class,
        InstantConverter::class,
        ResourceConverter::class,
        LocalDateConverter::class,
    ]
)
internal abstract class VitalDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun deviceDao(): DeviceDao
    abstract fun heartRateDao(): HeartRateDao

    companion object {

        private const val DATABASE_NAME = "vital-database"

        fun buildDatabase(context: Context): VitalDatabase =
            Room.databaseBuilder(context, VitalDatabase::class.java, DATABASE_NAME)
                .build()
    }
}
