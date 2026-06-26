package com.healthterra.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthterra.R
import com.healthterra.ui.components.general.ContributionCalendar
import com.healthterra.ui.components.general.RadioButtonGroup
import com.healthterra.ui.components.screen.ColorCodingHelper
import com.healthterra.ui.viewModels.DailyTrackingsViewModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen() {
    val dailyTrackingsViewModel: DailyTrackingsViewModel = viewModel(factory = DailyTrackingsViewModel.Factory)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM")

    // Converts the category daily trackings list to a monthly map (date is the key, the number of completed category goals is the
    // value). Because of remember, everytime the current month changes (by the sliders in ContributionCalendar), the database changes
    // or another category is selected, ContributionCalendar is updated
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    val currentYearMonthFormatted = currentYearMonth.format(formatter)
    val dailyTrackingsList by dailyTrackingsViewModel.dailyTrackingsFromYearMonth(currentYearMonthFormatted).collectAsState(initial = emptyList())
    var selectedCategory by remember { mutableStateOf("Combined") }

    val categoryContributionsMap = remember(dailyTrackingsList, selectedCategory) {
        dailyTrackingsList.associate { dailyTracking ->
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
        }
    }

    // Prepares this variable because there is different color coding for the combined option
    val maxGoalsMetCount = if (selectedCategory == "Combined") 4 else 1

    // Parses the oldest year month String into a YearMonth
    val oldestYearMonthString by dailyTrackingsViewModel.oldestYearMonthUserDailyTrackings().collectAsState(initial = null)
    val oldestYearMonth = remember(oldestYearMonthString) {
        oldestYearMonthString?.let { safeString ->
            val prefix = if (safeString.length >= 7) safeString.substring(0, 7) else safeString // Extracts "yyyy-MM" from "yyyy-MM-dd"
            YearMonth.parse(prefix, formatter)
        } ?: YearMonth.now()
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Text(
            text = "$selectedCategory Goal History",
            fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ContributionCalendar(
                currentYearMonth = currentYearMonth,
                oldestYearMonth = oldestYearMonth,
                contributionsMap = categoryContributionsMap,
                maxGoalsMetCount = maxGoalsMetCount,
                onYearMonthChange = { newYearMonth -> currentYearMonth = newYearMonth }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButtonGroup(
                    modifier = Modifier.weight(1f),
                    options = listOf("Combined", "Water", "Calories", "Exercise", "Steps"),
                    selectedOption = selectedCategory,
                    iconsList = listOf(R.drawable.combined, R.drawable.water, R.drawable.calories, R.drawable.exercise, R.drawable.steps),
                    showText = false
                ) { newCategory ->
                    selectedCategory = newCategory
                }

                ColorCodingHelper(maxGoalsMetCount = maxGoalsMetCount)
            }
        }
    }
}
