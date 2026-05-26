package com.kostas.gohealth.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kostas.gohealth.helpers.generateRandomProfilePictureString
import com.kostas.gohealth.helpers.generateRandomUsername
import java.time.LocalDate

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    @ColumnInfo(name = "profile_picture_string") val profilePictureString: String = generateRandomProfilePictureString(),

    // A default value is needed because this column was changed in a later migration
    @ColumnInfo(name = "username", defaultValue = "Guest") val username: String = generateRandomUsername(),

    @ColumnInfo(name = "appearance") val appearance: String = "Light",
    @ColumnInfo(name = "last_saved_steps") val lastSavedSteps: Int = 0,
    @ColumnInfo(name = "step_tracking") val stepTracking: String = "Enabled",
    @ColumnInfo(name = "last_saved_date") val lastSavedDate: String = LocalDate.now().toString(),

    // String? is needed because this column was added in a later migration, will initialize it in the code too
    @ColumnInfo(name = "initial_weight_goal_date") val initialWeightGoalDate: String? = LocalDate.now().toString()
)
