package com.healthterra.helpers

// For a better view, it rounds the goal
fun roundGoal(categoryGoal: Int): Int {
    val remainder = categoryGoal % 10
    val roundedCategoryGoal = when {
        remainder < 5 -> categoryGoal - remainder
        remainder == 5 -> categoryGoal
        else -> categoryGoal + (10 - remainder)
    }

    return roundedCategoryGoal
}
