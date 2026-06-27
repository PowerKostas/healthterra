package com.healthterra.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.healthterra.data.entities.TodayTrackings
import kotlinx.coroutines.flow.Flow

@Dao
interface TodayTrackingsDao {
    @Query("SELECT * FROM today_trackings")
    fun getAll(): Flow<List<TodayTrackings>>

    @Insert
    suspend fun insert(todayTrackings: TodayTrackings)

    @Update
    suspend fun update(todayTrackings: TodayTrackings)

    @Query("UPDATE today_trackings SET steps_progress = :newSteps WHERE userId = :uid")
    suspend fun updateStepsProgress(uid: Int, newSteps: Int)
}
