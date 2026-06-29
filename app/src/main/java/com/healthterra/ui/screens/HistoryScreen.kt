package com.healthterra.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthterra.R
import com.healthterra.data.entities.DailyTrackings
import com.healthterra.helpers.generateContributionsMap
import com.healthterra.helpers.processGraphData
import com.healthterra.ui.components.general.ContributionCalendar
import com.healthterra.ui.components.general.DynamicLineGraph
import com.healthterra.ui.components.general.PillButtonGroup
import com.healthterra.ui.components.general.RadioButtonGroup
import com.healthterra.ui.components.screen.ColorCodingHelper
import com.healthterra.ui.viewModels.CharacteristicsViewModel
import com.healthterra.ui.viewModels.DailyTrackingsViewModel
import com.healthterra.ui.viewModels.TodayTrackingsViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen() {
    val dailyTrackingsViewModel: DailyTrackingsViewModel = viewModel(factory = DailyTrackingsViewModel.Factory)

    val todayTrackingsViewModel = viewModel<TodayTrackingsViewModel>(factory = TodayTrackingsViewModel.Factory)
    val userTodayTrackingsList by todayTrackingsViewModel.todayTrackings.collectAsState()
    val userTodayTrackings = userTodayTrackingsList.firstOrNull()

    val characteristicsViewModel = viewModel<CharacteristicsViewModel>(factory = CharacteristicsViewModel.Factory)
    val userCharacteristicsList by characteristicsViewModel.characteristics.collectAsState()
    val userCharacteristics = userCharacteristicsList.firstOrNull()

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM")

    // Everything below is for the ContributionCalendar. Converts the category daily trackings list to a monthly map (date is the
    // key, the number of completed category goals is the value). Because of remember, everytime the current month changes (by the
    // sliders in ContributionCalendar), the database changes or another category is selected, ContributionCalendar is updated. Also
    // injects today's data directly into the map
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    val currentYearMonthFormatted = currentYearMonth.format(formatter)
    val monthlyDailyTrackingsList by dailyTrackingsViewModel.dailyTrackingsFromYearMonth(currentYearMonthFormatted).collectAsState(initial = emptyList())
    var selectedCategoryContributionCalendar by rememberSaveable { mutableStateOf("Combined") }

    val categoryContributionsMap = remember(monthlyDailyTrackingsList, userTodayTrackings, userCharacteristics, selectedCategoryContributionCalendar) {
        generateContributionsMap(
            dailyTrackingsList = monthlyDailyTrackingsList,
            userTodayTrackings = userTodayTrackings,
            userCharacteristics = userCharacteristics,
            selectedCategory = selectedCategoryContributionCalendar
        )
    }

    // Prepares this variable because there is different color coding for the combined option
    val maxGoalsMetCount = if (selectedCategoryContributionCalendar == "Combined") 4 else 1

    // Parses the oldest year month String into a YearMonth
    val oldestYearMonthString by dailyTrackingsViewModel.oldestYearMonthUserDailyTrackings().collectAsState(initial = null)
    val oldestYearMonth = remember(oldestYearMonthString) {
        oldestYearMonthString?.let { safeString ->
            val prefix = if (safeString.length >= 7) safeString.substring(0, 7) else safeString // Extracts "yyyy-MM" from "yyyy-MM-dd"
            YearMonth.parse(prefix, formatter)
        } ?: YearMonth.now()
    }

    var selectedSquareDate by remember { mutableStateOf<String?>(null) }

    // Finds the tracking data for the selected square date, today's data is separate
    val selectedSquareDetails = remember(selectedSquareDate, monthlyDailyTrackingsList, userTodayTrackings) {
        val safeDate = selectedSquareDate ?: LocalDate.now().toString()

        if (selectedSquareDate == null) {
            null
        }

        else {
            if (LocalDate.now().toString() == selectedSquareDate) {
                userTodayTrackings?.let { today ->
                    DailyTrackings(
                        userId = today.userId,
                        date = safeDate,
                        waterProgress = today.waterProgress.sum(),
                        caloriesProgress = today.caloriesProgress.sum(),
                        exerciseProgress = today.exerciseProgress.sum(),
                        stepsProgress = today.stepsProgress
                    )
                }
            }

            else {
                monthlyDailyTrackingsList.find { it.date == selectedSquareDate }
            }
        }
    }

    // Data preparation for the graphs, works similar to the ContributionCalendar one above
    val dailyTrackingsList by dailyTrackingsViewModel.dailyTrackings().collectAsState(initial = emptyList())
    var selectedGraphRange by remember { mutableStateOf("Week") }
    var selectedCategoryGraph by rememberSaveable { mutableStateOf("Steps") }

    // Returns a pair instead of a map because non-unique keys can happen
    val (chartValues, chartLabels) = remember(dailyTrackingsList, selectedGraphRange, selectedCategoryGraph) {
        processGraphData(dailyTrackingsList, selectedGraphRange, selectedCategoryGraph)
    }


    Column(
        verticalArrangement = Arrangement.spacedBy(64.dp, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "$selectedCategoryContributionCalendar Goal History",
                fontWeight = FontWeight.Bold
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ContributionCalendar(
                    currentYearMonth = currentYearMonth,
                    oldestYearMonth = oldestYearMonth,
                    contributionsMap = categoryContributionsMap,
                    maxGoalsMetCount = maxGoalsMetCount,
                    selectedSquareDate = selectedSquareDate,
                    selectedSquareDetails = selectedSquareDetails,
                    onYearMonthChange = { newYearMonth -> currentYearMonth = newYearMonth },
                    onSquareClick = { clickedSquareDate -> selectedSquareDate = clickedSquareDate },
                    onPopupDismiss = { selectedSquareDate = null }
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButtonGroup(
                        modifier = Modifier.weight(1f),
                        options = listOf("Combined", "Water", "Calories", "Exercise", "Steps"),
                        selectedOption = selectedCategoryContributionCalendar,
                        iconsList = listOf(R.drawable.combined, R.drawable.water, R.drawable.calories, R.drawable.exercise, R.drawable.steps),
                        showText = false
                    ) { newCategory ->
                        selectedCategoryContributionCalendar = newCategory
                    }

                    ColorCodingHelper(maxGoalsMetCount = maxGoalsMetCount)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "$selectedCategoryGraph Graph",
                    fontWeight = FontWeight.Bold
                )

                if (chartValues.size <= 1) {
                    // Matches the height of DynamicGraph
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    ) {
                        Text(
                            text = if (selectedGraphRange == "All Time") "Not enough data for an All-Time graph" else "Not enough data for a $selectedGraphRange graph",
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else {
                    DynamicLineGraph(chartValues, chartLabels)
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButtonGroup(
                    modifier = Modifier.weight(1f),
                    options = listOf("Water", "Calories", "Exercise", "Steps"),
                    selectedOption = selectedCategoryGraph,
                    iconsList = listOf(R.drawable.water, R.drawable.calories, R.drawable.exercise, R.drawable.steps),
                    showText = false
                ) { selectedCategoryGraph = it }

                PillButtonGroup(
                    options = listOf("Week", "Month", "Year", "All Time"),
                    selectedRange = selectedGraphRange,
                    onOptionSelected = { selectedGraphRange = it }
                )
            }
        }
    }
}
