package com.healthterra.services

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.time.LocalDate

fun createInitialUserDocument(uid: String, username: String, profilePictureString: String) {
    val initialUserData = mapOf(
        "activityLevel" to null,
        "age" to null,
        "appearance" to "Light",
        "daysGoal" to 0,
        "gender" to null,
        "height" to null,
        "initialWeightGoalDate" to null,
        "kgGoal" to 0,
        "lastSavedDate" to LocalDate.now().toString(),
        "profilePictureString" to profilePictureString,
        "stepTracking" to "Enabled",
        "username" to username,
        "weight" to null,
        "weightGoal" to "Maintain",
        "leaderboardsVisibility" to "Anonymous",

        "achievements" to mapOf(
            "maxSteps" to 0,
            "activeWaterStreak" to 0,
            "activeCaloriesStreak" to 0,
            "activeExerciseStreak" to 0,
            "activeStepsStreak" to 0,
            "maxWaterStreak" to 0,
            "maxCaloriesStreak" to 0,
            "maxExerciseStreak" to 0,
            "maxStepsStreak" to 0,
            "appearedOnWaterLeaderboards" to false,
            "appearedOnCaloriesLeaderboards" to false,
            "appearedOnExerciseLeaderboards" to false,
            "appearedOnStepsLeaderboards" to false,
            "appearedOnTotalStepsLeaderboards" to false,
            "appearedOnHealthiestUser" to false,
            "secret" to false,
            "earlyPlaytester" to false
        )
    )

    FirebaseFirestore.getInstance()
        .collection("users")
        .document(uid)
        .set(initialUserData, SetOptions.merge())
}
