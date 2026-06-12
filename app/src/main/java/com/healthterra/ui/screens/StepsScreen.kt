package com.healthterra.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthterra.R
import com.healthterra.services.checkActivityPermissions
import com.healthterra.ui.components.general.ProgressBar
import com.healthterra.ui.viewModels.SettingsViewModel

@Composable
fun StepsScreen(categoryProgress: Int, categoryGoal: Int) {
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
    val userSettingsList by settingsViewModel.settings.collectAsState()
    val userSettings = userSettingsList.firstOrNull()

    userSettings ?: return // Loading screen

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(70.dp, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Checks the value of isActivityRecognitionEnabled every time the screen comes back into focus and if a change happened, it
        // redraws the screen
        val context = LocalContext.current

        var isActivityRecognitionEnabled by remember { mutableStateOf(checkActivityPermissions(context)) }

        LifecycleResumeEffect(Unit) {
            isActivityRecognitionEnabled = checkActivityPermissions(context)
            onPauseOrDispose { }
        }

        // Builds the appropriate text, if both permissions and settings are disabled, notifications gets priority in the text
        val text = buildAnnotatedString {
            if (isActivityRecognitionEnabled && userSettings.stepTracking == "Enabled") {
                append("Currently tracking steps...")
            }

            else if (!isActivityRecognitionEnabled) {
                append("Physical activity permissions are ")

                withStyle(style = SpanStyle(color = Color(0xFFE53935))) {
                    append("disabled")
                }

                append(". Navigate to your device Settings to enable them.")
            }

            else {
                append("Step tracking is ")

                withStyle(style = SpanStyle(color = Color(0xFFE53935))) {
                    append("disabled")
                }

                append(". You can turn it back on in your Profile.")
            }
        }

        Text(
            text = text,
            color = Color(0xFFE0AC69),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Icon(
            painter = painterResource(id = R.drawable.steps),
            contentDescription = "Category",
            tint = Color.Unspecified,
            modifier = Modifier.size(200.dp)
        )

        val remainder = categoryGoal % 10
        val roundedCategoryGoal = when {
            remainder < 5 -> categoryGoal - remainder
            remainder == 5 -> categoryGoal
            else -> categoryGoal + (10 - remainder)
        }

        Text(
            text = "$categoryProgress / $roundedCategoryGoal steps",
            style = MaterialTheme.typography.titleLarge
        )

        ProgressBar(20.dp, Color(0xFFE0AC69), categoryProgress.toFloat() / roundedCategoryGoal)
    }
}
