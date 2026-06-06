package com.healthterra.helpers

import java.security.SecureRandom

private val random = SecureRandom()

fun generateRandomUsername(): String {
    val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    var username = "Terra-"

    for (i in 0 until 15 - username.length) { // Max username length is 15, 15 - len(username) = X Available characters
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
