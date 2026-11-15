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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.work.workDataOf
import com.healthterra.data.entities.TodayTrackings
import com.healthterra.helpers.calculateCaloriesGoal
import com.healthterra.helpers.calculateExerciseGoal
import com.healthterra.helpers.calculateWaterGoal
import com.healthterra.helpers.roundValue
import com.healthterra.services.SyncDailyTrackingsWorker
import com.healthterra.ui.components.general.ActionButton
import com.healthterra.ui.components.general.BulletGraph
import com.healthterra.ui.components.general.ProgressBar
import com.healthterra.ui.components.general.SearchTextField
import com.healthterra.ui.components.screen.CustomButtonDialog
import com.healthterra.ui.components.screen.FoodTable
import com.healthterra.ui.viewModels.CharacteristicsViewModel
import com.healthterra.ui.viewModels.FoodViewModel
import com.healthterra.ui.viewModels.TodayTrackingsViewModel
import java.time.LocalDate
import kotlin.math.roundToInt

// Water, calories and exercise screen
@Composable
fun CategoriesScreen(categoryName: String, iconId: Int, progressBarColour: Color, categoryProgress: Int, categoryGoal: Int, metric: String, buttonIconIds: List<Int>?, buttonTexts: List<String>, fontSize: TextUnit) {
    val characteristicsViewModel: CharacteristicsViewModel = viewModel(factory = CharacteristicsViewModel.Factory)
    val userCharacteristicsList by characteristicsViewModel.characteristics.collectAsState()
    val userCharacteristics = userCharacteristicsList.firstOrNull()

    val todayTrackingsViewModel: TodayTrackingsViewModel = viewModel(factory = TodayTrackingsViewModel.Factory)
    val userTodayTrackingsList by todayTrackingsViewModel.todayTrackings.collectAsState()
    val userTodayTrackings = userTodayTrackingsList.firstOrNull()

    // Gets auto updatable variables for the food table
    val foodViewModel: FoodViewModel = viewModel(factory = FoodViewModel.Factory)
    val searchResults by foodViewModel.searchResults.collectAsState()
    val isLoading by foodViewModel.isLoading.collectAsState()

    var showCustomAlertDialog by rememberSaveable { mutableStateOf(false) }

    // Uses regex to get 100 from "+100mL" or "+100"
    val regex = "(?<=\\+)\\d+".toRegex()

    val context = LocalContext.current


    // Syncs trackings to Firestore, if a goal was just reached or lost, uses snapshots to handle multi-day offline syncing, needs network
    fun checkGoalStatusAndSync(oldCategoryValue: Int, newCategoryValue: Int, updatedTodayTrackings: TodayTrackings) {
        // Uses minimum value for the calories range goal
        val categoryGoalFix = if (categoryName == "Calories") {
            roundValue((categoryGoal * 0.9).roundToInt())
        }

        else {
            categoryGoal
        }

        val justReachedGoal = categoryGoalFix in (oldCategoryValue + 1)..newCategoryValue
        val justLostGoal = categoryGoalFix in (newCategoryValue + 1)..oldCategoryValue
        val goalStatusChanged = justReachedGoal || justLostGoal
        if (goalStatusChanged && userCharacteristics != null && userTodayTrackings != null) {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val syncRequest = OneTimeWorkRequestBuilder<SyncDailyTrackingsWorker>()
                .setConstraints(constraints)
                .setInitialDelay(3, java.util.concurrent.TimeUnit.SECONDS) // To avoid many writes, if the user is spamming the UI component
                .setInputData(workDataOf(
                    "snapshot_water_progress" to updatedTodayTrackings.waterProgress.sum(),
                    "snapshot_calories_progress" to updatedTodayTrackings.caloriesProgress.sum(),
                    "snapshot_exercise_progress" to updatedTodayTrackings.exerciseProgress.sum(),
                    "snapshot_water_goal" to calculateWaterGoal(userCharacteristics),
                    "snapshot_calories_goal" to calculateCaloriesGoal(userCharacteristics),
                    "snapshot_exercise_goal" to calculateExerciseGoal(userCharacteristics),
                    "snapshot_date" to LocalDate.now().toString()
                ))
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "SyncDailyTrackingsWorker_${LocalDate.now()}", // Shared id with DailyMaintenance because they use the same worker
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
        }
    }

    // Handles which category to update
    fun handleAddAmount(amount: Int) {
        userTodayTrackings?.let { todayTrackings ->
            val oldCategoryValue = when (categoryName) {
                "Water" -> todayTrackings.waterProgress.sum()
                "Calories" -> todayTrackings.caloriesProgress.sum()
                "Exercise" -> todayTrackings.exerciseProgress.sum()
                else -> 0
            }

            val updatedTodayTrackings = when (categoryName) {
                "Water" -> todayTrackings.copy(waterProgress = todayTrackings.waterProgress + amount)
                "Calories" -> todayTrackings.copy(caloriesProgress = todayTrackings.caloriesProgress + amount)
                "Exercise" -> todayTrackings.copy(exerciseProgress = todayTrackings.exerciseProgress + amount)
                else -> throw IllegalStateException("Invalid Input")
            }

            todayTrackingsViewModel.updateUserTodayTrackings(updatedTodayTrackings, userCharacteristics)

            val newCategoryValue = oldCategoryValue + amount
            checkGoalStatusAndSync(oldCategoryValue, newCategoryValue, updatedTodayTrackings) // Uses updatedTrackings instead of userTrackings to avoid synchronization bugs
        }
    }

    fun handleDeletePrevious() {
        userTodayTrackings?.let { todayTrackings ->
            val oldCategoryValue = when (categoryName) {
                "Water" -> todayTrackings.waterProgress.sum()
                "Calories" -> todayTrackings.caloriesProgress.sum()
                "Exercise" -> todayTrackings.exerciseProgress.sum()
                else -> 0
            }

            val updatedTodayTrackings = when (categoryName) {
                "Water" -> todayTrackings.copy(waterProgress = todayTrackings.waterProgress.dropLast(1))
                "Calories" -> todayTrackings.copy(caloriesProgress = todayTrackings.caloriesProgress.dropLast(1))
                "Exercise" -> todayTrackings.copy(exerciseProgress = todayTrackings.exerciseProgress.dropLast(1))
                else -> throw IllegalStateException("Invalid Input")
            }

            todayTrackingsViewModel.updateUserTodayTrackings(updatedTodayTrackings, userCharacteristics)

            val newCategoryValue = when (categoryName) {
                "Water" -> updatedTodayTrackings.waterProgress.sum()
                "Calories" -> updatedTodayTrackings.caloriesProgress.sum()
                "Exercise" -> updatedTodayTrackings.exerciseProgress.sum()
                else -> 0
            }

            checkGoalStatusAndSync(oldCategoryValue, newCategoryValue, updatedTodayTrackings)
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

        val isCalories = categoryName == "Calories"

        // The calories category uses a +-10% range, the others just have a minimum goal
        val minValue = if (isCalories) roundValue((categoryGoal * 0.9).roundToInt()) else categoryGoal
        val maxValue = if (isCalories) roundValue((categoryGoal * 1.1).roundToInt()) else null

        val goalReached = if (maxValue != null) categoryProgress in minValue..maxValue else categoryProgress >= minValue
        val textColour = if (goalReached) Color(0xFF4CAF50) else Color(0xFFE53935)
        val goalText = if (maxValue != null) "$minValue-$maxValue" else "$categoryGoal"

        val annotatedText = buildAnnotatedString {
            withStyle(style = SpanStyle(color = textColour)) {
                append(categoryProgress.toString())
            }

            append(" / $goalText $metric")
        }

        Text(
            text = annotatedText,
            style = MaterialTheme.typography.titleLarge
        )

        if (isCalories) {
            BulletGraph(categoryProgress, minValue, maxValue!!, progressBarColour)
        }

        else {
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

                var searchQuery by rememberSaveable { mutableStateOf("") }
                var hasSearched by rememberSaveable { mutableStateOf(false) }

                // To restore the food table if the app was closed for a long time
                LaunchedEffect(Unit) {
                    if (hasSearched && searchQuery.isNotEmpty() && searchResults.isEmpty()) {
                        foodViewModel.searchFood(searchQuery)
                    }
                }

                SearchTextField(
                    searchQuery,
                    Modifier.fillMaxWidth(),
                    "Enter a food item...",
                    progressBarColour,
                    3,

                    onQueryChange = {
                        searchQuery = it
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
