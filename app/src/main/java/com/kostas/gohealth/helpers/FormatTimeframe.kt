package com.kostas.gohealth.helpers

import kotlin.math.roundToInt

fun formatTimeframe(totalDays: Int): String {
    // Anything 21 days or under shows as weeks (2, 3 weeks)
    return if (totalDays <= 21) {
        val weeks = totalDays / 7
        "$weeks week${if (weeks > 1) "s" else ""}"
    }

    // Anything over 21 days converts to nearest whole month
    else {
        val months = (totalDays / 30.0).roundToInt()
        "$months month${if (months > 1) "s" else ""}"
    }
}
