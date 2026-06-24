package com.healthterra.services

import android.content.Context
import com.healthterra.helpers.generateRandomProfilePictureString
import com.healthterra.ui.viewModels.CharacteristicsViewModel
import com.healthterra.ui.viewModels.DailyTrackingsViewModel
import com.healthterra.ui.viewModels.SettingsViewModel
import com.healthterra.ui.viewModels.TrackingsViewModel

fun roomDelete(characteristicsViewModel: CharacteristicsViewModel, settingsViewModel: SettingsViewModel, trackingsViewModel: TrackingsViewModel, dailyTrackingsViewModel: DailyTrackingsViewModel, randomUsername: String, context: Context) {
    val userCharacteristics = characteristicsViewModel.characteristics.value.firstOrNull()
    val userSettings = settingsViewModel.settings.value.firstOrNull()
    val userTrackings = trackingsViewModel.trackings.value.firstOrNull()

    if (userCharacteristics == null || userSettings == null || userTrackings == null) {
        return
    }

    characteristicsViewModel.updateUserCharacteristics(
        userCharacteristics.copy(
            gender = null,
            age = null,
            height = null,
            weight = null,
            activityLevel = null,
            weightGoal = "Maintain",
            kgGoal = 0,
            daysGoal = 0
        ),

        context
    )

    settingsViewModel.updateUserSettings(
        userSettings.copy(
            profilePictureString = generateRandomProfilePictureString(),
            username = randomUsername,
            leaderboardsVisibility = "Anonymous"
        ),

        context
    )

    trackingsViewModel.updateUserTrackings(
        userTrackings.copy(
            waterProgress = emptyList(),
            caloriesProgress = emptyList(),
            exerciseProgress = emptyList(),
            stepsProgress = 0
        )
    )

    dailyTrackingsViewModel.deleteUserDailyTrackings()
}
