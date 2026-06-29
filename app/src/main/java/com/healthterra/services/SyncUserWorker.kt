package com.healthterra.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.healthterra.data.UserDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

// The function is triggered from SettingsViewModel and MainActivity.onStop, profilePictureString and username are for the leaderboards, the
// others are for backup
class SyncUserWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.success()

        return try {
            val database = UserDatabase.getDatabase(applicationContext)
            val userSettings = database.settingsDao().getAll().first().firstOrNull() ?: return Result.success()
            val userAchievements = database.achievementsDao().getAll().first().firstOrNull() ?: return Result.success()

            val userUpdateData = mutableMapOf<String, Any?>(
                "profilePictureString" to userSettings.profilePictureString,
                "username" to userSettings.username,
                "appearance" to userSettings.appearance,
                "stepTracking" to userSettings.stepTracking,
                "lastSavedDate" to userSettings.lastSavedDate,
                "initialWeightGoalDate" to userSettings.initialWeightGoalDate,
                "leaderboardsVisibility" to userSettings.leaderboardsVisibility
            )

            val achievementsUpdateData = mapOf(
                "maxSteps" to userAchievements.maxSteps,
                "activeWaterStreak" to userAchievements.activeWaterStreak,
                "activeCaloriesStreak" to userAchievements.activeCaloriesStreak,
                "activeExerciseStreak" to userAchievements.activeExerciseStreak,
                "activeStepsStreak" to userAchievements.activeStepsStreak,
                "maxWaterStreak" to userAchievements.maxWaterStreak,
                "maxCaloriesStreak" to userAchievements.maxCaloriesStreak,
                "maxExerciseStreak" to userAchievements.maxExerciseStreak,
                "maxStepsStreak" to userAchievements.maxStepsStreak,
                "appearedOnWaterLeaderboards" to userAchievements.appearedOnWaterLeaderboards,
                "appearedOnCaloriesLeaderboards" to userAchievements.appearedOnCaloriesLeaderboards,
                "appearedOnExerciseLeaderboards" to userAchievements.appearedOnExerciseLeaderboards,
                "appearedOnStepsLeaderboards" to userAchievements.appearedOnStepsLeaderboards,
                "appearedOnTotalStepsLeaderboards" to userAchievements.appearedOnTotalStepsLeaderboards,
                "appearedOnHealthiestUser" to userAchievements.appearedOnHealthiestUser,
                "secret" to userAchievements.secret,
                "earlyPlaytester" to userAchievements.earlyPlaytester
            )

            // Achievements data is nested inside one field in the user's document
            userUpdateData["achievements"] = achievementsUpdateData

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .set(userUpdateData, SetOptions.merge())
                .await()

            Result.success()
        }

        catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
