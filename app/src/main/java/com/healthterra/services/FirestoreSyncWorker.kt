package com.healthterra.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.healthterra.data.UserDatabase
import kotlinx.coroutines.flow.first

// The worker is triggered from onStop and DailyMaintenance
class FirestoreSyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        // Flags to make the worker sync only specific data, defaults to true
        val shouldSyncDailyTrackings = inputData.getBoolean("sync_daily_trackings", true)
        val shouldSyncUser = inputData.getBoolean("sync_user", true)

        return try {
            val database = UserDatabase.getDatabase(applicationContext)
            val userCharacteristics = database.characteristicsDao().getAll().first().firstOrNull()

            if (shouldSyncDailyTrackings) {
                val userTrackings = database.trackingsDao().getAll().first().firstOrNull()

                // Optional snapshots, defaults to -1 and then gets converted to null
                val snapshotWater = inputData.getInt("snapshot_water_progress", -1).takeIf { it >= 0 }
                val snapshotCalories = inputData.getInt("snapshot_calories_progress", -1).takeIf { it >= 0 }
                val snapshotExercise = inputData.getInt("snapshot_exercise_progress", -1).takeIf { it >= 0 }
                val snapshotSteps = inputData.getInt("snapshot_steps_progress", -1).takeIf { it >= 0 }
                val targetDate = inputData.getString("target_date")

                if (userTrackings != null && userCharacteristics != null) {
                    syncDailyTrackingsToFirestore(
                        userTrackings = userTrackings,
                        userCharacteristics = userCharacteristics,
                        waterProgress = snapshotWater,
                        caloriesProgress = snapshotCalories,
                        exerciseProgress = snapshotExercise,
                        stepsProgress = snapshotSteps,
                        targetDate = targetDate
                    )
                }
            }

            if (shouldSyncUser) {
                val userSettings = database.settingsDao().getAll().first().firstOrNull()
                if (userCharacteristics != null && userSettings != null) {
                    syncUserToFirestore(userCharacteristics, userSettings)
                }
            }

            Result.success()
        }

        catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
