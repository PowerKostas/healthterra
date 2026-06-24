package com.healthterra.services

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.healthterra.data.documents.HealthiestUser
import com.healthterra.data.documents.LeaderboardEntry

@Composable
// Sets up listeners in the Firestore database to continuously fetch the highest-scoring users and their details for
// the water, calories, exercise, and steps goals, doesn't work on the background, only when the screen is active
fun getCategoryTopUsers(category: String, limitCount: Long): State<List<LeaderboardEntry>> {
    return produceState(initialValue = emptyList(), key1 = category) {
        val db = Firebase.firestore

        val listenerRegistration = db.collection("leaderboards")
            .orderBy(category, Query.Direction.DESCENDING)
            .limit(limitCount)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }

                value = snapshot.documents.mapNotNull { doc ->
                    LeaderboardEntry(
                        uid = doc.id,
                        username = doc.getString("username") ?: "",
                        profilePictureString = doc.getString("profilePictureString") ?: "",
                        waterGoalsCompleted = doc.getLong("waterGoalsCompleted") ?: 0L,
                        caloriesGoalsCompleted = doc.getLong("caloriesGoalsCompleted") ?: 0L,
                        exerciseGoalsCompleted = doc.getLong("exerciseGoalsCompleted") ?: 0L,
                        stepsGoalsCompleted = doc.getLong("stepsGoalsCompleted") ?: 0L,
                        totalSteps = doc.getLong("totalSteps") ?: 0L
                    )
                }
            }

        awaitDispose {
            listenerRegistration.remove()
        }
    }
}

// Same job as getTopCategoryUser, but gets the actual user's data
@Composable
fun getCurrentUser(userId: String?): State<LeaderboardEntry?> {
    return produceState(initialValue = null, key1 = userId) {
        if (userId == null) {
            value = null
            return@produceState
        }

        val db = Firebase.firestore

        val listenerRegistration = db.collection("leaderboards")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }

                value = LeaderboardEntry(
                    uid = snapshot.id,
                    username = snapshot.getString("username") ?: "",
                    profilePictureString = snapshot.getString("profilePictureString") ?: "",
                    waterGoalsCompleted = snapshot.getLong("waterGoalsCompleted") ?: 0L,
                    caloriesGoalsCompleted = snapshot.getLong("caloriesGoalsCompleted") ?: 0L,
                    exerciseGoalsCompleted = snapshot.getLong("exerciseGoalsCompleted") ?: 0L,
                    stepsGoalsCompleted = snapshot.getLong("stepsGoalsCompleted") ?: 0L,
                    totalSteps = snapshot.getLong("totalSteps") ?: 0L
                )
            }

        awaitDispose {
            listenerRegistration.remove()
        }
    }
}

// Same job as getTopCategoryUser, but gets the healthiest user
@Composable
fun getHealthiestUser(): State<HealthiestUser?> {
    return produceState(initialValue = null) {
        val db = Firebase.firestore

        val listenerRegistration = db.collection("app_state").document("healthiest_user")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    value = null
                    return@addSnapshotListener
                }

                value = HealthiestUser(
                    uid = snapshot.getString("uid") ?: "",
                    healthiestUserScore = snapshot.getLong("healthiestUserScore") ?: 0L,
                    profilePictureString = snapshot.getString("profilePictureString") ?: "",
                    username = snapshot.getString("username") ?: ""
                )
            }

        awaitDispose {
            listenerRegistration.remove()
        }
    }
}
