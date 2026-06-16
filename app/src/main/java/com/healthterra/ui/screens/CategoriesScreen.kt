package com.healthterra.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.healthterra.helpers.roundGoal
import com.healthterra.services.SyncDailyTrackingsWorker
import com.healthterra.ui.components.general.ActionButton
import com.healthterra.ui.components.general.BulletGraph
import com.healthterra.ui.components.general.ProgressBar
import com.healthterra.ui.components.general.SearchTextField
import com.healthterra.ui.components.screen.CustomButtonDialog
import com.healthterra.ui.components.screen.FoodTable
import com.healthterra.ui.viewModels.CharacteristicsViewModel
import com.healthterra.ui.viewModels.FoodViewModel
import com.healthterra.ui.viewModels.TrackingsViewModel
import kotlin.math.roundToInt

// Water, calories and exercise screen
@Composable
fun CategoriesScreen(categoryName: String, iconId: Int, progressBarColour: Color, categoryProgress: Int, categoryGoal: Int, metric: String, buttonIconIds: List<Int>?, buttonTexts: List<String>, fontSize: TextUnit) {
    val characteristicsViewModel: CharacteristicsViewModel = viewModel(factory = CharacteristicsViewModel.Factory)
    val userCharacteristics by characteristicsViewModel.characteristics.collectAsState()
    val characteristics = userCharacteristics.firstOrNull()

    val trackingsViewModel: TrackingsViewModel = viewModel(factory = TrackingsViewModel.Factory)
    val userTrackingsList by trackingsViewModel.trackings.collectAsState()
    val userTrackings = userTrackingsList.firstOrNull()

    // Gets auto updatable variables for the food table
    val foodViewModel: FoodViewModel = viewModel(factory = FoodViewModel.Factory)
    val searchResults by foodViewModel.searchResults.collectAsState()
    val isLoading by foodViewModel.isLoading.collectAsState()

    var showCustomAlertDialog by rememberSaveable { mutableStateOf(false) }

    // Uses regex to get 100 from "+100mL" or "+100"
    val regex = "(?<=\\+)\\d+".toRegex()

    val context = LocalContext.current

    // Handles which category to update
    val handleAddAmount: (Int) -> Unit = { amount ->
        userTrackings?.let { trackings ->
            val oldTrackings = when (categoryName) {
                "Water" -> trackings.waterProgress.sum()
                "Calories" -> trackings.caloriesProgress.sum()
                "Exercise" -> trackings.exerciseProgress.sum()
                else -> 0
            }

            val updatedTrackings = when (categoryName) {
                "Water" -> trackings.copy(waterProgress = trackings.waterProgress + amount)
                "Calories" -> trackings.copy(caloriesProgress = trackings.caloriesProgress + amount)
                "Exercise" -> trackings.copy(exerciseProgress = trackings.exerciseProgress + amount)
                else -> throw IllegalStateException("Invalid Input")
            }

            trackingsViewModel.updateUserTrackings(updatedTrackings)

            // Syncs trackings to Firestore, if a goal was just reached, needs network
            val newTrackings = oldTrackings + amount

            // Uses minimum value for the calories range goal
            val categoryGoalFix = if (categoryName == "Calories") {
                roundGoal((categoryGoal - categoryGoal * 0.1).roundToInt())
            }

            else {
                categoryGoal
            }

            val justReachedGoal = categoryGoalFix in (oldTrackings + 1)..newTrackings
            if (justReachedGoal && characteristics != null) {
                val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                val syncRequest = OneTimeWorkRequestBuilder<SyncDailyTrackingsWorker>()
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "SyncDailyTrackingsWorker",
                    ExistingWorkPolicy.REPLACE,
                    syncRequest
                )
            }
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


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // Adds bottom padding equal to the height of the keyboard, so the keyboard doesn't hide the text field
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = "Category",
            tint = Color.Unspecified,
            modifier = Modifier.size(200.dp)
        )

        if (categoryName == "Calories") {
            // It does +-10% of the goal because calories use a range
            val minValue = roundGoal((categoryGoal - categoryGoal * 0.1).roundToInt())
            val maxValue = roundGoal((categoryGoal + categoryGoal * 0.1).roundToInt())

            val textColour = if (categoryProgress in minValue..maxValue) Color(0xFF4CAF50) else Color(0xFFE53935)
            val annotatedText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = textColour)) {
                    append(categoryProgress.toString())
                }

                append(" / $minValue-$maxValue $metric")
            }

            Text(
                text = annotatedText,
                style = MaterialTheme.typography.titleLarge
            )

            BulletGraph(categoryProgress, minValue, maxValue, progressBarColour)
        }

        else {
            Text(
                text = "$categoryProgress / $categoryGoal $metric",
                style = MaterialTheme.typography.titleLarge
            )

            ProgressBar(20.dp, progressBarColour, categoryProgress.toFloat() / categoryGoal)
        }

        if (categoryName == "Exercise") {
            Text(
                text = "Add $metric of any exercise to reach your daily goal!",
                textAlign = TextAlign.Center
            )
        }

        else {
            Text(
                text = "Add $metric to reach your daily goal!",
                textAlign = TextAlign.Center
            )
        }

        if (categoryName == "Calories") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    val modifier = Modifier.weight(1f)

                    ActionButton(modifier = modifier, colour = progressBarColour, text = "Custom", fontSize = 16.sp) { showCustomAlertDialog = true }
                    ActionButton(modifier = modifier, colour = Color(0xFFE53935), text = "Undo", fontSize = 16.sp) { handleDeletePrevious() }
                }

                var hasSearched by rememberSaveable { mutableStateOf(false) }
                SearchTextField(
                    Modifier.fillMaxWidth(),
                    "Enter a food item...",
                    progressBarColour,
                    3,

                    onInputChange = {
                        hasSearched = false
                    },

                    onSearch = { input ->
                        foodViewModel.searchFood(input)
                        hasSearched = true
                    }
                )

                if (hasSearched && !isLoading) {
                    FoodTable(rows = searchResults, onFoodSelected = { calories -> handleAddAmount(calories) })
                }
            }
        }

        else {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val modifier = Modifier.weight(1f)

                    for ((i, text) in buttonTexts.withIndex()) {
                        ActionButton(modifier, progressBarColour, buttonIconIds?.get(i), text, fontSize) { handleAddAmount(regex.find(buttonTexts[i])?.value?.toIntOrNull() ?: 0) }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    val modifier = Modifier.weight(1f)

                    ActionButton(modifier = modifier, colour = progressBarColour, text = "Custom", fontSize = 16.sp) { showCustomAlertDialog = true }
                    ActionButton(modifier = modifier, colour = Color(0xFFE53935), text = "Undo", fontSize = 16.sp) { handleDeletePrevious() }
                }
            }
        }
    }

    if (showCustomAlertDialog) {
        CustomButtonDialog (
            metric = metric,
            onDismiss = { showCustomAlertDialog = false },
            onConfirm = { amount ->
                handleAddAmount(amount)
            }
        )
    }
}
