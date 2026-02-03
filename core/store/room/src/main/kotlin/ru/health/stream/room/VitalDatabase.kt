package ru.health.stream.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.health.stream.room.converter.InstantConverter
import ru.health.stream.room.converter.ResourceConverter
import ru.health.stream.room.converter.StatusConverter
import ru.health.stream.room.dao.DeviceDao
import ru.health.stream.room.dao.HeartRateDao
import ru.health.stream.room.entity.DeviceEntity
import ru.health.stream.room.entity.HeartRateEntity

@Database(entities = [HeartRateEntity::class, DeviceEntity::class], version = 1)
@TypeConverters(InstantConverter::class, ResourceConverter::class, StatusConverter::class)
internal abstract class VitalDatabase : RoomDatabase() {

    abstract fun deviceDao(): DeviceDao
    abstract fun heartRateDao(): HeartRateDao

    companion object {

        private const val DATABASE_NAME = "vital-database"

        fun buildDatabase(context: Context): VitalDatabase =
            Room.databaseBuilder(context, VitalDatabase::class.java, DATABASE_NAME)
                .build()
    }
}
