package com.healthterra.ui.components.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.healthterra.data.entities.Characteristics
import com.healthterra.data.entities.Settings
import com.healthterra.helpers.formatTimeframe
import com.healthterra.helpers.isCaloriesPlanExtreme
import com.healthterra.ui.components.general.RadioButtonGroup
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightGoalSelector(userCharacteristics: Characteristics, userSettings: Settings, onGoalChange: (goal: String, kg: Int, days: Int) -> Unit) {
    var selectedWeightGoal by remember(userCharacteristics.weightGoal) {
        mutableStateOf(userCharacteristics.weightGoal)
    }

    // The kg slider requires positive floats, kg can be negative if the lose option was selected and zero if the loss or gain option was
    // selected after the maintain option. If it's the first time the app is run, kg will go to 1 from 0, but it doesn't matter because the
    // database won't be updated until the save changes button is pressed
    var selectedKgGoal by remember(userCharacteristics.kgGoal) {
        mutableFloatStateOf(abs(userCharacteristics.kgGoal).toFloat().coerceAtLeast(1f))
    }

    // Maps the 14 slider positions (2 for the week options and 12 for the month options) to specific day intervals
    val timeframeOptions = remember {
        val today = LocalDate.now()
        val options = mutableListOf(14, 21)

        for (months in 1..12) {
            val futureDate = today.plusMonths(months.toLong())
            val exactDays = ChronoUnit.DAYS.between(today, futureDate).toInt()
            options.add(exactDays)
        }

        options
    }

    // Same as kg, but it initializes the slider to the closest matching index based on the user's previously saved days goal
    var selectedDaysIndex by remember(userCharacteristics.daysGoal) {
        val initialDays = userCharacteristics.daysGoal.coerceAtLeast(14)
        val closestIndex = timeframeOptions.indices.minBy { abs(timeframeOptions[it] - initialDays) }
        mutableFloatStateOf(closestIndex.toFloat())
    }

    val selectedDaysGoal = timeframeOptions[selectedDaysIndex.roundToInt()]

    // Zero out values if maintain is selected, give a negative value if lose is selected, this gets called every time a new radio button
    // option is selected, or a slider is moved, because their variables are in a remember/mutableStateOf
    val savedKgGoal = if (selectedWeightGoal == "Maintain") 0 else if (selectedWeightGoal == "Lose") -selectedKgGoal.roundToInt() else selectedKgGoal.roundToInt()
    val savedDaysGoal = if (selectedWeightGoal == "Maintain") 0 else selectedDaysGoal

    // Determines if the local UI state is different from the saved database state
    val hasChanges = selectedWeightGoal != userCharacteristics.weightGoal || savedKgGoal != userCharacteristics.kgGoal || savedDaysGoal != userCharacteristics.daysGoal

    val customSliderColors = SliderDefaults.colors(
        activeTickColor = Color.Transparent,
        inactiveTickColor = Color.Transparent
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),

        modifier = Modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(20.dp)
    ) {
        RadioButtonGroup(
            options = listOf("Lose", "Maintain", "Gain"),
            selectedOption = selectedWeightGoal,
            showBorder = false
        ) { optionSelected ->
            // UI change, if we go from maintain to lose/gain the screen remembers the selectedKgGoal and selectedDaysGoal values, because
            // they are never changed here
            selectedWeightGoal = optionSelected
        }

        // Slider menu, only visible on lose or gain
        if (selectedWeightGoal == "Lose" || selectedWeightGoal == "Gain") {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Amount to ${selectedWeightGoal.lowercase()}")
                    Text(text = "${selectedKgGoal.roundToInt()} kg")
                }

                Slider(
                    value = selectedKgGoal,
                    onValueChange = {
                        selectedKgGoal = it // UI change
                    },

                    colors = customSliderColors,

                    track = { sliderState ->
                        SliderDefaults.Track(
                            sliderState = sliderState,
                            colors = customSliderColors,
                            drawStopIndicator = null
                        )
                    },

                    valueRange = 1f..50f, // From 1kg to 50kg
                    steps = 48
                )
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Timeframe")
                    Text(text = formatTimeframe(selectedDaysGoal))
                }

                Slider(
                    value = selectedDaysIndex,
                    onValueChange = {
                        selectedDaysIndex = it
                    },

                    colors = customSliderColors,

                    track = { sliderState ->
                        SliderDefaults.Track(
                            sliderState = sliderState,
                            colors = customSliderColors,
                            drawStopIndicator = null
                        )
                    },

                    valueRange = 0f..13f, // 2 week options and 12 month options = 14
                    steps = 12
                )
            }
        }

        Button(
            onClick = {
                onGoalChange(selectedWeightGoal, savedKgGoal, savedDaysGoal)
            },

            enabled = hasChanges,
        ) {
            Text("Save Changes")
        }

        if (!hasChanges) { // The messages only show when the save changes button has been pressed
            val showError = isCaloriesPlanExtreme(userCharacteristics)
            if (showError) {
                Text(
                    text = "To ensure safe calorie intake, your daily goal has been set to the recommended amount!",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFE53935),
                    textAlign = TextAlign.Center
                )
            }

            else {
                if (userCharacteristics.kgGoal != 0 && userCharacteristics.daysGoal != 0) { // If the user has set a calories lose/gain goal
                    // Gets initialWeightGoalDate and adds the timeframe to the goal to it
                    val initialWeightGoalDate = userSettings.initialWeightGoalDate
                    val weightGoalDate = LocalDate.parse(initialWeightGoalDate).plusDays(userCharacteristics.daysGoal.toLong())
                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yy")
                    val formattedWeightGoalDate= weightGoalDate.format(formatter)

                    Text(
                        text =  "You're all set! The weight goal plan will end on $formattedWeightGoalDate.",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    // Updates initialWeightGoalDate thus the weight goal text as well, when the timeframe is hit
                    LaunchedEffect(weightGoalDate) {
                        if (LocalDate.now() >= weightGoalDate) {
                            onGoalChange(selectedWeightGoal, selectedKgGoal.roundToInt(), selectedDaysGoal)
                        }
                    }
                }
            }
        }
    }
}
