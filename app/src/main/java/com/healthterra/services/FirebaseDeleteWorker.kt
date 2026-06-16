package com.healthterra.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

// First Firestore delete, then Firebase Authentication delete and UI text change, the user would have to open the app again to get a new empty
// account. All subdocuments must be deleted before a parent document is, that's the reason for the complicated code. Deletes the subdocuments in
// chunks to avoid Firestore's 500 document batch limit
class FirebaseDeleteWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            val user = Firebase.auth.currentUser ?: return Result.success()

            val db = Firebase.firestore
            val uid = user.uid

            val dailyTrackingsSnapshot = db.collection("users").document(uid).collection("daily_trackings").get().await()
            val chunks = dailyTrackingsSnapshot.documents.chunked(500)

            for (chunk in chunks) {
                val batch = db.batch()
                for (document in chunk) {
                    batch.delete(document.reference)
                }

                batch.commit().await()
            }

            val parentBatch = db.batch()
            parentBatch.delete(db.collection("users").document(uid))
            parentBatch.delete(db.collection("leaderboards").document(uid))
            parentBatch.commit().await()
            user.delete().await()

            Result.success()

        }

        catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
