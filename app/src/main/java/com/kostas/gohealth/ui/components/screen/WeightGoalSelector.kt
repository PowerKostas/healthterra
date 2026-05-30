package com.kostas.gohealth.ui.components.screen

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
import com.kostas.gohealth.data.entities.Characteristics
import com.kostas.gohealth.data.entities.Settings
import com.kostas.gohealth.helpers.isCaloriesPlanExtreme
import com.kostas.gohealth.ui.components.general.RadioButtonGroup
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightGoalSelector(userCharacteristics: Characteristics, userSettings: Settings, onGoalChange: (goal: String, kg: Int, days: Int) -> Unit) {
    var selectedWeightGoal by remember { mutableStateOf(userCharacteristics.weightGoal)}

    // The sliders require positive floats, kg can be negative if the lose option was selected and zero if the maintain option was selected
    // and then the loss or gain option. Days can be zero if the maintain option was selected and then the loss or gain option. If it's the
    // first time the app is run, kg and days will go to 1 and 14 from 0 and 0, but it doesn't matter because the database won't be updated
    // until the save changes button is pressed
    var selectedKgGoal by remember { mutableFloatStateOf(abs(userCharacteristics.kgGoal).toFloat().coerceAtLeast(1f)) }
    var selectedDaysGoal by remember { mutableFloatStateOf(userCharacteristics.daysGoal.toFloat().coerceAtLeast(14f)) }

    // Zero out values if maintain is selected, give a negative value if lose is selected, this gets called every time a new radio button
    // option is selected, or a slider is moved, because their variables are in a remember/mutableStateOf
    val savedKgGoal = if (selectedWeightGoal == "Maintain") 0 else if (selectedWeightGoal == "Lose") -selectedKgGoal.roundToInt() else selectedKgGoal.roundToInt()
    val savedDaysGoal = if (selectedWeightGoal == "Maintain") 0 else selectedDaysGoal.roundToInt()

    // Determines if the local UI state is different from the saved database state
    val hasChanges = selectedWeightGoal != userCharacteristics.weightGoal || savedKgGoal != userCharacteristics.kgGoal || savedDaysGoal != userCharacteristics.daysGoal

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
            listOf("Lose", "Maintain", "Gain"),
            selectedWeightGoal
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

                    // Removes annoying dot
                    track = { sliderState ->
                        SliderDefaults.Track(sliderState = sliderState, drawStopIndicator = null)
                    },

                    valueRange = 1f..50f, // From 1kg to 50kg
                )
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Timeframe")
                    Text(text = "${selectedDaysGoal.roundToInt()} days")
                }

                Slider(
                    value = selectedDaysGoal,
                    onValueChange = {
                        selectedDaysGoal = it
                    },

                    track = { sliderState ->
                        SliderDefaults.Track(sliderState = sliderState, drawStopIndicator = null)
                    },

                    valueRange = 14f..365f, // From 1 week to 1 year
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
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
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
                        color = Color(0xFF4CAF50),
                        textAlign = TextAlign.Center,
                    )

                    // Updates initialWeightGoalDate thus the weight goal text as well, when the timeframe is hit
                    LaunchedEffect(weightGoalDate) {
                        if (LocalDate.now() >= weightGoalDate) {
                            onGoalChange(selectedWeightGoal, selectedKgGoal.roundToInt(), selectedDaysGoal.roundToInt())
                        }
                    }
                }
            }
        }
    }
}
