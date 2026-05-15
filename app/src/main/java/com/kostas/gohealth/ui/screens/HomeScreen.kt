package com.kostas.gohealth.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kostas.gohealth.R
import com.kostas.gohealth.helpers.calculateCaloriesGoal
import com.kostas.gohealth.helpers.calculateExerciseGoal
import com.kostas.gohealth.helpers.calculateStepsGoal
import com.kostas.gohealth.helpers.calculateWaterGoal
import com.kostas.gohealth.ui.components.screen.ProgressBox
import com.kostas.gohealth.ui.viewModels.CharacteristicsViewModel
import com.kostas.gohealth.ui.viewModels.TrackingsViewModel

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    // Gets values from the database
    val trackingsViewModel = viewModel<TrackingsViewModel>(factory = TrackingsViewModel.Factory)
    val userTrackingsList by trackingsViewModel.trackings.collectAsState()
    val userTrackings = userTrackingsList.firstOrNull()

    val characteristicsViewModel = viewModel<CharacteristicsViewModel>(factory = CharacteristicsViewModel.Factory)
    val userCharacteristicsList by characteristicsViewModel.characteristics.collectAsState()
    val userCharacteristics = userCharacteristicsList.firstOrNull()

    // Waits for the database to load
    if (userTrackings == null || userCharacteristics == null) {
        return
    }

    val waterProgressSum = userTrackings.waterProgress.sum()
    val caloriesProgressSum = userTrackings.caloriesProgress.sum()
    val exerciseProgressSum = userTrackings.exerciseProgress.sum()
    val stepsProgress = userTrackings.stepsProgress

    val waterGoal = calculateWaterGoal(userCharacteristics)
    val caloriesGoal = calculateCaloriesGoal(userCharacteristics)
    val exerciseGoal = calculateExerciseGoal(userCharacteristics)
    val stepsGoal = calculateStepsGoal(userCharacteristics)

    // Draws the screen
    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface)

        Column(
            verticalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterVertically),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp, 32.dp, 16.dp, 32.dp)
        ) {
            // Warning text, if the user hasn't filled all the characteristics in his profile
            if (userCharacteristics.gender == null || userCharacteristics.age == null || userCharacteristics.height == null || userCharacteristics.weight == null || userCharacteristics.activityLevel == null || userCharacteristics.weightGoal == null) {
                Text(
                    text = "Complete your profile for more personalized results.",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            ProgressBox(R.drawable.water, "Water", Color(0xFF2196F3), waterProgressSum, waterGoal, onClick = { onNavigate("Water") })
            ProgressBox(R.drawable.calories, "Calories", Color(0xFF8B4513), caloriesProgressSum, caloriesGoal, onClick = { onNavigate("Calories") })
            ProgressBox(R.drawable.exercise, "Exercise", Color.Black, exerciseProgressSum, exerciseGoal, onClick = { onNavigate("Exercise") })
            ProgressBox(R.drawable.steps, "Steps", Color(0xFFE0AC69), stepsProgress, stepsGoal, onClick = { onNavigate("Steps") })
        }
    }
}
