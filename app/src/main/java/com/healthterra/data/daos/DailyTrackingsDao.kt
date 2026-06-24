package com.healthterra.data.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.healthterra.data.entities.DailyTrackings
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyTrackingsDao {
    @Query("SELECT * FROM daily_trackings ORDER BY date DESC LIMIT :limit")
    fun getDailyTrackings(limit: Int): Flow<List<DailyTrackings>>

    // If a row with today's date and userId doesn't exist, it inserts it, otherwise it updates the existing row
    @Upsert
    suspend fun upsert(dailyTracking: DailyTrackings)

    @Query("DELETE FROM daily_trackings")
    suspend fun delete()
}
