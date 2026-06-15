package com.healthterra.services

import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.healthterra.data.UserDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate

// The function is triggered from onResume and from performDailyMaintenanceAppActive. Because of Mutex, if multiple triggers fire
// simultaneously, duplicates are put on hold, preventing race conditions. If it's a new day, it resets the trackings and settings tables and
// sends a final sync to Firestore
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

                // Syncs to Firestore, because the local Room API reset happens faster, the data is stored in the worker with the snapshots
                val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                val syncRequest = OneTimeWorkRequestBuilder<FirestoreSyncWorker>()
                    .setConstraints(constraints)
                    .setInputData(workDataOf(
                        "sync_user" to false,
                        "snapshot_water_progress" to userTrackings.waterProgress.sum(),
                        "snapshot_calories_progress" to userTrackings.caloriesProgress.sum(),
                        "snapshot_exercise_progress" to userTrackings.exerciseProgress.sum(),
                        "snapshot_steps_progress" to userTrackings.stepsProgress,
                        "snapshot_date" to userSettings.lastSavedDate
                    ))
                    .build()

                WorkManager.getInstance(context).enqueue(syncRequest)

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
