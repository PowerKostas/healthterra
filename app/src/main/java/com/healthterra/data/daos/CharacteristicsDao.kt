package com.healthterra.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.healthterra.data.entities.Characteristics
import kotlinx.coroutines.flow.Flow

// The DAO (Data Access Object) is an interface that defines how to read and write to the database. Flow means that the table that the
// getters use auto updates after every modification. Suspend means that it runs on a different thread than the main one. There is no
// deleting function in the app, so we don't need one here too
@Dao
interface CharacteristicsDao {
    @Query("SELECT * FROM characteristics")
    fun getAll(): Flow<List<Characteristics>>

    @Insert
    suspend fun insert(characteristics: Characteristics)

    @Update
    suspend fun update(characteristics: Characteristics)
}
