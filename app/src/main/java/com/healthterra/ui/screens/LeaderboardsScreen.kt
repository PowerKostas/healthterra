package com.healthterra.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.healthterra.R
import com.healthterra.data.documents.HealthiestUser
import com.healthterra.helpers.createGoldMetallicBrush
import com.healthterra.services.getCategoryTopUsers
import com.healthterra.services.getCurrentUser
import com.healthterra.services.getHealthiestUser
import com.healthterra.ui.components.general.InfoDialog
import com.healthterra.ui.components.screen.LeaderboardBox
import com.healthterra.ui.components.screen.LeaderboardDialog
import com.healthterra.ui.components.screen.avatarMap

// Have to isolate the animated row so the screen doesn't recompose on every frame and block clicks
@Composable
fun HealthiestUserAnimatedRow(avatarMap: Map<String, Int>, healthiestUser: HealthiestUser) {
    // Creates a float number that counts from 0 to 1 at a steady speed (LinearEasing), it takes durationMillis seconds to
    // hit 1, it reverses to 0, the moment it reaches 1 (RepeatMode.Reverse)
    val infiniteTransition = rememberInfiniteTransition()
    val animationProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = avatarMap.getValue(healthiestUser.profilePictureString),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(80.dp)

                // Every time animation progress gets a new value, it recreates the brush instance, relative to the animated component. By
                // reading the animation progress inside the Draw Phase (rather than the Composition Phase), it prevents the screen from
                // recomposing on every frame. The animated icon border is drawn directly on top of the image
                .drawWithCache {
                    val brush = createGoldMetallicBrush(animationProgress.value, IntSize(size.width.toInt(), size.height.toInt()))
                    val strokeWidth = 3.dp.toPx()

                    onDrawWithContent {
                        drawContent()
                        drawCircle(
                            brush = brush,
                            radius = (size.minDimension - strokeWidth) / 2f,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
                        )
                    }
                }
        )

        Text(
            text = healthiestUser.username,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,

            modifier = Modifier
                // Similar to the animated icon border, but this time graphicsLayer renders the text to a temporary, invisible
                // canvas. BlendMode.SrcIn uses that text as a mask, painting the animated gold brush only where the text pixels exist
                .graphicsLayer { compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen }
                .drawWithCache {
                    val brush = createGoldMetallicBrush(animationProgress.value, IntSize(size.width.toInt(), size.height.toInt()))
                    onDrawWithContent {
                        drawContent()
                        drawRect(
                            brush = brush,
                            blendMode = androidx.compose.ui.graphics.BlendMode.SrcIn
                        )
                    }
                }
        )
    }
}


// Even if the user has no network connection, the screen will show the old data, except on a fresh install, then it will be empty
@Composable
fun LeaderboardsScreen() {
    val waterLeaderboards by getCategoryTopUsers("waterGoalsCompleted", 1)
    val caloriesLeaderboards by getCategoryTopUsers("caloriesGoalsCompleted", 1)
    val exerciseLeaderboards by getCategoryTopUsers("exerciseGoalsCompleted", 1)
    val stepsLeaderboards by getCategoryTopUsers("stepsGoalsCompleted", 1)
    val totalStepsLeaderboards by getCategoryTopUsers("totalSteps", 1)

    val topWaterUser = waterLeaderboards.firstOrNull()
    val topCaloriesUser = caloriesLeaderboards.firstOrNull()
    val topExerciseUser = exerciseLeaderboards.firstOrNull()
    val topStepsUser = stepsLeaderboards.firstOrNull()
    val topTotalStepsUser = totalStepsLeaderboards.firstOrNull()

    val currentUserId = Firebase.auth.currentUser?.uid
    val tempCurrentUser by getCurrentUser(currentUserId)
    val currentUser = tempCurrentUser

    val tempHealthiestUser by getHealthiestUser()
    val healthiestUser = tempHealthiestUser

    // Tracks which LeaderboardDialog is currently open
    var selectedLeaderboardDialog by rememberSaveable { mutableStateOf<String?>(null) }

    var showHealthiestUserDialog by rememberSaveable { mutableStateOf(false) }

    if (topWaterUser != null && topCaloriesUser != null && topExerciseUser != null && topStepsUser != null && topTotalStepsUser != null && currentUser != null && healthiestUser != null) { // Loading screen
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 24.dp, bottom = 12.dp)

        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD4AF37), thickness = 2.dp)

                Text(
                    text = "Daily Goals Completed",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFD4AF37),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD4AF37), thickness = 2.dp)
            }

            topWaterUser.let { user ->
                // Checks if the current user is the top user, if he is, make his score null to know not to draw a specific component
                // Also makes the LeaderboardBox clickable, when clicked the LeaderboardDialog shows
                val currentUserScore = if (currentUserId == user.uid) null else (currentUser.waterGoalsCompleted.toString())
                LeaderboardBox(Modifier.clickable { selectedLeaderboardDialog = "waterGoalsCompleted" }, avatarMap.getValue(user.profilePictureString), user.username, R.drawable.water, "Water", user.waterGoalsCompleted.toString(), currentUserScore)
            }

            topCaloriesUser.let { user ->
                val currentUserScore = if (currentUserId == user.uid) null else (currentUser.caloriesGoalsCompleted.toString())
                LeaderboardBox(Modifier.clickable { selectedLeaderboardDialog = "caloriesGoalsCompleted" }, avatarMap.getValue(user.profilePictureString), user.username, R.drawable.calories, "Calories", user.caloriesGoalsCompleted.toString(), currentUserScore)
            }

            topExerciseUser.let { user ->
                val currentUserScore = if (currentUserId == user.uid) null else (currentUser.exerciseGoalsCompleted.toString())
                LeaderboardBox(Modifier.clickable { selectedLeaderboardDialog = "exerciseGoalsCompleted" }, avatarMap.getValue(user.profilePictureString), user.username, R.drawable.exercise, "Exercise", user.exerciseGoalsCompleted.toString(), currentUserScore)
            }

            topStepsUser.let { user ->
                val currentUserScore = if (currentUserId == user.uid) null else (currentUser.stepsGoalsCompleted.toString())
                LeaderboardBox(Modifier.clickable { selectedLeaderboardDialog = "stepsGoalsCompleted" }, avatarMap.getValue(user.profilePictureString), user.username, R.drawable.steps, "Steps", user.stepsGoalsCompleted.toString(), currentUserScore)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD4AF37), thickness = 2.dp)

                Text(
                    text = "Total Steps",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFD4AF37),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD4AF37), thickness = 2.dp)
            }

            topTotalStepsUser.let { user ->
                val currentUserScore = if (currentUserId == user.uid) null else (currentUser.totalSteps.toString())
                LeaderboardBox(Modifier.clickable { selectedLeaderboardDialog = "totalSteps" }, avatarMap.getValue(user.profilePictureString), user.username, R.drawable.steps, "Steps", user.totalSteps.toString(), currentUserScore)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD4AF37), thickness = 2.dp)

                Text(
                    text = "Healthiest User",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFD4AF37),
                    fontWeight = FontWeight.ExtraBold,
                )

                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info Button",
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { showHealthiestUserDialog = true }
                )

                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD4AF37), thickness = 2.dp)
            }

            HealthiestUserAnimatedRow(healthiestUser = healthiestUser, avatarMap = avatarMap)

            HorizontalDivider(color = Color(0xFFD4AF37), thickness = 2.dp)
        }
    }


    // Show the LeaderboardDialog, if a category is clicked
    selectedLeaderboardDialog?.let { categoryString ->
        val categoryLeaderboards by getCategoryTopUsers(categoryString, 50)
        LeaderboardDialog(categoryString, categoryLeaderboards, avatarMap, onDismiss = { selectedLeaderboardDialog = null })
    }

    if (showHealthiestUserDialog) {
        InfoDialog(Icons.Default.Info, Color(0xFFD4AF37), null, AnnotatedString("The 'Healthiest User' is the person with the highest total score. You earn points by completing each of your daily goals."), "Got it", null, true, FontWeight.Bold, { showHealthiestUserDialog = false }, { showHealthiestUserDialog = false })
    }
}
