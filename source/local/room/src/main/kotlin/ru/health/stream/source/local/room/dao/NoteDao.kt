package ru.health.stream.source.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.health.stream.source.local.room.entity.NoteEntity

@Dao
internal interface NoteDao {

    @Query("SELECT * FROM note WHERE id == :id")
    suspend fun getNoteById(id: String): NoteEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllNotes(entities: List<NoteEntity>)
}
