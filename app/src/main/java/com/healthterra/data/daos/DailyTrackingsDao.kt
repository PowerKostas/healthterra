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

    @Query("SELECT * FROM daily_trackings WHERE date LIKE :yearMonth || '%'")
    fun getDailyTrackingsFromYearMonth(yearMonth: String): Flow<List<DailyTrackings>>

    @Query("SELECT MIN(date) FROM daily_trackings")
    fun getOldestYearMonth(): Flow<String?>

    // If a row with today's date and userId doesn't exist, it inserts it, otherwise it updates the existing row
    @Upsert
    suspend fun upsert(dailyTracking: DailyTrackings)

    @Query("DELETE FROM daily_trackings")
    suspend fun delete()
}
