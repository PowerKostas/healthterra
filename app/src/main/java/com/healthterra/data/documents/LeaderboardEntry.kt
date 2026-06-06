package com.healthterra.data.documents

// For the remote Firestore database
data class LeaderboardEntry(
    val userId: String = "",
    val username: String = "",
    val profilePictureString: String = "",
    val waterGoalsCompleted: Long = 0L,
    val caloriesGoalsCompleted: Long = 0L,
    val exerciseGoalsCompleted: Long = 0L,
    val stepsGoalsCompleted: Long = 0L,
    val totalSteps: Long = 0L
)
