package com.healthterra.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.healthterra.data.UserDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

// The function is triggered from SettingsViewModel and MainActivity.onStop, profilePictureString and username are for the leaderboards, the
// others are for backup
class SyncUserWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.success()

        return try {
            val database = UserDatabase.getDatabase(applicationContext)
            val userSettings = database.settingsDao().getAll().first().firstOrNull()
            userSettings ?: return Result.success()

            val updateData = mapOf(
                "profilePictureString" to userSettings.profilePictureString,
                "username" to userSettings.username,
                "appearance" to userSettings.appearance,
                "stepTracking" to userSettings.stepTracking,
                "lastSavedDate" to userSettings.lastSavedDate,
                "initialWeightGoalDate" to userSettings.initialWeightGoalDate
            )

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
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
