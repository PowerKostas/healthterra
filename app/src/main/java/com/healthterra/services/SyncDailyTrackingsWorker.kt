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

// The function is triggered from CategoriesScreen and DailyMaintenance, the daily_sync cloud function uses this data
class SyncDailyTrackingsWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.success()

        return try {
            val database = UserDatabase.getDatabase(applicationContext)
            val userCharacteristics = database.characteristicsDao().getAll().first().firstOrNull()
            val userTrackings = database.trackingsDao().getAll().first().firstOrNull()

            if (userCharacteristics == null || userTrackings == null) {
                return Result.success()
            }

            val waterGoal = calculateWaterGoal(userCharacteristics)
            val caloriesGoal = calculateCaloriesGoal(userCharacteristics)
            val exerciseGoal = calculateExerciseGoal(userCharacteristics)
            val stepsGoal = calculateStepsGoal(userCharacteristics)

            // Optional snapshots, defaults to -1 and then gets converted to null
            val snapshotWater = inputData.getInt("snapshot_water_progress", -1).takeIf { it >= 0 }
            val snapshotCalories = inputData.getInt("snapshot_calories_progress", -1).takeIf { it >= 0 }
            val snapshotExercise = inputData.getInt("snapshot_exercise_progress", -1).takeIf { it >= 0 }
            val snapshotSteps = inputData.getInt("snapshot_steps_progress", -1).takeIf { it >= 0 }
            val snapshotDate = inputData.getString("snapshot_date")

            // If parameters are provided, they are used, otherwise use the Room API values
            val waterProgress = snapshotWater ?: userTrackings.waterProgress.sum()
            val caloriesProgress = snapshotCalories ?: userTrackings.caloriesProgress.sum()
            val exerciseProgress = snapshotExercise ?: userTrackings.exerciseProgress.sum()
            val stepsProgress = snapshotSteps ?: userTrackings.stepsProgress
            val documentDate = snapshotDate ?: LocalDate.now().toString()

            val updateData = mapOf(
                "waterProgress" to waterProgress,
                "caloriesProgress" to caloriesProgress,
                "exerciseProgress" to exerciseProgress,
                "stepsProgress" to stepsProgress,
                "waterGoal" to waterGoal,
                "caloriesGoal" to caloriesGoal,
                "exerciseGoal" to exerciseGoal,
                "stepsGoal" to stepsGoal
            )

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("daily_trackings")
                .document(documentDate)
                .set(updateData, SetOptions.merge())
                .await()

            Result.success()
        }

        catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
