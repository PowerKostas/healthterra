package com.kostas.gohealth.ui.screens

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kostas.gohealth.R
import com.kostas.gohealth.helpers.calculateCaloriesGoal
import com.kostas.gohealth.helpers.calculateExerciseGoal
import com.kostas.gohealth.helpers.calculateStepsGoal
import com.kostas.gohealth.helpers.calculateWaterGoal
import com.kostas.gohealth.helpers.checkAutoTimeSetting
import com.kostas.gohealth.ui.components.general.InfoDialog
import com.kostas.gohealth.ui.components.screen.ProgressBox
import com.kostas.gohealth.ui.viewModels.CharacteristicsViewModel
import com.kostas.gohealth.ui.viewModels.SettingsViewModel
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

    var showProfileWarningDialog by remember { mutableStateOf(false) }
    var showSettingsErrorDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    var isAutoTimeEnabled by remember { mutableStateOf(checkAutoTimeSetting(context)) }
    val isProfileIncomplete = userCharacteristics.gender == null || userCharacteristics.age == null || userCharacteristics.height == null || userCharacteristics.weight == null || userCharacteristics.activityLevel == null

    // For the edge case that the user leaves the app open, changes the 'Automatic date and time' setting and comes back, every time the
    // user returns to the app, it checks the setting and hides the error button/dialog if needed
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAutoTimeEnabled = checkAutoTimeSetting(context)

                if (isAutoTimeEnabled) {
                    showSettingsErrorDialog = false
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Draws the screen
    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp, 32.dp, 16.dp, 16.dp) // Smaller bottom than top padding, because the bottom navigation bar has padding too
        ) {
            // Only adds the error row if one of the warnings/errors is present
            if (isProfileIncomplete || !isAutoTimeEnabled) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)) {
                    // Warning text, if the user hasn't filled all the characteristics in his profile
                    if (isProfileIncomplete) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Warning Button",
                            tint = Color(0xFFFFA000),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { showProfileWarningDialog = true }
                        )
                    }

                    // Error text, if the user has turned off the automatic date and time setting
                    if (!isAutoTimeEnabled) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error Button",
                            tint = Color(0xFFE53935),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { showSettingsErrorDialog = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            Column(verticalArrangement = Arrangement.spacedBy(48.dp)) {
                ProgressBox(R.drawable.water, "Water", Color(0xFF2196F3), waterProgressSum, waterGoal, onClick = { onNavigate("Water") })
                ProgressBox(R.drawable.calories, "Calories", Color(0xFF8B4513), caloriesProgressSum, caloriesGoal, onClick = { onNavigate("Calories") })
                ProgressBox(R.drawable.exercise, "Exercise", Color.Black, exerciseProgressSum, exerciseGoal, onClick = { onNavigate("Exercise") })
                ProgressBox(R.drawable.steps, "Steps", Color(0xFFE0AC69), stepsProgress, stepsGoal, onClick = { onNavigate("Steps") })
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
        append("To provide our core features, GoHealth needs to process your personal wellness data. By tapping Consent, you explicitly agree to the processing of this data as outlined in the ")

        withLink(LinkAnnotation.Url("https://powerkostas.github.io/gohealth-web/privacy.html", styles = linkStyle)) {
            append("Privacy Policy")
        }

        append(" and you accept the ")

        withLink(LinkAnnotation.Url("https://powerkostas.github.io/gohealth-web/terms.html", styles = linkStyle)) {
            append("Terms & Conditions")
        }

        append(".")
    }

    if (userSettings.showMandatoryDialog) {
        InfoDialog(null, MaterialTheme.colorScheme.onPrimary, "Welcome!", annotatedText, "Consent", "Exit", false,
            { settingsViewModel.updateUserSettings(
                userSettings.copy(showMandatoryDialog = false)
            )},

            { activity.finish() }
        )
    }

    if (showProfileWarningDialog) {
        InfoDialog(Icons.Default.Error, Color(0xFFFFA000), null, AnnotatedString("Complete your profile for more personalized results."), "Got it", null, true, { showProfileWarningDialog = false }, { showProfileWarningDialog = false })
    }

    if (showSettingsErrorDialog) {
        InfoDialog(Icons.Default.Error, Color(0xFFE53935), null, AnnotatedString("Leaderboard syncing is paused! Please enable 'Automatic date and time' in your device settings."), "Got it", null, true, { showSettingsErrorDialog = false }, { showSettingsErrorDialog = false })
    }
}
