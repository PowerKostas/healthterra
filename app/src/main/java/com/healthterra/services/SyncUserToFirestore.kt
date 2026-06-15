package com.healthterra.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.healthterra.data.entities.Characteristics
import com.healthterra.data.entities.Settings
import kotlinx.coroutines.tasks.await

// The function is triggered from FirestoreSyncWorker
suspend fun syncUserToFirestore(userCharacteristics: Characteristics, userSettings: Settings) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

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

    try {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(userDataMap, SetOptions.merge())
            .await()
    }

    catch(e: Exception) {
        e.printStackTrace()
    }
}
