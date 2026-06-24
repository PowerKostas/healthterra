package com.healthterra.ui.screens

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthterra.R
import com.healthterra.helpers.calculateCaloriesGoal
import com.healthterra.helpers.calculateExerciseGoal
import com.healthterra.helpers.calculateStepsGoal
import com.healthterra.helpers.calculateWaterGoal
import com.healthterra.helpers.roundGoal
import com.healthterra.ui.components.general.InfoDialog
import com.healthterra.ui.components.screen.ProgressBox
import com.healthterra.ui.components.screen.ProgressHeader
import com.healthterra.ui.viewModels.CharacteristicsViewModel
import com.healthterra.ui.viewModels.SettingsViewModel
import com.healthterra.ui.viewModels.TrackingsViewModel
import kotlin.math.roundToInt

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    // Gets values from the database
    val trackingsViewModel = viewModel<TrackingsViewModel>(factory = TrackingsViewModel.Factory)
    val userTrackingsList by trackingsViewModel.trackings.collectAsState()
    val userTrackings = userTrackingsList.firstOrNull()

    val characteristicsViewModel = viewModel<CharacteristicsViewModel>(factory = CharacteristicsViewModel.Factory)
    val userCharacteristicsList by characteristicsViewModel.characteristics.collectAsState()
    val userCharacteristics = userCharacteristicsList.firstOrNull()

    val settingsViewModel = viewModel<SettingsViewModel>(factory = SettingsViewModel.Factory)
    val userSettingsList by settingsViewModel.settings.collectAsState()
    val userSettings = userSettingsList.firstOrNull()

    // Waits for the database to load
    if (userTrackings == null || userCharacteristics == null || userSettings == null) {
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

    // Calculates how many goals are met
    val minCaloriesValue = roundGoal((caloriesGoal - caloriesGoal * 0.1).roundToInt())
    val maxCaloriesValue = roundGoal((caloriesGoal + caloriesGoal * 0.1).roundToInt())
    val completedGoals = listOf(waterProgressSum >= waterGoal, caloriesProgressSum in minCaloriesValue..maxCaloriesValue, exerciseProgressSum >= exerciseGoal, stepsProgress >= stepsGoal).count { it }

    val isProfileIncomplete = userCharacteristics.gender == null || userCharacteristics.age == null || userCharacteristics.height == null || userCharacteristics.weight == null || userCharacteristics.activityLevel == null
    val isAnonymous = userSettings.leaderboardsVisibility == "Anonymous"

    var showProfileIncompleteDialog by rememberSaveable { mutableStateOf(false) }
    var showAnonymousDialog by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as Activity

    // Draws the screen, verticalScroll creates infinite vertical space, which breaks fillMaxSize(). BoxWithConstraints can grab the true screen
    // height with maxHeight. The goal is to stretch the progress boxes as much as possible, without making the screen scrollable. On smaller
    // screens, the progress boxes collapse, so the screen is still made scrollable and dynamicProgressBoxHeight sets a tested minimum floor. On
    // bigger screens, in order for the 2 rows of progress boxes to stretch perfectly, the exact remaining height is calculated and divided by 2
    var topHalfColumnPx by remember { mutableIntStateOf(0) }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Remaining height = Total height - Top half column height - 24.dp (Space between header and progress boxes) - 24.dp (Space between the
        // two progress boxes rows - 24.dp (Bottom padding) - 1.dp (Rounding issues)
        val remainingHeight = maxHeight - with(LocalDensity.current) { topHalfColumnPx.toDp() } - (24.dp + 24.dp + 12.dp + 1.dp)
        val dynamicProgressBoxHeight = maxOf(200.dp, remainingHeight / 2)

        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (topHalfColumnPx > 0) 1f else 0f) // Hide the screen while measuring to avoid jumping
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp)
        ) {
            // Top half column
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { size ->
                        topHalfColumnPx = size.height
                    }
            ) {
                ProgressHeader(Color.White, completedGoals, 4)

                // Text and error row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Daily Metrics",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Only adds the row if one of the warnings is present
                    if (isProfileIncomplete || isAnonymous) {
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            // If the user hasn't filled all the characteristics in their profile
                            if (isProfileIncomplete) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Warning Button 1",
                                    tint = Color(0xFFFFA000),
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { showProfileIncompleteDialog = true }
                                )
                            }

                            if (isAnonymous) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = "Warning Button 3",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { showAnonymousDialog = true }
                                )
                            }
                        }
                    }
                }
            }

            // Bottom half column
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    ProgressBox(modifier = Modifier.weight(1f).height(dynamicProgressBoxHeight), R.drawable.water, "Water", Color(0xFF2196F3), waterProgressSum, waterGoal, onClick = { onNavigate("Water") })
                    ProgressBox(modifier = Modifier.weight(1f).height(dynamicProgressBoxHeight), R.drawable.calories, "Calories", Color(0xFF8B4513), caloriesProgressSum, caloriesGoal, onClick = { onNavigate("Calories") })
                }

                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    ProgressBox(modifier = Modifier.weight(1f).height(dynamicProgressBoxHeight), R.drawable.exercise, "Exercise", Color.Black, exerciseProgressSum, exerciseGoal, onClick = { onNavigate("Exercise") })
                    ProgressBox(modifier = Modifier.weight(1f).height(dynamicProgressBoxHeight), R.drawable.steps, "Steps", Color(0xFFE0AC69), stepsProgress, stepsGoal, onClick = { onNavigate("Steps") })
                }
            }
        }
    }


    val linkStyle = TextLinkStyles(
        style = SpanStyle(
            color = Color(0xFF0645AD),
            textDecoration = TextDecoration.Underline,
        ),

        pressedStyle = SpanStyle(
            color = Color(0xFF002266),
            background = Color(0x330645AD)
        )
    )

    val annotatedText = buildAnnotatedString {
        append("To provide our core features, Healthterra needs to process your personal wellness data. By tapping Consent, you explicitly agree to the processing of this data as outlined in the ")

        withLink(LinkAnnotation.Url("https://powerkostas.github.io/healthterra-web/privacy.html", styles = linkStyle)) {
            append("Privacy Policy")
        }

        append(" and you accept the ")

        withLink(LinkAnnotation.Url("https://powerkostas.github.io/healthterra-web/terms.html", styles = linkStyle)) {
            append("Terms & Conditions")
        }

        append(".")
    }

    if (userSettings.showMandatoryDialog) {
        InfoDialog(null, MaterialTheme.colorScheme.onPrimary, "Welcome!", annotatedText, "Consent", "Exit", false, FontWeight.Normal,
            { settingsViewModel.updateUserSettings(
                userSettings.copy(showMandatoryDialog = false),
                context,
                false
            )},

            { activity.finish() }
        )
    }

    if (showProfileIncompleteDialog) {
        InfoDialog(Icons.Default.Error, Color(0xFFFFA000), null, AnnotatedString("Complete your Profile for more personalized results."), "Got it", null, true, FontWeight.Bold, { showProfileIncompleteDialog = false }, { showProfileIncompleteDialog= false })
    }

    if (showAnonymousDialog) {
        InfoDialog(Icons.Default.VisibilityOff, MaterialTheme.colorScheme.primary, null, AnnotatedString("You are currently anonymous on the Leaderboards. Manage your visibility in your Profile."), "Got it", null, true, FontWeight.Bold, { showAnonymousDialog = false }, { showAnonymousDialog = false })
    }
}
