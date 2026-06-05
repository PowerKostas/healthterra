package com.kostas.gohealth.helpers

import java.security.SecureRandom

private val random = SecureRandom()

fun generateRandomUsername(): String {
    val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    var username = "GoHealth-"

    for (i in 0 until 6) { // Max username length is 15, 15 - len(GoHealth-) = 6
        username += characters[random.nextInt(characters.length)]
    }

    return username
}

fun generateRandomProfilePictureString(): String {
    val profilePictureStrings = listOf(
        "alien", "cat", "dinosaur", "dog", "dolphin", "dragon", "duck", "eagle", "elephant", "flamingo",
        "horse", "lion", "monkey", "mouse", "octopus", "panda", "penguin", "pig", "sheep", "snail", "turtle"
    )

    return profilePictureStrings[random.nextInt(profilePictureStrings.size)]
}
