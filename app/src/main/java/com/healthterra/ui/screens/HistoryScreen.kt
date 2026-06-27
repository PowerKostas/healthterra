package com.healthterra.ui.screens

import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthterra.R
import com.healthterra.data.entities.DailyTrackings
import com.healthterra.helpers.generateContributionsMap
import com.healthterra.ui.components.general.ContributionCalendar
import com.healthterra.ui.components.general.RadioButtonGroup
import com.healthterra.ui.components.screen.ColorCodingHelper
import com.healthterra.ui.viewModels.CharacteristicsViewModel
import com.healthterra.ui.viewModels.DailyTrackingsViewModel
import com.healthterra.ui.viewModels.TodayTrackingsViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.ceil

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

    // Converts the category daily trackings list to a monthly map (date is the key, the number of completed category goals is the
    // value). Because of remember, everytime the current month changes (by the sliders in ContributionCalendar), the database changes
    // or another category is selected, ContributionCalendar is updated. Also injects today's data directly into the map
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    val currentYearMonthFormatted = currentYearMonth.format(formatter)
    val monthlyDailyTrackingsList by dailyTrackingsViewModel.dailyTrackingsFromYearMonth(currentYearMonthFormatted).collectAsState(initial = emptyList())
    var selectedCategory by remember { mutableStateOf("Combined") }

    val categoryContributionsMap = remember(monthlyDailyTrackingsList, userTodayTrackings, userCharacteristics, selectedCategory) {
        generateContributionsMap(
            dailyTrackingsList = monthlyDailyTrackingsList,
            userTodayTrackings = userTodayTrackings,
            userCharacteristics = userCharacteristics,
            selectedCategory = selectedCategory
        )
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

        val dailyTrackingsList by dailyTrackingsViewModel.dailyTrackings().collectAsState(initial = emptyList())
        WeeklyStepsBarGraph(dailyTrackingsList)
    }
}



@Composable
fun WeeklyStepsBarGraph(dailyTrackings: List<DailyTrackings>) {
    val weeklyData = dailyTrackings.takeLast(7)
    val textMeasurer = rememberTextMeasurer()
    val barColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        val steps = weeklyData.map { it.stepsProgress.toFloat() }
        val daysOfWeek = weeklyData.map { dailyTracking ->
            val date = LocalDate.parse(dailyTracking.date)
            date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        }

        val rawMax = steps.maxOrNull() ?: 0f
        val maxStep = if (rawMax == 0f) 1000f else (ceil(rawMax / 1000.0) * 1000).toFloat()

        val yAxisPadding = 125f
        val xAxisPadding = 75f
        val graphWidth = size.width - yAxisPadding
        val graphHeight = size.height - xAxisPadding

        // 1. Draw the Y-axis checkpoints (Same as your original code)
        val numCheckpoints = 5
        for (i in 0 until numCheckpoints) {
            val stepValue = (maxStep / (numCheckpoints - 1)) * i
            val yCheckpoint = graphHeight - ((stepValue / maxStep) * graphHeight)

            drawLine(
                color = Color.LightGray,
                start = Offset(yAxisPadding, yCheckpoint),
                end = Offset(size.width, yCheckpoint),
                strokeWidth = 2f
            )

            drawText(
                textMeasurer = textMeasurer,
                text = stepValue.toInt().toString(),
                topLeft = Offset(0f, yCheckpoint - 25f)
            )
        }

        // 2. Draw the Bars (MUCH SIMPLER)
        val sectionWidth = graphWidth / steps.size
        val barWidth = sectionWidth * 0.6f // Bars take up 60% of their section

        steps.forEachIndexed { index, stepValue ->
            val barHeight = (stepValue / maxStep) * graphHeight
            // Center the bar inside its section
            val x = yAxisPadding + (index * sectionWidth) + ((sectionWidth - barWidth) / 2f)
            val y = graphHeight - barHeight

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f) // Rounded tops
            )
        }

        // 3. Draw X-axis texts centered under each bar
        daysOfWeek.forEachIndexed { index, day ->
            val textLayoutResult = textMeasurer.measure(day)
            val textWidth = textLayoutResult.size.width
            val x = yAxisPadding + (index * sectionWidth) + ((sectionWidth - textWidth) / 2f)

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(x, graphHeight + 15f)
            )
        }
    }
}



@Composable
fun WeeklyStepsGraph(dailyTrackings: List<DailyTrackings>) {
    val weeklyData = dailyTrackings.takeLast(7)
    val textMeasurer = rememberTextMeasurer()
    val lineColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        val steps = weeklyData.map { it.stepsProgress.toFloat() }

        // Converts "YYYY-MM-DD" to "Mon", "Tue" ...
        val daysOfWeek = weeklyData.map { dailyTracking ->
            val date = LocalDate.parse(dailyTracking.date)
            date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        }

        // Rounds max steps up to the nearest 1000
        val rawMax = steps.maxOrNull() ?: 0f
        val maxStep = (ceil(rawMax / 1000.0) * 1000).toFloat()

        val yAxisPadding = 125f
        val xAxisPadding = 75f
        val graphWidth = size.width - yAxisPadding
        val graphHeight = size.height - xAxisPadding

        // Draws the y-axis 5 checkpoints and grid lines
        val numCheckpoints = 5
        for (i in 0 until numCheckpoints) {
            val stepValue = (maxStep / (numCheckpoints - 1)) * i
            val yCheckpoint = graphHeight - ((stepValue / maxStep) * graphHeight)

            // Draws faint horizontal line
            drawLine(
                color = Color.LightGray,
                start = Offset(yAxisPadding, yCheckpoint),
                end = Offset(size.width, yCheckpoint),
                strokeWidth = 2f
            )

            // Draws rounded step value text
            drawText(
                textMeasurer = textMeasurer,
                text = stepValue.toInt().toString(),
                topLeft = Offset(0f, yCheckpoint - 25f) // -25f = Vertical alignment between text and checkpoint
            )
        }

        // Calculates graph points
        val xPointSpacing = if (steps.size > 1) graphWidth / (steps.size - 1) else graphWidth
        val linePath = Path()
        val fillPath = Path()
        val points = mutableListOf<Offset>()

        steps.forEachIndexed { index, stepValue ->
            val x = yAxisPadding + (index * xPointSpacing)
            val y = graphHeight - ((stepValue / maxStep) * graphHeight)
            points.add(Offset(x, y))
        }

        // Draws smooth graph lines (Cubic Bezier)
        if (points.isNotEmpty()) {
            points.forEachIndexed { index, point ->
                if (index == 0) {
                    linePath.moveTo(point.x, point.y)
                    fillPath.moveTo(point.x, point.y)
                }

                else {
                    val previousPoint = points[index - 1]
                    val controlX = (previousPoint.x + point.x) / 2f

                    linePath.cubicTo(
                        x1 = controlX, y1 = previousPoint.y,
                        x2 = controlX, y2 = point.y,
                        x3 = point.x, y3 = point.y
                    )

                    fillPath.cubicTo(
                        x1 = controlX, y1 = previousPoint.y,
                        x2 = controlX, y2 = point.y,
                        x3 = point.x, y3 = point.y
                    )
                }
            }

            val firstX = points.first().x
            val lastX = points.last().x

            fillPath.lineTo(lastX, graphHeight)
            fillPath.lineTo(firstX, graphHeight)
            fillPath.close()

            // Draws the gradient below the line
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.5f), Color.Transparent),
                    startY = 0f,
                    endY = graphHeight
                )
            )

            // Draws rounded line
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 4.dp.toPx())
            )
        }

        // Draws x-axis texts dynamically shifted to prevent clipping
        daysOfWeek.forEachIndexed { index, day ->
            val x = yAxisPadding + (index * xPointSpacing)

            val textLayoutResult = textMeasurer.measure(day)
            val textWidth = textLayoutResult.size.width

            val textXOffset = when (index) {
                0 -> x
                daysOfWeek.lastIndex -> x - textWidth
                else -> x - (textWidth / 2f)
            }

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(textXOffset, graphHeight + 15f)
            )
        }
    }
}
