package com.healthterra.services

import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.healthterra.data.UserDatabase
import com.healthterra.helpers.calculateCaloriesGoal
import com.healthterra.helpers.calculateExerciseGoal
import com.healthterra.helpers.calculateStepsGoal
import com.healthterra.helpers.calculateWaterGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate

// The function is triggered from onResume and from performDailyMaintenanceAppActive. Because of Mutex, if multiple triggers fire
// simultaneously, duplicates are put on hold, preventing race conditions. If it's a new day, it sends a final sync to Firestore and resets the
// trackings and settings tables
private val mutex = Mutex()
suspend fun performDailyMaintenance(context: Context) {
    mutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                val database = UserDatabase.getDatabase(context)
                val trackingsDao = database.trackingsDao()
                val settingsDao = database.settingsDao()
                val characteristicsDao = database.characteristicsDao()

                val userTrackings = trackingsDao.getAll().first().firstOrNull()
                val userSettings = settingsDao.getAll().first().firstOrNull()
                val userCharacteristics = characteristicsDao.getAll().first().firstOrNull()

                // Triggers on a fresh install
                if (userTrackings == null || userSettings == null || userCharacteristics == null) {
                    return@withContext
                }

                // If it has already reset today, it doesn't reset again
                if (LocalDate.now().toString() <= userSettings.lastSavedDate) {
                    return@withContext
                }

                // Sends a reset signal to the StepTrackerService
                if (userSettings.stepTracking == "Enabled") {
                    val resetIntent = Intent(context, StepTrackerService::class.java).apply {
                        action = "RESET_STEPS_MIDNIGHT"
                    }

                    context.startService(resetIntent)
                }

                // Syncs trackings to Firestore to preserve data before the Room API reset, because the reset happens faster than the sync and
                // to also handle multi-day offline syncing, the data is stored in the worker using snapshots, needs network
                val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                val syncRequest = OneTimeWorkRequestBuilder<SyncDailyTrackingsWorker>()
                    .setConstraints(constraints)
                    .setInputData(workDataOf(
                        "snapshot_water_progress" to userTrackings.waterProgress.sum(),
                        "snapshot_calories_progress" to userTrackings.caloriesProgress.sum(),
                        "snapshot_exercise_progress" to userTrackings.exerciseProgress.sum(),
                        "snapshot_steps_progress" to userTrackings.stepsProgress,
                        "snapshot_water_goal" to calculateWaterGoal(userCharacteristics),
                        "snapshot_calories_goal" to calculateCaloriesGoal(userCharacteristics),
                        "snapshot_exercise_goal" to calculateExerciseGoal(userCharacteristics),
                        "snapshot_steps_goal" to calculateStepsGoal(userCharacteristics),
                        "snapshot_date" to userSettings.lastSavedDate // Can't use LocalDate.now() because the function runs on a new day
                    ))
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "SyncDailyTrackingsWorker_${userSettings.lastSavedDate}", // Shared id with DailyMaintenance because they use the same worker
                    ExistingWorkPolicy.REPLACE,
                    syncRequest
                )

                val updateUserTrackings = userTrackings.copy(
                    waterProgress = emptyList(),
                    caloriesProgress = emptyList(),
                    exerciseProgress = emptyList(),
                    stepsProgress = 0
                )

                trackingsDao.update(updateUserTrackings)

                val updateUserSettings = userSettings.copy(lastSavedDate = LocalDate.now().toString())
                settingsDao.update(updateUserSettings)
            }

            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
