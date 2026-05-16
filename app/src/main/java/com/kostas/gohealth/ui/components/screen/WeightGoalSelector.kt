package com.kostas.gohealth.ui.components.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kostas.gohealth.helpers.isCaloriesPlanExtreme
import com.kostas.gohealth.ui.components.general.RadioButtonGroup
import com.kostas.gohealth.ui.viewModels.CharacteristicsViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightGoalSelector(initialWeightGoal: String, initialKgGoal: Int, initialDaysGoal: Int, onGoalChange: (goal: String, kg: Int, days: Int) -> Unit) {
    val characteristicsViewModel: CharacteristicsViewModel = viewModel(factory = CharacteristicsViewModel.Factory)
    val userCharacteristicsList by characteristicsViewModel.characteristics.collectAsState()
    val userCharacteristics = userCharacteristicsList.firstOrNull()

    var selectedWeightGoal by remember { mutableStateOf(initialWeightGoal)}

    // The sliders require positive floats, kg can be negative if the lose option was selected and zero if the maintain option was selected
    // and then the loss or gain option. Days can be zero if the maintain option was selected and then the loss or gain option. If it's the
    // first time the app is run, kg and days will go to 1 and 14 from 0 and 0, but it doesn't matter because the database won't be updated
    // until a new radio button group option is selected
    var selectedKgGoal by remember { mutableFloatStateOf(abs(initialKgGoal).toFloat().coerceAtLeast(1f)) }
    var selectedDaysGoal by remember { mutableFloatStateOf(initialDaysGoal.toFloat().coerceAtLeast(14f)) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),

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
            //UI change, if we go from maintain to lose/gain the screen remembers the selectedKgGoal and selectedDaysGoal values, because
            // they are never changed here
            selectedWeightGoal = optionSelected

            // Database change, zero out values if maintain is selected, give a negative value if lose is selected
            val savedKgGoal = if (optionSelected == "Maintain") 0 else if (optionSelected == "Lose") -selectedKgGoal.roundToInt() else selectedKgGoal.roundToInt()
            val savedDaysGoal = if (optionSelected == "Maintain") 0 else selectedDaysGoal.roundToInt()
            onGoalChange(optionSelected, savedKgGoal, savedDaysGoal)
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

                        // Database change, give a negative value if lose is selected
                        val savedKgGoal = if (selectedWeightGoal == "Lose") -selectedKgGoal.roundToInt() else selectedKgGoal.roundToInt()
                        onGoalChange(selectedWeightGoal, savedKgGoal, selectedDaysGoal.roundToInt())
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

                        val savedKgGoal = if (selectedWeightGoal == "Lose") -selectedKgGoal.roundToInt() else selectedKgGoal.roundToInt()
                        onGoalChange(selectedWeightGoal, savedKgGoal, selectedDaysGoal.roundToInt())
                    },

                    valueRange = 14f..365f, // From 1 week to 1 year
                )
            }
        }

        val showError = isCaloriesPlanExtreme(userCharacteristics)
        if (showError) {
            Text(
                text = "To ensure safe calorie intake, your daily goal has been set to the recommended amount!",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
    }
}
