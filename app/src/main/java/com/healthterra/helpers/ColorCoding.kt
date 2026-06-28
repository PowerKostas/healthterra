package com.healthterra.helpers

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.healthterra.ui.themes.LocalDarkTheme

@Composable
fun colorCoding(goalsMetCount: Int, maxGoalsMetCount: Int): Color {
    val isDark = LocalDarkTheme.current

    // Surface color, but 10% lighter in dark mode and 10% darker in light mode
    val surfaceColor = if (isDark) lerp(MaterialTheme.colorScheme.surface, Color.White, 0.1f) else lerp(MaterialTheme.colorScheme.surface, Color.Black, 0.1f)

    return if (maxGoalsMetCount == 4) {
        when (goalsMetCount) {
            0 -> surfaceColor
            1 -> if (isDark) Color(0xFFB36733) else Color(0xFFDE8D54)
            2 -> if (isDark) Color(0xFFAA7E35) else Color(0xFFD4A352)
            3 -> if (isDark) Color(0xFF66823A) else Color(0xFFA8B862)
            else -> MaterialTheme.colorScheme.primary
        }
    }

    else {
        if (goalsMetCount == 0) surfaceColor else MaterialTheme.colorScheme.primary
    }
}
