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
import com.healthterra.data.entities.DailyTrackings
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
import java.time.temporal.ChronoUnit
import kotlin.math.max

// The function is triggered from onResume, performDailyMaintenanceAppActive and StepTracker. Because of Mutex, if multiple triggers fire
// simultaneously, duplicates are put on hold, preventing race conditions. If it's a new day, it sends a final sync to Firestore and resets the
// trackings and settings tables
private val mutex = Mutex()
suspend fun performDailyMaintenance(context: Context) {
    mutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                val database = UserDatabase.getDatabase(context)
                val todayTrackingsDao = database.todayTrackingsDao()
                val settingsDao = database.settingsDao()
                val characteristicsDao = database.characteristicsDao()
                val dailyTrackingsDao = database.dailyTrackingsDao()
                val achievementsDao = database.achievementsDao()

                val userTodayTrackings = todayTrackingsDao.getAll().first().firstOrNull()
                val userSettings = settingsDao.getAll().first().firstOrNull()
                val userCharacteristics = characteristicsDao.getAll().first().firstOrNull()
                val userAchievements = achievementsDao.getAll().first().firstOrNull()

                // Triggers on a fresh install
                if (userTodayTrackings == null || userSettings == null || userCharacteristics == null || userAchievements == null) {
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

                val waterGoal = calculateWaterGoal(userCharacteristics)
                val caloriesGoal = calculateCaloriesGoal(userCharacteristics)
                val exerciseGoal = calculateExerciseGoal(userCharacteristics)
                val stepsGoal = calculateStepsGoal(userCharacteristics)
                val waterProgress = userTodayTrackings.waterProgress.sum()
                val caloriesProgress = userTodayTrackings.caloriesProgress.sum()
                val exerciseProgress = userTodayTrackings.exerciseProgress.sum()
                val stepsProgress = userTodayTrackings.stepsProgress
                val lastSavedDate = userSettings.lastSavedDate

                // Local sync for the daily trackings table
                val yesterdayTracking = DailyTrackings(
                    userId = userTodayTrackings.userId,
                    date = lastSavedDate,
                    waterProgress = waterProgress,
                    caloriesProgress = caloriesProgress,
                    exerciseProgress = exerciseProgress,
                    stepsProgress = stepsProgress,
                    waterGoal = waterGoal,
                    caloriesGoal = caloriesGoal,
                    exerciseGoal = exerciseGoal,
                    stepsGoal = stepsGoal
                )

                dailyTrackingsDao.upsert(yesterdayTracking)

                // Local sync for the achievements table, because it is updated at the end of the day, today trackings will also be used in
                // the UI to keep the streaks real time
                val maxSteps = max(userTodayTrackings.stepsProgress, userAchievements.maxSteps)

                val isNextDay = ChronoUnit.DAYS.between(LocalDate.parse(lastSavedDate), LocalDate.now()) == 1L

                // Calculates max streaks before applying any missing day penalties
                val tempWaterStreak = if (waterProgress >= waterGoal) userAchievements.activeWaterStreak + 1 else 0
                val tempCaloriesStreak = if (caloriesProgress >= caloriesGoal) userAchievements.activeCaloriesStreak + 1 else 0
                val tempExerciseStreak = if (exerciseProgress >= exerciseGoal) userAchievements.activeExerciseStreak + 1 else 0
                val tempStepsStreak = if (stepsProgress >= stepsGoal) userAchievements.activeStepsStreak + 1 else 0

                val maxWaterStreak = max(tempWaterStreak, userAchievements.maxWaterStreak)
                val maxCaloriesStreak = max(tempCaloriesStreak, userAchievements.maxCaloriesStreak)
                val maxExerciseStreak = max(tempExerciseStreak, userAchievements.maxExerciseStreak)
                val maxStepsStreak = max(tempStepsStreak, userAchievements.maxStepsStreak)

                val activeWaterStreak = if (isNextDay) tempWaterStreak else 0
                val activeCaloriesStreak = if (isNextDay) tempCaloriesStreak else 0
                val activeExerciseStreak = if (isNextDay) tempExerciseStreak else 0
                val activeStepsStreak = if (isNextDay) tempStepsStreak else 0

                val updateAchievements = userAchievements.copy(
                    maxSteps = maxSteps,
                    activeWaterStreak = activeWaterStreak,
                    activeCaloriesStreak = activeCaloriesStreak,
                    activeExerciseStreak = activeExerciseStreak,
                    activeStepsStreak = activeStepsStreak,
                    maxWaterStreak = maxWaterStreak,
                    maxCaloriesStreak = maxCaloriesStreak,
                    maxExerciseStreak = maxExerciseStreak,
                    maxStepsStreak = maxStepsStreak
                )

                achievementsDao.update(updateAchievements)

                // Syncs trackings to Firestore to preserve data before the Room API reset, because the reset happens faster than the sync and
                // to also handle multi-day offline syncing, the data is stored in the worker using snapshots, needs network
                val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                val syncRequest = OneTimeWorkRequestBuilder<SyncDailyTrackingsWorker>()
                    .setConstraints(constraints)
                    .setInputData(workDataOf(
                        "snapshot_water_progress" to waterProgress,
                        "snapshot_calories_progress" to caloriesProgress,
                        "snapshot_exercise_progress" to exerciseProgress,
                        "snapshot_steps_progress" to stepsProgress,
                        "snapshot_water_goal" to waterGoal,
                        "snapshot_calories_goal" to caloriesGoal,
                        "snapshot_exercise_goal" to exerciseGoal,
                        "snapshot_steps_goal" to stepsGoal,
                        "snapshot_date" to lastSavedDate // Can't use LocalDate.now() because the function runs on a new day
                    ))
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "SyncDailyTrackingsWorker_${userSettings.lastSavedDate}", // Shared id with DailyMaintenance because they use the same worker
                    ExistingWorkPolicy.REPLACE,
                    syncRequest
                )

                val updateUserTodayTrackings = userTodayTrackings.copy(
                    waterProgress = emptyList(),
                    caloriesProgress = emptyList(),
                    exerciseProgress = emptyList(),
                    stepsProgress = 0
                )

                todayTrackingsDao.update(updateUserTodayTrackings)

                val updateUserSettings = userSettings.copy(lastSavedDate = LocalDate.now().toString())
                settingsDao.update(updateUserSettings)
            }

            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
