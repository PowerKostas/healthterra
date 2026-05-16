package com.kostas.gohealth.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kostas.gohealth.data.DatabaseProvider
import com.kostas.gohealth.helpers.calculateCaloriesGoal
import com.kostas.gohealth.helpers.calculateExerciseGoal
import com.kostas.gohealth.helpers.calculateStepsGoal
import com.kostas.gohealth.helpers.calculateWaterGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate

// If it's a new day, it resets the trackings and settings tables. Also, the remote Firestore database is updated with the users' needed
// details and with the incremented total water, calories, exercise, steps goals completed and the total steps. If there is no network, the
// data goes in Firebase's cache, and it will eventually update the database when a connection is back up, usually when the user reopens
// the app. It also resets the trackings and settings table
class DailyMaintenanceWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val database = DatabaseProvider.getDatabase(applicationContext)
                val trackingsDao = database.trackingsDao()
                val settingsDao = database.settingsDao()

                val userTrackings = trackingsDao.getAll().first().firstOrNull()
                val userSettings = settingsDao.getAll().first().firstOrNull()
                val userCharacteristics = database.characteristicsDao().getAll().first().firstOrNull()

                // Triggers on a fresh install
                if (userTrackings == null || userSettings == null || userCharacteristics == null) {
                    return@withContext Result.success()
                }

                // If it has already reset today, it doesn't reset again
                if (LocalDate.now().toString() <= userSettings.lastSavedDate) {
                    return@withContext Result.success()
                }

                val updateUserTrackings = userTrackings.copy(
                    waterProgress = emptyList(),
                    caloriesProgress = emptyList(),
                    exerciseProgress = emptyList(),
                    stepsProgress = 0
                )

                trackingsDao.update(updateUserTrackings)

                val updateUserSettings = userSettings.copy(lastSavedDate = LocalDate.now().toString())
                settingsDao.update(updateUserSettings)

                val waterGoal = calculateWaterGoal(userCharacteristics)
                val caloriesGoal = calculateCaloriesGoal(userCharacteristics)
                val exerciseGoal = calculateExerciseGoal(userCharacteristics)
                val stepsGoal = calculateStepsGoal(userCharacteristics)

                // Because calories uses a 100 kcal buffer range
                val minCaloriesValue = caloriesGoal - 100
                val maxCaloriesValue = caloriesGoal + 100

                val waterGoalCompleted = if (userTrackings.waterProgress.sum() >= waterGoal) 1L else 0L
                val caloriesGoalCompleted = if (userTrackings.caloriesProgress.sum() in minCaloriesValue..maxCaloriesValue) 1L else 0L
                val exerciseGoalCompleted = if (userTrackings.exerciseProgress.sum() >= exerciseGoal) 1L else 0L
                val stepsGoalCompleted = if (userTrackings.stepsProgress >= stepsGoal) 1L else 0L
                val totalSteps = userTrackings.stepsProgress

                val updateData = hashMapOf(
                    "username" to (userSettings.username ?: ""),
                    "profilePictureString" to userSettings.profilePictureString,
                    "waterGoalsCompleted" to FieldValue.increment(waterGoalCompleted),
                    "caloriesGoalsCompleted" to FieldValue.increment(caloriesGoalCompleted),
                    "exerciseGoalsCompleted" to FieldValue.increment(exerciseGoalCompleted),
                    "stepsGoalsCompleted" to FieldValue.increment(stepsGoalCompleted),
                    "totalSteps" to FieldValue.increment(totalSteps.toLong())
                )

                FirebaseFirestore.getInstance().collection("leaderboards")
                    .document(Firebase.auth.currentUser?.uid ?: "")
                    .set(updateData, SetOptions.merge())

                Result.success()
            }

            catch (e: Exception) {
                e.printStackTrace()
                Result.retry()
            }
        }
    }
}
