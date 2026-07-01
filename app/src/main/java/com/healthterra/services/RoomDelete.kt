package com.healthterra.services

import android.content.Context
import com.healthterra.helpers.generateRandomProfilePictureString
import com.healthterra.ui.viewModels.AchievementsViewModel
import com.healthterra.ui.viewModels.CharacteristicsViewModel
import com.healthterra.ui.viewModels.DailyTrackingsViewModel
import com.healthterra.ui.viewModels.SettingsViewModel
import com.healthterra.ui.viewModels.TodayTrackingsViewModel

fun roomDelete(characteristicsViewModel: CharacteristicsViewModel, settingsViewModel: SettingsViewModel, todayTrackingsViewModel: TodayTrackingsViewModel, dailyTrackingsViewModel: DailyTrackingsViewModel, achievementsViewModel: AchievementsViewModel, randomUsername: String, context: Context) {
    val userCharacteristics = characteristicsViewModel.characteristics.value.firstOrNull()
    val userSettings = settingsViewModel.settings.value.firstOrNull()
    val userTodayTrackings = todayTrackingsViewModel.todayTrackings.value.firstOrNull()
    val userAchievements = achievementsViewModel.achievements.value.firstOrNull()

    if (userCharacteristics == null || userSettings == null || userTodayTrackings == null || userAchievements == null) {
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

    todayTrackingsViewModel.updateUserTodayTrackings(
        userTodayTrackings.copy(
            waterProgress = emptyList(),
            caloriesProgress = emptyList(),
            exerciseProgress = emptyList(),
            stepsProgress = 0
        )
    )

    dailyTrackingsViewModel.deleteUserDailyTrackings()

    achievementsViewModel.updateUserAchievements(
        userAchievements.copy(
            appearedOnWaterLeaderboards = false,
            appearedOnCaloriesLeaderboards = false,
            appearedOnExerciseLeaderboards = false,
            appearedOnStepsLeaderboards = false,
            appearedOnTotalStepsLeaderboards = false,
            appearedOnHealthiestUser = false,
            secret = false
        )
    )
}
