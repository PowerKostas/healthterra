package com.healthterra.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.healthterra.data.daos.CharacteristicsDao
import com.healthterra.data.daos.DailyTrackingsDao
import com.healthterra.data.daos.SettingsDao
import com.healthterra.data.daos.TrackingsDao
import com.healthterra.data.entities.Characteristics
import com.healthterra.data.entities.DailyTrackings
import com.healthterra.data.entities.Settings
import com.healthterra.data.entities.Trackings

// Separate database for the variable user data, if I make a change, because of the auto migrations code, the user doesn't have to delete the
// app and reinstall it. When I make a change, don't run the app without a new auto migration and a change in build.gradle.kts (app), if I
// accidentally do, just delete the app in the emulator and redo the process
@TypeConverters(Converters::class) // Automatically runs the converters, I can just use the lists as lists in code now
@Database(
    entities = [Settings::class, Characteristics::class, Trackings::class, DailyTrackings::class],
    version = 5,
    autoMigrations = [
        AutoMigration(from = 4, to = 5),
        //AutoMigration(from = 5, to = 6, spec = AppDatabase.MyRenameMigration::class)
    ]
)
abstract class UserDatabase : RoomDatabase() {
    /*
    @RenameColumn(
        tableName = "trackings",
        fromColumnName = "push_ups_progress",
        toColumnName = "exercise_progress"
    )
    class MyRenameMigration : AutoMigrationSpec
    */

    abstract fun characteristicsDao(): CharacteristicsDao
    abstract fun settingsDao(): SettingsDao
    abstract fun trackingsDao(): TrackingsDao
    abstract fun dailyTrackingsDao(): DailyTrackingsDao

    companion object {
        @Volatile
        private var INSTANCE: UserDatabase? = null

        fun getDatabase(context: Context): UserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "user_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}
