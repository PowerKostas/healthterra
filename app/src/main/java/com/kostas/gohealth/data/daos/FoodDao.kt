package com.kostas.gohealth.data.daos

import androidx.room.Dao
import androidx.room.Query
import com.kostas.gohealth.data.entities.Food

@Dao
interface FoodDao {
    // Finds any instance of the searchQuery inside a String
    @Query("SELECT * FROM food WHERE item LIKE '%' || :searchQuery || '%'")
    suspend fun searchFood(searchQuery: String): List<Food>
}
