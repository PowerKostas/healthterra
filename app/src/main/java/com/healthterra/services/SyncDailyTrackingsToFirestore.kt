package com.healthterra.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.healthterra.data.entities.Characteristics
import com.healthterra.data.entities.Trackings
import com.healthterra.helpers.calculateCaloriesGoal
import com.healthterra.helpers.calculateExerciseGoal
import com.healthterra.helpers.calculateStepsGoal
import com.healthterra.helpers.calculateWaterGoal
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

// The function is triggered from FirestoreSyncWorker, the daily_sync cloud function uses this data
suspend fun syncDailyTrackingsToFirestore(userTrackings: Trackings, userCharacteristics: Characteristics, waterProgress: Int? = null, caloriesProgress: Int? = null, exerciseProgress: Int? = null, stepsProgress: Int? = null, targetDate: String? = null) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val waterGoal = calculateWaterGoal(userCharacteristics)
    val caloriesGoal = calculateCaloriesGoal(userCharacteristics)
    val exerciseGoal = calculateExerciseGoal(userCharacteristics)
    val stepsGoal = calculateStepsGoal(userCharacteristics)

    // If parameters are provided, they are used, otherwise use the Room API values
    val waterProgress = waterProgress ?: userTrackings.waterProgress.sum()
    val caloriesProgress = caloriesProgress ?: userTrackings.caloriesProgress.sum()
    val exerciseProgress = exerciseProgress ?: userTrackings.exerciseProgress.sum()
    val stepsProgress = stepsProgress ?: userTrackings.stepsProgress
    val documentDate = targetDate ?: LocalDate.now().toString()

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

    try {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("daily_trackings")
            .document(documentDate)
            .set(updateData, SetOptions.merge())
            .await()
    }

    catch(e: Exception) {
        e.printStackTrace()
    }
}
