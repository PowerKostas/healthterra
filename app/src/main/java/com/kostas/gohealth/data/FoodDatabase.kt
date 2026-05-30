package com.kostas.gohealth.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kostas.gohealth.data.daos.FoodDao
import com.kostas.gohealth.data.entities.Food

// Separate database for the static food data, if I make a change, bump the version number and the new table will replace with the old one
@Database(
    entities = [Food::class],
    version = 1,
    exportSchema = false
)
abstract class FoodDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao

    companion object {
        @Volatile
        private var INSTANCE: FoodDatabase? = null

        fun getDatabase(context: Context): FoodDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FoodDatabase::class.java,
                    "food_database"
                )
                    .createFromAsset("databases/item_size_kcal.db")
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
