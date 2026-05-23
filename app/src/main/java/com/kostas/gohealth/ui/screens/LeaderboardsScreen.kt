package com.kostas.gohealth.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.kostas.gohealth.R
import com.kostas.gohealth.helpers.getCategoryTopUsers
import com.kostas.gohealth.helpers.getCurrentUser
import com.kostas.gohealth.ui.components.screen.LeaderboardBox
import com.kostas.gohealth.ui.components.screen.LeaderboardDialog

@Composable
fun LeaderboardsScreen() {
    val waterLeaderboards by getCategoryTopUsers("waterGoalsCompleted", 50)
    val caloriesLeaderboards by getCategoryTopUsers("caloriesGoalsCompleted", 50)
    val exerciseLeaderboards by getCategoryTopUsers("exerciseGoalsCompleted", 50)
    val stepsLeaderboards by getCategoryTopUsers("stepsGoalsCompleted", 50)
    val totalStepsLeaderboards by getCategoryTopUsers("totalSteps", 50)

    val topWaterUser = waterLeaderboards.firstOrNull()
    val topCaloriesUser = caloriesLeaderboards.firstOrNull()
    val topExerciseUser = exerciseLeaderboards.firstOrNull()
    val topStepsUser = stepsLeaderboards.firstOrNull()
    val topTotalStepsUser = totalStepsLeaderboards.firstOrNull()

    val currentUserId = Firebase.auth.currentUser?.uid
    val currentUser by getCurrentUser(currentUserId)

    // Tracks which LeaderboardDialog is currently open
    var selectedLeaderboardDialog by remember {
        mutableStateOf<String?>(null)
    }

    val avatarMap = mapOf(
        "bear" to R.drawable.bear,
        "cat" to R.drawable.cat,
        "dinosaur" to R.drawable.dinosaur,
        "dog" to R.drawable.dog,
        "dolphin" to R.drawable.dolphin,
        "duck" to R.drawable.duck,
        "eagle" to R.drawable.eagle,
        "elephant" to R.drawable.elephant,
        "horse" to R.drawable.horse,
        "lion" to R.drawable.lion,
        "penguin" to R.drawable.penguin,
        "sheep" to R.drawable.sheep
    )

    if (topWaterUser != null && topCaloriesUser != null && topExerciseUser != null && topStepsUser != null && topTotalStepsUser != null) { // Loading Screen
        // Draws the screen
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 24.dp)
            ) {
                Text(
                    text = "Daily Goals Completed",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37)
                )

                Spacer(modifier = Modifier.padding(4.dp))

                topWaterUser.let { user ->
                    // Checks if the current user is the top user, if he is, make his score null to know not to draw a specific component
                    // Also makes the LeaderboardBox clickable, when clicked the LeaderboardDialog shows
                    val currentUserScore = if (currentUserId == user.userId) null else (currentUser?.waterGoalsCompleted?.toString() ?: "0")
                    LeaderboardBox(Modifier.clickable { selectedLeaderboardDialog = "waterGoalsCompleted" }, avatarMap.getValue(user.profilePictureString), user.username, R.drawable.water, "Water", user.waterGoalsCompleted.toString(), currentUserScore)
                }

                topCaloriesUser.let { user ->
                    val currentUserScore = if (currentUserId == user.userId) null else (currentUser?.caloriesGoalsCompleted?.toString() ?: "0")
                    LeaderboardBox(Modifier.clickable { selectedLeaderboardDialog = "caloriesGoalsCompleted" }, avatarMap.getValue(user.profilePictureString), user.username, R.drawable.calories, "Calories", user.caloriesGoalsCompleted.toString(), currentUserScore)
                }

                topExerciseUser.let { user ->
                    val currentUserScore = if (currentUserId == user.userId) null else (currentUser?.exerciseGoalsCompleted?.toString() ?: "0")
                    LeaderboardBox(Modifier.clickable { selectedLeaderboardDialog = "exerciseGoalsCompleted" }, avatarMap.getValue(user.profilePictureString), user.username, R.drawable.exercise, "Exercise", user.exerciseGoalsCompleted.toString(), currentUserScore)
                }

                topStepsUser.let { user ->
                    val currentUserScore = if (currentUserId == user.userId) null else (currentUser?.stepsGoalsCompleted?.toString() ?: "0")
                    LeaderboardBox(Modifier.clickable { selectedLeaderboardDialog = "stepsGoalsCompleted" }, avatarMap.getValue(user.profilePictureString), user.username, R.drawable.steps, "Steps", user.stepsGoalsCompleted.toString(), currentUserScore)
                }

                Spacer(modifier = Modifier.padding(12.dp))

                Text(
                    text = "Total Steps",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37)
                )

                Spacer(modifier = Modifier.padding(4.dp))

                topTotalStepsUser.let { user ->
                    val currentUserScore = if (currentUserId == user.userId) null else (currentUser?.totalSteps?.toString() ?: "0")
                    LeaderboardBox(Modifier.clickable { selectedLeaderboardDialog = "totalSteps" }, avatarMap.getValue(user.profilePictureString), user.username, R.drawable.steps, "Steps", user.totalSteps.toString(), currentUserScore)
                }
            }
        }

        // Show the LeaderboardDialog, if a category is clicked
        selectedLeaderboardDialog?.let { categoryString ->
            val categoryLeaderboards = when (categoryString) {
                "waterGoalsCompleted" -> waterLeaderboards
                "caloriesGoalsCompleted" -> caloriesLeaderboards
                "exerciseGoalsCompleted" -> exerciseLeaderboards
                "stepsGoalsCompleted" -> stepsLeaderboards
                "totalSteps" -> totalStepsLeaderboards
                else -> emptyList()
            }

            LeaderboardDialog(categoryString, categoryLeaderboards, avatarMap, onDismiss = { selectedLeaderboardDialog = null })
        }
    }
}
