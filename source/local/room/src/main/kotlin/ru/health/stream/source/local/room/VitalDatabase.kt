package ru.health.stream.source.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.runBlocking
import ru.health.stream.source.local.room.converter.EmailConverter
import ru.health.stream.source.local.room.converter.InstantConverter
import ru.health.stream.source.local.room.converter.LengthConverter
import ru.health.stream.source.local.room.converter.LocalDateConverter
import ru.health.stream.source.local.room.dao.BloodPressureDao
import ru.health.stream.source.local.room.dao.BodyWeightDao
import ru.health.stream.source.local.room.dao.DeviceDao
import ru.health.stream.source.local.room.dao.HeartRateDao
import ru.health.stream.source.local.room.dao.NoteDao
import ru.health.stream.source.local.room.dao.OxygenSaturationDao
import ru.health.stream.source.local.room.dao.ResourceDao
import ru.health.stream.source.local.room.dao.UserDao
import ru.health.stream.source.local.room.entity.BloodPressureEntity
import ru.health.stream.source.local.room.entity.BodyWeightEntity
import ru.health.stream.source.local.room.entity.HeartRateEntity
import ru.health.stream.source.local.room.entity.NoteEntity
import ru.health.stream.source.local.room.entity.OxygenSaturationEntity
import ru.health.stream.source.local.room.entity.UserEntity
import ru.health.stream.source.local.room.entity.resource.DeviceEntity
import ru.health.stream.source.local.room.entity.resource.ResourceEntity

@Database(
    entities = [
        UserEntity::class,
        NoteEntity::class,
        DeviceEntity::class,
        ResourceEntity::class,
        HeartRateEntity::class,
        BodyWeightEntity::class,
        BloodPressureEntity::class,
        OxygenSaturationEntity::class,
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
    abstract fun bodyWeightDao(): BodyWeightDao
    abstract fun bloodPressureDao(): BloodPressureDao
    abstract fun oxygenSaturationDao(): OxygenSaturationDao

    companion object {

        private const val DATABASE_NAME = "vital-database"

        fun buildDatabase(context: Context, sqlCipherKeyManager: SqlCipherKeyManager): VitalDatabase = runBlocking {
            System.loadLibrary("sqlcipher")
            Room.databaseBuilder(context, VitalDatabase::class.java, DATABASE_NAME)
                .openHelperFactory(sqlCipherKeyManager.getSupportFactory())
                .build()
        }
    }
}
