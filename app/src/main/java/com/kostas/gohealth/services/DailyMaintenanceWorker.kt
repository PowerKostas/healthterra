package com.kostas.gohealth.services

import android.content.Context
import android.content.Intent
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
import com.kostas.gohealth.helpers.roundGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate
import kotlin.math.roundToInt

// Because of Mutex, if multiple triggers fire simultaneously (onResume and performDailyMaintenanceAppActive for example), duplicates are
// put on hold, preventing race conditions
private val mutex = Mutex()

// If it's a new day, it resets the trackings and settings tables. Also, the remote Firestore database is updated with the users' needed
// details and with the incremented total water, calories, exercise, steps goals completed and the total steps. If there is no network, the
// data goes in Firebase's cache, and it will eventually update the database when a connection is back up, usually when the user reopens
// the app. It also resets the trackings and settings table
suspend fun performDailyMaintenance(context: Context) {
    mutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                val database = DatabaseProvider.getDatabase(context)
                val trackingsDao = database.trackingsDao()
                val settingsDao = database.settingsDao()

                val userTrackings = trackingsDao.getAll().first().firstOrNull()
                val userSettings = settingsDao.getAll().first().firstOrNull()
                val userCharacteristics = database.characteristicsDao().getAll().first().firstOrNull()

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

                // Calories use a 10% of the goal kcal buffer range
                val minCaloriesValue = roundGoal((caloriesGoal - caloriesGoal * 0.1).roundToInt())
                val maxCaloriesValue = roundGoal((caloriesGoal + caloriesGoal * 0.1).roundToInt())

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

                // .await() ensures the worker stays alive until the background write is completed and the 3-second timeout prevents infinite
                // hangs when offline. If offline, Firestore caches the update locally and will automatically sync it to the cloud later
                withTimeoutOrNull(3000L) {
                    FirebaseFirestore.getInstance().collection("leaderboards")
                        .document(Firebase.auth.currentUser?.uid ?: "")
                        .set(updateData, SetOptions.merge())
                        .await()
                }
            }

            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
