package com.kostas.gohealth.data

import android.content.Context
import androidx.room.Room

// The View Models call this function, which creates the database if it doesn't exist already, and it guarantees that exactly one instance
// of the database is ever created
object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            )
                .createFromAsset("databases/item_size_calories.db")
                .build()

            INSTANCE = instance
            instance
        }
    }
}
