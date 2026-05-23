package com.kostas.gohealth.helpers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.kostas.gohealth.data.documents.LeaderboardEntry

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
                        userId = doc.id,
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
                    userId = snapshot.id,
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

// Same job as getTopCategoryUser, but gets the user ranked highest amongst all the categories
@Composable
fun getHealthiestUser(): State<LeaderboardEntry?> {
    return produceState(initialValue = null) {
        val db = Firebase.firestore

        val listenerRegistration = db.collection("leaderboards")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }

                // Gets all the data, of all the users
                val allUsers = snapshot.documents.mapNotNull { doc ->
                    LeaderboardEntry(
                        userId = doc.id,
                        username = doc.getString("username") ?: "",
                        profilePictureString = doc.getString("profilePictureString") ?: "",
                        waterGoalsCompleted = doc.getLong("waterGoalsCompleted") ?: 0L,
                        caloriesGoalsCompleted = doc.getLong("caloriesGoalsCompleted") ?: 0L,
                        exerciseGoalsCompleted = doc.getLong("exerciseGoalsCompleted") ?: 0L,
                        stepsGoalsCompleted = doc.getLong("stepsGoalsCompleted") ?: 0L,
                        totalSteps = doc.getLong("totalSteps") ?: 0L
                    )
                }

                // Gets a list of all the categories
                val categories = listOf(
                    LeaderboardEntry::waterGoalsCompleted,
                    LeaderboardEntry::caloriesGoalsCompleted,
                    LeaderboardEntry::exerciseGoalsCompleted,
                    LeaderboardEntry::stepsGoalsCompleted,
                    LeaderboardEntry::totalSteps
                )

                // Map that holds the accumulated rank score of each user
                val usersScore = mutableMapOf<String, Int>()
                allUsers.forEach { usersScore[it.userId] = 0 }

                // Calculates ranks for each category
                for (category in categories) {
                    val allUsersSorted = allUsers.sortedByDescending(category)

                    allUsersSorted.forEachIndexed { index, user ->
                        usersScore[user.userId] = (usersScore[user.userId] ?: 0) + index
                    }
                }

                // 4. Finds the user with the lowest overall score
                val healthiestUserId = usersScore.minBy { it.value }.key
                value = allUsers.find { it.userId == healthiestUserId }
            }

        awaitDispose {
            listenerRegistration.remove()
        }
    }
}
