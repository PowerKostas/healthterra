package com.healthterra.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "achievements",
    foreignKeys = [
        ForeignKey(
            entity = Settings::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class Achievements(
    @PrimaryKey val userId: Int,
    @ColumnInfo(name = "appeared_on_water_leaderboards") val appearedOnWaterLeaderboards: Boolean = false,
    @ColumnInfo(name = "appeared_on_calories_leaderboards") val appearedOnCaloriesLeaderboards: Boolean = false,
    @ColumnInfo(name = "appeared_on_exercise_leaderboards") val appearedOnExerciseLeaderboards: Boolean = false,
    @ColumnInfo(name = "appeared_on_steps_leaderboards") val appearedOnStepsLeaderboards: Boolean = false,
    @ColumnInfo(name = "appeared_on_total_steps_leaderboards") val appearedOnTotalStepsLeaderboards: Boolean = false,
    @ColumnInfo(name = "appeared_on_healthiest_user") val appearedOnHealthiestUser: Boolean = false,
    @ColumnInfo(name = "secret") val secret: Boolean = false,
)
