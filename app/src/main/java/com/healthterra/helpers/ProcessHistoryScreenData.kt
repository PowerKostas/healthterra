package com.healthterra.helpers

import com.healthterra.data.entities.Characteristics
import com.healthterra.data.entities.DailyTrackings
import com.healthterra.data.entities.TodayTrackings
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

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


fun processGraphData(dailyTrackingsList: List<DailyTrackings>, graphTimeRange: String, selectedCategory: String): Pair<List<Int>, List<String>> {
    val today = LocalDate.now()
    if (dailyTrackingsList.isEmpty()) return Pair(emptyList(), emptyList())

    // Date is the key, category progress is the value
    val getValue: (DailyTrackings) -> Int = when (selectedCategory) {
        "Water" -> { it -> it.waterProgress }
        "Calories" -> { it -> it.caloriesProgress }
        "Exercise" -> { it -> it.exerciseProgress }
        else -> { it -> it.stepsProgress }
    }

    val categoryDailyTrackingsMap = dailyTrackingsList.associate {
        LocalDate.parse(it.date) to getValue(it)
    }

    val values = mutableListOf<Int>()
    val labels = mutableListOf<String>()

    when (graphTimeRange) {
        // Shows data for the last 7 days, each value is a day
        "Week" -> {
            val start = today.minusDays(6)
            for (i in 0..6L) {
                val date = start.plusDays(i)
                values.add(categoryDailyTrackingsMap[date] ?: 0)
                labels.add(date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
            }
        }

        // Shows data for the last month, each value is the average of a week
        "Month" -> {
            var currentStart = today.minusMonths(1)
            val formatter = DateTimeFormatter.ofPattern("dd/MM")

            while (currentStart.isBefore(today)) {
                // Because each month is 4 weeks + 0, 1, 2 or 3 days, this creates 4 chunks of 7 days and 1 chunk of whatever is
                // remaining, if needed
                val currentEnd = minOf(currentStart.plusDays(6), today)

                val dataInRange = (0..ChronoUnit.DAYS.between(currentStart, currentEnd))
                    .mapNotNull { categoryDailyTrackingsMap[currentStart.plusDays(it)] }

                values.add(if (dataInRange.isNotEmpty()) dataInRange.average().toInt() else 0)
                labels.add("${currentStart.format(formatter)} -\n${currentEnd.format(formatter)}")

                currentStart = currentStart.plusDays(7)
            }
        }

        // Shows data for the last year, each value is the average of a month
        "Year" -> {
            val startMonth = YearMonth.from(today.minusYears(1).plusMonths(1))

            for (i in 0..11L) {
                val tempYearMonth = startMonth.plusMonths(i)
                val yearMonthValues = categoryDailyTrackingsMap.filterKeys { YearMonth.from(it) == tempYearMonth }.values
                val avg = if (yearMonthValues.isNotEmpty()) yearMonthValues.average() else 0.0

                // Only adds the point to the graph if the average for the month is greater than 0
                if (avg > 0.0) {
                    values.add(avg.toInt())
                    labels.add(tempYearMonth.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
                }
            }
        }

        // Shows all time data, each value is the average of a year
        "All Time" -> {
            val startYear = categoryDailyTrackingsMap.keys.minOrNull()?.year ?: today.year

            for (y in startYear..today.year) {
                val yearValues = categoryDailyTrackingsMap.filterKeys { it.year == y }.values
                val avg = if (yearValues.isNotEmpty()) yearValues.average() else 0.0
                values.add(avg.toInt())
                labels.add(y.toString())
            }
        }
    }

    return Pair(values, labels)
}
