package com.healthterra.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.healthterra.helpers.generateRandomProfilePictureString
import com.healthterra.helpers.generateRandomUsername
import java.time.LocalDate

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    @ColumnInfo(name = "profile_picture_string") val profilePictureString: String = generateRandomProfilePictureString(),
    @ColumnInfo(name = "username") val username: String = generateRandomUsername(),
    @ColumnInfo(name = "initial_weight_goal_date") val initialWeightGoalDate: String? = null,
    @ColumnInfo(name = "appearance") val appearance: String = "Light",
    @ColumnInfo(name = "step_tracking") val stepTracking: String = "Enabled",
    @ColumnInfo(name = "last_saved_date") val lastSavedDate: String = LocalDate.now().toString(),
    @ColumnInfo(name = "last_saved_steps") val lastSavedSteps: Int = 0,

    // These columns were added in a later migration, that's why they have a default value
    @ColumnInfo(name = "show_mandatory_dialog", defaultValue = "1") val showMandatoryDialog: Boolean = true,
    @ColumnInfo(name = "leaderboards_visibility", defaultValue = "'Anonymous'") val leaderboardsVisibility: String = "Anonymous"
)
