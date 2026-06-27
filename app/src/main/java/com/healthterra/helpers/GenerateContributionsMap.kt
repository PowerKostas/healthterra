package com.healthterra.helpers

import com.healthterra.data.entities.Characteristics
import com.healthterra.data.entities.DailyTrackings
import com.healthterra.data.entities.TodayTrackings
import java.time.LocalDate

fun generateContributionsMap(dailyTrackingsList: List<DailyTrackings>, userTodayTrackings: TodayTrackings?, userCharacteristics: Characteristics?, selectedCategory: String): Map<String, Int> {
    val map = dailyTrackingsList.associate { dailyTracking ->
        val goalsMetCount = when (selectedCategory) {
            "Combined" -> listOf(
                dailyTracking.waterProgress >= dailyTracking.waterGoal,
                dailyTracking.caloriesProgress >= dailyTracking.caloriesGoal,
                dailyTracking.exerciseProgress >= dailyTracking.exerciseGoal,
                dailyTracking.stepsProgress >= dailyTracking.stepsGoal
            ).count { it }

            "Water" -> if (dailyTracking.waterProgress >= dailyTracking.waterGoal) 1 else 0
            "Calories" -> if (dailyTracking.caloriesProgress >= dailyTracking.caloriesGoal) 1 else 0
            "Exercise" -> if (dailyTracking.exerciseProgress >= dailyTracking.exerciseGoal) 1 else 0
            "Steps" -> if (dailyTracking.stepsProgress >= dailyTracking.stepsGoal) 1 else 0
            else -> 0
        }

        dailyTracking.date to goalsMetCount
    }.toMutableMap()

    if (userTodayTrackings != null && userCharacteristics != null) {
        val todayDate = LocalDate.now().toString()

        val waterGoal = calculateWaterGoal(userCharacteristics)
        val caloriesGoal = calculateCaloriesGoal(userCharacteristics)
        val exerciseGoal = calculateExerciseGoal(userCharacteristics)
        val stepsGoal = calculateStepsGoal(userCharacteristics)

        val waterProgress = userTodayTrackings.waterProgress.sum()
        val caloriesProgress = userTodayTrackings.caloriesProgress.sum()
        val exerciseProgress = userTodayTrackings.exerciseProgress.sum()
        val stepsProgress = userTodayTrackings.stepsProgress

        val todayGoalsMetCount = when (selectedCategory) {
            "Combined" -> listOf(
                waterProgress >= waterGoal,
                caloriesProgress >= caloriesGoal,
                exerciseProgress >= exerciseGoal,
                stepsProgress >= stepsGoal
            ).count { it }

            "Water" -> if (waterProgress >= waterGoal) 1 else 0
            "Calories" -> if (caloriesProgress >= caloriesGoal) 1 else 0
            "Exercise" -> if (exerciseProgress >= exerciseGoal) 1 else 0
            "Steps" -> if (stepsProgress >= stepsGoal) 1 else 0
            else -> 0
        }

        map[todayDate] = todayGoalsMetCount
    }

    return map
}
