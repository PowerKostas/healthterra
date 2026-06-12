package com.healthterra.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.time.LocalDate

// The function is triggered from onStop, it syncs the daily trackings to Firestore, to use the data for the daily_sync cloud function
fun syncDailyTrackingsToFirestore(waterProgress: Int?, caloriesProgress: Int?, exerciseProgress: Int?, stepsProgress: Int?, waterGoal: Int, caloriesGoal: Int, exerciseGoal: Int, stepsGoal: Int) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    // Creates a map with items like "waterProgress": 720
    val updateData = mutableMapOf<String, Any>()
    waterProgress?.let { updateData["waterProgress"] = it }
    caloriesProgress?.let { updateData["caloriesProgress"] = it }
    exerciseProgress?.let { updateData["exerciseProgress"] = it }
    stepsProgress?.let { updateData["stepsProgress"] = it }

    if (updateData.isEmpty()) return

    updateData["waterGoal"] = waterGoal
    updateData["caloriesGoal"] = caloriesGoal
    updateData["exerciseGoal"] = exerciseGoal
    updateData["stepsGoal"] = stepsGoal

    try {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("daily_trackings")
            .document(LocalDate.now().toString())
            .set(updateData, SetOptions.merge())
    }

    catch(e: Exception) {
        e.printStackTrace()
    }
}
