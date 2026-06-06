package com.healthterra.data.daos

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.healthterra.data.entities.Food

@Dao
interface FoodDao {
    // Executes dynamic SQL queries, because the view model handles each input word individually, and it doesn't know how many words the
    // user will type
    @RawQuery
    suspend fun searchFoodRaw(query: SupportSQLiteQuery): List<Food>
}
