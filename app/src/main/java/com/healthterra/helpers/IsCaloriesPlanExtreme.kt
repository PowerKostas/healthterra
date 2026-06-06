package com.healthterra.helpers

import com.healthterra.data.entities.Characteristics

// To show error message in the profile screen, very similar to Formulas.calculateCaloriesGoal
fun isCaloriesPlanExtreme(userCharacteristics: Characteristics?): Boolean {
    val weight = userCharacteristics?.weight ?: 70f
    val height = userCharacteristics?.height ?: 170f
    val age = userCharacteristics?.age ?: 30f
    val kgGoal = userCharacteristics?.kgGoal ?: 0
    val daysGoal = userCharacteristics?.daysGoal ?: 0

    if (kgGoal != 0 && daysGoal != 0) { // If the user has set a calories lose/gain goal
        val genderValues = mapOf(
            "Male" to 5,
            "Female" to -161
        ).withDefault { -78 }

        val genderValue = genderValues.getValue(userCharacteristics?.gender ?: "")

        val activityLevelValues = mapOf(
            "Sedentary" to 1.2f,
            "Moderate" to 1.55f,
            "High" to 1.725f
        ).withDefault { 1.55f }

        val activityLevelValue =
            activityLevelValues.getValue(userCharacteristics?.activityLevel ?: "")

        val bmr = (10f * weight) + (6.25f * height) - (5f * age) + genderValue
        val tdee = bmr * activityLevelValue

        val maxDailyCaloriesDeficit = -(tdee * 0.30f)
        val maxDailyCaloriesSurplus = tdee * 0.20f

        val caloriesChange = kgGoal * 7700f
        val dailyCaloriesAdjustment = caloriesChange / daysGoal.toFloat()

        // If the daily calories adjustment is outside the deficit surplus range, it will get coerced in the calculateCaloriesGoal function, show
        // the error
        return dailyCaloriesAdjustment !in maxDailyCaloriesDeficit..maxDailyCaloriesSurplus
    }

    else {
        return false
    }
}