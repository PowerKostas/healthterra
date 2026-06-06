package com.healthterra.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// An entity represents a table within the local Room API database, uses userId from Settings as a foreign key
@Entity(
    tableName = "characteristics",
    foreignKeys = [
        ForeignKey(
            entity = Settings::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Characteristics(
    @PrimaryKey val userId: Int,
    @ColumnInfo(name = "gender") val gender: String? = null,
    @ColumnInfo(name = "age") val age: Float? = null,
    @ColumnInfo(name = "height") val height: Float? = null,
    @ColumnInfo(name = "weight") val weight: Float? = null,
    @ColumnInfo(name = "activity_level") val activityLevel: String? = null,
    @ColumnInfo(name = "weight_goal") val weightGoal: String = "Maintain",
    @ColumnInfo(name = "kg_goal") val kgGoal: Int = 0,
    @ColumnInfo(name = "days_goal") val daysGoal: Int = 0
)
