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

// The function is triggered from CharacteristicsViewModel, SettingsViewModel and MainActivity
class SyncUserWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.success()

        return try {
            val database = UserDatabase.getDatabase(applicationContext)
            val userCharacteristics = database.characteristicsDao().getAll().first().firstOrNull()
            val userSettings = database.settingsDao().getAll().first().firstOrNull()

            if (userCharacteristics == null || userSettings == null) {
                return Result.success()
            }

            // It needs to update both the user and the daily_trackings documents, so it's doing batched writes
            val firestore = FirebaseFirestore.getInstance()
            val batch = firestore.batch()

            val userDataMap = mapOf(
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
            batch.set(firestore.collection("users").document(uid), userDataMap, SetOptions.merge())

            val dailyTrackingsGoalsMap = mapOf(
                "waterGoal" to calculateWaterGoal(userCharacteristics),
                "caloriesGoal" to calculateCaloriesGoal(userCharacteristics),
                "exerciseGoal" to calculateExerciseGoal(userCharacteristics),
                "stepsGoal" to calculateStepsGoal(userCharacteristics)
            )

            // Adds the daily trackings goals update to the batch
            batch.set(firestore.collection("users").document(uid).collection("daily_trackings").document(LocalDate.now().toString()), dailyTrackingsGoalsMap, SetOptions.merge())

            batch.commit().await()
            Result.success()
        }

        catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
