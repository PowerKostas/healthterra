package com.healthterra.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.healthterra.data.UserDatabase
import com.healthterra.helpers.calculateCaloriesGoal
import com.healthterra.helpers.calculateExerciseGoal
import com.healthterra.helpers.calculateStepsGoal
import com.healthterra.helpers.calculateWaterGoal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

// The function is triggered from CharacteristicsViewModel, characteristics, profilePictureString and username are for the leaderboards, the
// others are for backup, daily trackings is for the leaderboards_sync cloud function
class SyncUserDailyTrackingsWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.success()

        return try {
            val database = UserDatabase.getDatabase(applicationContext)
            val userCharacteristics = database.characteristicsDao().getAll().first().firstOrNull()
            val userSettings = database.settingsDao().getAll().first().firstOrNull()
            val userTrackings = database.trackingsDao().getAll().first().firstOrNull()

            if (userCharacteristics == null || userSettings == null || userTrackings == null) {
                return Result.success()
            }

            // It needs to update both the user and the daily_trackings documents, so it's doing batched writes
            val firestore = FirebaseFirestore.getInstance()
            val batch = firestore.batch()

            val updateData = mapOf(
                "gender" to userCharacteristics.gender,
                "age" to userCharacteristics.age,
                "height" to userCharacteristics.height,
                "weight" to userCharacteristics.weight,
                "activityLevel" to userCharacteristics.activityLevel,
                "weightGoal" to userCharacteristics.weightGoal,
                "kgGoal" to userCharacteristics.kgGoal,
                "daysGoal" to userCharacteristics.daysGoal,

                "profilePictureString" to userSettings.profilePictureString,
                "username" to userSettings.username,
                "appearance" to userSettings.appearance,
                "stepTracking" to userSettings.stepTracking,
                "lastSavedDate" to userSettings.lastSavedDate,
                "initialWeightGoalDate" to userSettings.initialWeightGoalDate
            )

            // Adds the user update to the batch
            batch.set(firestore.collection("users").document(uid), updateData, SetOptions.merge())

            // Optional snapshots, defaults to -1 and then gets converted to null
            val snapshotWaterGoal = inputData.getInt("snapshot_water_goal", -1).takeIf { it >= 0 }
            val snapshotCaloriesGoal = inputData.getInt("snapshot_calories_goal", -1).takeIf { it >= 0 }
            val snapshotExerciseGoal = inputData.getInt("snapshot_exercise_goal", -1).takeIf { it >= 0 }
            val snapshotStepsGoal = inputData.getInt("snapshot_steps_goal", -1).takeIf { it >= 0 }

            // If parameters are provided, they are used, otherwise use the Room API values. It syncs progress too, only to fix the edge case
            // example that the goal is 2700, the user inputs 1000 + 1000 + 1000 + 1000, only 3000 is synced, they update their goal to 3200, now
            // the goal seems as if it's not reached
            val dailyTrackingsGoalsMap = mapOf(
                "waterGoal" to (snapshotWaterGoal ?: calculateWaterGoal(userCharacteristics)),
                "caloriesGoal" to (snapshotCaloriesGoal ?: calculateCaloriesGoal(userCharacteristics)),
                "exerciseGoal" to (snapshotExerciseGoal ?: calculateExerciseGoal(userCharacteristics)),
                "stepsGoal" to (snapshotStepsGoal ?: calculateStepsGoal(userCharacteristics)),
                "waterProgress" to userTrackings.waterProgress.sum(),
                "caloriesProgress" to userTrackings.caloriesProgress.sum(),
                "exerciseProgress" to userTrackings.exerciseProgress.sum(),
                "stepsProgress" to userTrackings.stepsProgress
            )

            // Adds the daily trackings goals update to the batch
            val snapshotDate = inputData.getString("snapshot_date") ?: LocalDate.now().toString()
            batch.set(firestore.collection("users").document(uid).collection("daily_trackings").document(snapshotDate), dailyTrackingsGoalsMap, SetOptions.merge())

            batch.commit().await()
            Result.success()
        }

        catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
