package com.kostas.gohealth.ui.components.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kostas.gohealth.ui.screens.getCategoryTopUsers

@Composable
fun LeaderboardDialog(categoryString: String, avatarMap: Map<String, Int>, onDismiss: () -> Unit) {
    val fullLeaderboard by getCategoryTopUsers(categoryString, 100)
    val testFullLeaderboard = fullLeaderboard + fullLeaderboard + fullLeaderboard + fullLeaderboard

    val correctCategoryString = when (categoryString) {
        "waterGoalsCompleted" -> "Daily Water Goals Completed"
        "caloriesGoalsCompleted" -> " Daily Calories Goals Completed"
        "exerciseGoalsCompleted" -> "Daily Exercise Goals Completed"
        "stepsGoalsCompleted" -> "Daily Steps Goals Completed"
        "totalSteps" -> "Total Steps"
        else -> ""
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxSize(0.9f) // The dialog takes 90% of the screen size
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = correctCategoryString,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37),
                    textAlign = TextAlign.Center
                )

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(testFullLeaderboard) { index, user -> // Loops through the leaderboard and gets index and user
                        val score = when (categoryString) {
                            "waterGoalsCompleted" -> user.waterGoalsCompleted
                            "caloriesGoalsCompleted" -> user.caloriesGoalsCompleted
                            "pushUpsGoalsCompleted" -> user.exerciseGoalsCompleted
                            "stepsGoalsCompleted" -> user.stepsGoalsCompleted
                            "totalSteps" -> user.totalSteps
                            else -> 0
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${index + 1}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )

                            Image(
                                painter = painterResource(id = avatarMap.getValue(user.profilePictureString)),
                                contentDescription = "Leaderboard Icon",
                                modifier = Modifier.size(48.dp)
                            )

                            Text(
                                text = user.username,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = score.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
