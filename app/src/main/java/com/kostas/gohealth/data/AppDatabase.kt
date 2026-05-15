package com.kostas.gohealth.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.kostas.gohealth.data.daos.CharacteristicsDao
import com.kostas.gohealth.data.daos.SettingsDao
import com.kostas.gohealth.data.daos.TrackingsDao
import com.kostas.gohealth.data.entities.Characteristics
import com.kostas.gohealth.data.entities.Settings
import com.kostas.gohealth.data.entities.Trackings

// Because of all the migration code, the user doesn't have to delete the app and reinstall it, if I change the database. When I change the
// database, don't run the app without a new auto migration, if I accidentally do, just delete the app in the emulator and redo the process
@TypeConverters(Converters::class) // Automatically runs the converters, I can just use the lists/dates as lists/dates in code now
@Database(
    entities = [Settings::class, Characteristics::class, Trackings::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = AppDatabase.MyRenameMigration::class),
    ]
)
abstract class AppDatabase : RoomDatabase() {
    @RenameColumn(
        tableName = "trackings",
        fromColumnName = "push_ups_progress",
        toColumnName = "exercise_progress"
    )
    class MyRenameMigration : AutoMigrationSpec

    abstract fun characteristicsDao(): CharacteristicsDao
    abstract fun settingsDao(): SettingsDao
    abstract fun trackingsDao(): TrackingsDao
}
