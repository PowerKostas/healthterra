package com.healthterra.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.healthterra.data.entities.Achievements
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementsDao {
    @Query("SELECT * FROM achievements")
    fun getAll(): Flow<List<Achievements>>

    @Insert
    suspend fun insert(achievements: Achievements)

    @Update
    suspend fun update(achievements: Achievements)
}
