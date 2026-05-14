package com.kostas.gohealth.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kostas.gohealth.ui.components.general.ActionButton
import com.kostas.gohealth.ui.components.general.ProgressBar
import com.kostas.gohealth.ui.components.screen.CustomAlertDialog
import com.kostas.gohealth.ui.viewModels.TrackingsViewModel

// Water, calories and exercise screen
@Composable
fun CategoriesScreen(categoryName: String, iconId: Int, progressBarColour: Color, categoryProgress: Int, categoryGoal: Int, metric: String, buttonIconIds: List<Int>?, buttonTexts: List<String>) {
    val trackingsViewModel: TrackingsViewModel = viewModel(factory = TrackingsViewModel.Factory)
    val userTrackingsList by trackingsViewModel.trackings.collectAsState()
    val userTrackings = userTrackingsList.firstOrNull()

    var showCustomAlertDialog by remember { mutableStateOf(false) }

    // Handles which category to update
    val handleAddAmount: (Int) -> Unit = { amount ->
        userTrackings?.let { trackings ->
            val updatedTrackings = when (categoryName) {
                "Water" -> trackings.copy(waterProgress = trackings.waterProgress + amount)
                "Calories" -> trackings.copy(caloriesProgress = trackings.caloriesProgress + amount)
                "Exercise" -> trackings.copy(exerciseProgress = trackings.exerciseProgress + amount)
                else -> throw IllegalStateException("Invalid Input")
            }

            trackingsViewModel.updateUserTrackings(updatedTrackings)
        }
    }

    val handleDeletePrevious: () -> Unit = {
        userTrackings?.let { trackings ->
            val updatedUser = when (categoryName) {
                "Water" -> trackings.copy(waterProgress = trackings.waterProgress.dropLast(1))
                "Calories" -> trackings.copy(caloriesProgress = trackings.caloriesProgress.dropLast(1))
                "Exercise" -> trackings.copy(exerciseProgress = trackings.exerciseProgress.dropLast(1))
                else -> throw IllegalStateException("Invalid Input")
            }
            trackingsViewModel.updateUserTrackings(updatedUser)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterVertically),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = "Category",
                tint = Color.Unspecified,
                modifier = Modifier.size(200.dp)
            )

            Text(
                text = "$categoryProgress / $categoryGoal $metric",
                style = MaterialTheme.typography.titleLarge
            )

            ProgressBar(20.dp, progressBarColour, categoryProgress.toFloat() / categoryGoal)

            if (metric != "reps") {
                Text(
                    text = "Add $metric to reach your daily goal!",
                    textAlign = TextAlign.Center
                )
            }

            else {
                Text(
                    text = "Add $metric of any exercise to reach your daily goal!",
                    textAlign = TextAlign.Center
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    val modifier = Modifier.weight(1f)

                    // Uses regex to get 100 from "+100mL" or "+100"
                    val regex = "(?<=\\+)\\d+".toRegex()
                    ActionButton(modifier, progressBarColour, buttonIconIds?.get(0), buttonTexts[0]) { handleAddAmount(regex.find(buttonTexts[0])?.value?.toIntOrNull() ?: 0) }
                    ActionButton(modifier, progressBarColour, buttonIconIds?.get(1), buttonTexts[1]) { handleAddAmount(regex.find(buttonTexts[1])?.value?.toIntOrNull() ?: 0) }
                    ActionButton(modifier, progressBarColour, buttonIconIds?.get(2), buttonTexts[2]) { handleAddAmount(regex.find(buttonTexts[2])?.value?.toIntOrNull() ?: 0) }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    val modifier = Modifier.weight(1f)
                    ActionButton(modifier, progressBarColour, null, "Custom") { showCustomAlertDialog = true }
                    ActionButton(modifier, Color(0xFFE53935), null, "Undo") { handleDeletePrevious() }
                }
            }
        }

        if (showCustomAlertDialog) {
            CustomAlertDialog(
                metric = metric,
                onDismiss = { showCustomAlertDialog = false },
                onConfirm = { amount ->
                    handleAddAmount(amount)
                }
            )
        }
    }
}
