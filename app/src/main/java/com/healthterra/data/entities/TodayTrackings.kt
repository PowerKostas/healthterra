package com.healthterra.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// Holds just today's trackings
@Entity(
    tableName = "today_trackings",
    foreignKeys = [
        ForeignKey(
            entity = Settings::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

// Every non steps column will hold a list of all the additions of the user, the reason is the undo button, breaks 1NF but ok
data class TodayTrackings(
    @PrimaryKey val userId: Int,
    @ColumnInfo(name = "water_progress") val waterProgress: List<Int> = emptyList(),
    @ColumnInfo(name = "calories_progress") val caloriesProgress: List<Int> = emptyList(),
    @ColumnInfo(name = "exercise_progress") val exerciseProgress: List<Int> = emptyList(),
    @ColumnInfo(name = "steps_progress") val stepsProgress: Int = 0,
)
