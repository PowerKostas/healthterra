package com.healthterra.ui.components.general

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.healthterra.data.entities.DailyTrackings
import com.healthterra.helpers.colorCoding
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun ContributionCalendar(currentYearMonth: YearMonth, oldestYearMonth: YearMonth, contributionsMap: Map<String, Int>, maxGoalsMetCount: Int, selectedSquareDate: String?, selectedSquareDetails: DailyTrackings?, onYearMonthChange: (YearMonth) -> Unit, onSquareClick: (String) -> Unit, onPopupDismiss: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // The component shows the day corresponding to every column, because every month starts with a different day, the order of the days needs
    // to shift. Example: If the month starts on a Friday (daysIndex 4), this drops the first 4 days and appends them to the end
    val dynamicDaysOfWeek = remember(currentYearMonth.atDay(1)) {
        val baseDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val daysIndex = currentYearMonth.atDay(1).dayOfWeek.value - 1
        baseDays.drop(daysIndex) + baseDays.take(daysIndex)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Dynamic days of week header
            Row {
                dynamicDaysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Monthly contribution calendar, if a goal was met that day, the square will be green, otherwise it will be gray. Because only
            // February has 4 rows instead of 5, it always renders 35 slots (5 rows * 7 columns) to keep the height consistent
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (row in 0 until 5) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (col in 0 until 7) {
                            val dayIndex = (row * 7) + col

                            if (dayIndex < currentYearMonth.lengthOfMonth()) {
                                val dateString = currentYearMonth.atDay(dayIndex + 1).format(formatter)
                                val goalsMetCount = contributionsMap[dateString] ?: 0
                                val squareColor = colorCoding(goalsMetCount, maxGoalsMetCount)

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(squareColor)
                                        .clickable { onSquareClick(dateString) }
                                ) {
                                    Text(
                                        text = (dayIndex + 1).toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )

                                    // Clickable popup
                                    if (dateString == selectedSquareDate) {
                                        Popup(
                                            alignment = Alignment.Center, // Popup opens directly over the square
                                            onDismissRequest = onPopupDismiss,

                                            // Because focusable is false, clicks outside the component go to onDismiss and clicks inside the
                                            // component go to onSquareClick
                                            properties = PopupProperties(focusable = false)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                shadowElevation = 8.dp,
                                                color = MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                if (selectedSquareDetails != null) {
                                                    Column(
                                                        modifier = Modifier.padding(12.dp),
                                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = dateString,
                                                            fontWeight = FontWeight.Bold,
                                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 14.sp),
                                                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 2.dp)
                                                        )

                                                        Text("Water: ${selectedSquareDetails.waterProgress} ml", style = MaterialTheme.typography.labelSmall)
                                                        Text("Calories: ${selectedSquareDetails.caloriesProgress} kcal", style = MaterialTheme.typography.labelSmall)
                                                        Text("Exercise: ${selectedSquareDetails.exerciseProgress} reps", style = MaterialTheme.typography.labelSmall)
                                                        Text("Steps: ${selectedSquareDetails.stepsProgress} steps", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }

                                                else {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        Text(
                                                            text = dateString,
                                                            fontWeight = FontWeight.Bold,
                                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 14.sp),
                                                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 2.dp)
                                                        )

                                                        Text("No data recorded.", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Invisible filler square to maintain the 5th row's height
                            else {
                                Box(modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Slider row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onYearMonthChange(currentYearMonth.minusMonths(1)) },
                enabled = currentYearMonth > oldestYearMonth // Disables going in past months that the user has no data
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
            }

            Text(
                text = currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.bodyLarge
            )

            IconButton(
                onClick = { onYearMonthChange(currentYearMonth.plusMonths(1)) },
                enabled = currentYearMonth < YearMonth.now() // Disables going in the future
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
            }
        }
    }
}
