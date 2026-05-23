package com.kostas.gohealth.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.kostas.gohealth.data.entities.Trackings
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackingsDao {
    @Query("SELECT * FROM trackings")
    fun getAll(): Flow<List<Trackings>>

    @Insert
    suspend fun insert(trackings: Trackings)

    @Update
    suspend fun update(trackings: Trackings)

    @Query("UPDATE trackings SET steps_progress = :newSteps WHERE userId = :uid")
    suspend fun updateStepsProgress(uid: Int, newSteps: Int)
}
