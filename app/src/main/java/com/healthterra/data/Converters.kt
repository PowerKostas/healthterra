package com.healthterra.data

import androidx.room.TypeConverter

// I have to use this trick because SQLite cannot store lists in a column
class Converters {
    // Converts the list to a comma-separated string: "1,2,3"
    @TypeConverter
    fun fromIntList(list: List<Int>?): String? {
        return list?.joinToString(", ")
    }

    // Converts the comma-separated string back to a list of Ints
    @TypeConverter
    fun toIntList(data: String?): List<Int> {
        if (data.isNullOrBlank()) return emptyList()
        return data.split(", ").map { it.toInt() }
    }
}
