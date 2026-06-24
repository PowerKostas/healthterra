package com.healthterra.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

// Holds history of all the trackings
@Entity(
    tableName = "daily_trackings",
    primaryKeys = ["userId", "date"], // Composite primary key, that's why its put here
    foreignKeys = [
        ForeignKey(
            entity = Settings::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DailyTrackings(
    val userId: Int,
    val date: String,
    @ColumnInfo(name = "water_progress") val waterProgress: Int = 0,
    @ColumnInfo(name = "calories_progress") val caloriesProgress: Int = 0,
    @ColumnInfo(name = "exercise_progress") val exerciseProgress: Int = 0,
    @ColumnInfo(name = "steps_progress") val stepsProgress: Int = 0,
    @ColumnInfo(name = "water_goal") val waterGoal: Int = 0,
    @ColumnInfo(name = "calories_goal") val caloriesGoal: Int = 0,
    @ColumnInfo(name = "exercise_goal") val exerciseGoal: Int = 0,
    @ColumnInfo(name = "steps_goal") val stepsGoal: Int = 0
)
