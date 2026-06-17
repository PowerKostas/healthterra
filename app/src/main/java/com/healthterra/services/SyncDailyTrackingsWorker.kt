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

// The function is triggered from CategoriesScreen, DailyMaintenance, StepTracker and MainActivity.schedulePeriodicSync, the leaderboards_sync
// cloud function uses this data
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

            // Optional snapshots, defaults to -1 and then gets converted to null
            val snapshotWater = inputData.getInt("snapshot_water_progress", -1).takeIf { it >= 0 }
            val snapshotCalories = inputData.getInt("snapshot_calories_progress", -1).takeIf { it >= 0 }
            val snapshotExercise = inputData.getInt("snapshot_exercise_progress", -1).takeIf { it >= 0 }
            val snapshotSteps = inputData.getInt("snapshot_steps_progress", -1).takeIf { it >= 0 }
            val snapshotWaterGoal = inputData.getInt("snapshot_water_goal", -1).takeIf { it >= 0 }
            val snapshotCaloriesGoal = inputData.getInt("snapshot_calories_goal", -1).takeIf { it >= 0 }
            val snapshotExerciseGoal = inputData.getInt("snapshot_exercise_goal", -1).takeIf { it >= 0 }
            val snapshotStepsGoal = inputData.getInt("snapshot_steps_goal", -1).takeIf { it >= 0 }

            // If parameters are provided, they are used, otherwise use the Room API values
            val updateData = mapOf(
                "waterProgress" to (snapshotWater ?: userTrackings.waterProgress.sum()),
                "caloriesProgress" to (snapshotCalories ?: userTrackings.caloriesProgress.sum()),
                "exerciseProgress" to (snapshotExercise ?: userTrackings.exerciseProgress.sum()),
                "stepsProgress" to (snapshotSteps ?: userTrackings.stepsProgress),
                "waterGoal" to (snapshotWaterGoal ?: calculateWaterGoal(userCharacteristics)),
                "caloriesGoal" to (snapshotCaloriesGoal ?: calculateCaloriesGoal(userCharacteristics)),
                "exerciseGoal" to (snapshotExerciseGoal ?: calculateExerciseGoal(userCharacteristics)),
                "stepsGoal" to (snapshotStepsGoal ?: calculateStepsGoal(userCharacteristics)),
            )

            val snapshotDate = inputData.getString("snapshot_date")
            val documentDate = snapshotDate ?: LocalDate.now().toString()
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
