package com.kostas.gohealth.helpers

import com.kostas.gohealth.data.entities.Characteristics
import kotlin.math.roundToInt

// For a better view, it rounds the goal
fun roundGoal(categoryGoal: Int): Int {
    val remainder = categoryGoal % 10
    val roundedCategoryGoal = when {
        remainder < 5 -> categoryGoal - remainder
        remainder == 5 -> categoryGoal
        else -> categoryGoal + (10 - remainder)
    }

    return roundedCategoryGoal
}


fun calculateWaterGoal(userCharacteristics: Characteristics?): Int {
    val weight = userCharacteristics?.weight ?: 70f

    val genderValues = mapOf(
        "Male" to 35,
        "Female" to 31
    ).withDefault { 33 }

    val genderValue = genderValues.getValue(userCharacteristics?.gender ?: "")

    val activityLevelValues = mapOf(
        "Sedentary" to 1.0f,
        "Moderate" to 1.2f,
        "High" to 1.4f
    ).withDefault { 1.2f }

    val activityLevelValue = activityLevelValues.getValue(userCharacteristics?.activityLevel ?: "")

    val waterGoal = ((weight * genderValue) * activityLevelValue).roundToInt()
    return roundGoal(waterGoal)
}


fun calculateCaloriesGoal(userCharacteristics: Characteristics?): Int {
    val weight = userCharacteristics?.weight ?: 70f
    val height = userCharacteristics?.height ?: 170f
    val age = userCharacteristics?.age ?: 30f
    val kgGoal = userCharacteristics?.kgGoal ?: 0
    val daysGoal = userCharacteristics?.daysGoal ?: 0

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

    val activityLevelValue = activityLevelValues.getValue(userCharacteristics?.activityLevel ?: "")

    val bmr = (10f * weight) + (6.25f * height) - (5f * age) + genderValue
    val tdee = bmr * activityLevelValue

    if (kgGoal != 0 && daysGoal != 0) { // If the user has set a calories lose/gain goal
        val maxDailyCaloriesDeficit = -(tdee * 0.30f)
        val maxDailyCaloriesSurplus = tdee * 0.20f

        // You need to eat 7700 less/more kcal to lose/gain 1kg, divide that number by the timeframe the user selected and get the daily
        // calories adjustment
        val caloriesChange = kgGoal * 7700
        val dailyCaloriesAdjustment = (caloriesChange / daysGoal.toFloat()).coerceIn(maxDailyCaloriesDeficit, maxDailyCaloriesSurplus)

        var caloriesGoal = (tdee + dailyCaloriesAdjustment).roundToInt()

        // It should 1500 and 1200, but upped it by 100, because of the 100 calories range
        val hardFloor = when (userCharacteristics?.gender) {
            "Male" -> 1600
            "Female" -> 1300
            else -> 1450
        }

        caloriesGoal = maxOf(caloriesGoal, hardFloor)
        return roundGoal(caloriesGoal)
    }

    else {
        return roundGoal(tdee.roundToInt())
    }
}


fun calculateExerciseGoal(userCharacteristics: Characteristics?): Int {
    val weight = userCharacteristics?.weight ?: 70f
    val height = userCharacteristics?.height ?: 170f

    val bmi = weight / ((height / 100) * (height / 100))

    val bmiBasedReps = when {
        bmi < 25 -> 40
        bmi < 30 -> 30
        else -> 20
    }

    val age = userCharacteristics?.age ?: 30f

    val ageValue = when {
        age < 30 -> 2.0f
        age < 50 -> 1.5f
        else -> 1.0f
    }

    val genderValues = mapOf(
        "Male" to 1.0f,
        "Female" to 0.65f
    ).withDefault { 0.825f }

    val genderValue = genderValues.getValue(userCharacteristics?.gender ?: "")

    val activityLevelValues = mapOf(
        "Sedentary" to 1.0f,
        "Moderate" to 1.5f,
        "High" to 2f
    ).withDefault { 1.5f }

    val activityLevelValue = activityLevelValues.getValue(userCharacteristics?.activityLevel ?: "")

    val weightGoalValues = mapOf(
        "Lose" to 1.2f,
        "Maintain" to 1.0f,
        "Gain" to 1.0f
    ).withDefault { 1.0f }

    val weightGoalValue = weightGoalValues.getValue(userCharacteristics?.weightGoal ?: "")

    val repsGoal = (bmiBasedReps * activityLevelValue * ageValue * genderValue * weightGoalValue).roundToInt()
    return roundGoal(repsGoal)
}


fun calculateStepsGoal(userCharacteristics: Characteristics?): Int {
    val activityLevelValues = mapOf(
        "Sedentary" to 6000,
        "Moderate" to 8000,
        "High" to 11000
    ).withDefault { 8000 }

    val activityLevelBasedSteps = activityLevelValues.getValue(userCharacteristics?.activityLevel ?: "")

    val weightGoalValues = mapOf(
        "Lose" to 2000,
        "Maintain" to 0,
        "Gain" to 0
    ).withDefault { 0 }

    val weightGoalValue = weightGoalValues.getValue(userCharacteristics?.weightGoal ?: "")

    val age = userCharacteristics?.age ?: 30f

    val ageValue = when {
        age < 30 -> 1000
        age < 50 -> 0
        else -> -1000
    }

    return activityLevelBasedSteps + weightGoalValue + ageValue
}
