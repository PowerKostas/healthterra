package com.healthterra.ui.components.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.healthterra.helpers.roundGoal
import com.healthterra.ui.components.general.ProgressBar
import kotlin.math.roundToInt

@Composable
fun ProgressBox(modifier: Modifier = Modifier, iconId: Int, category: String, progressBarColour: Color, progress: Int, goal: Int, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(
                2.dp,
                MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(16.dp)
            )

            .clickable(
                onClick = { onClick() }
            )

            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = category,
                tint = Color.Unspecified,
                modifier = Modifier.size(80.dp)
            )

            Text(
                text = category,
                style = MaterialTheme.typography.labelLarge
            )

            val minCaloriesValue = roundGoal((goal - goal * 0.1).roundToInt())
            val maxCaloriesValue = roundGoal((goal + goal * 0.1).roundToInt())

            // Calculates percentage from the minimum value, not the average
            val progressPercentage = if (category == "Calories") {
                (progress.toFloat() / minCaloriesValue).coerceAtMost(1.0f)
            }

            else {
                (progress.toFloat() / goal).coerceAtMost(1.0f)
            }

            ProgressBar(12.dp, progressBarColour, progressPercentage)

            // Special message if the user passes the calories range
            if (category == "Calories" && progress > maxCaloriesValue) {
                Text(
                    text = "Calories Exceeded!",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFE53935)
                )
            }

            else {
                Text(
                    text = "${"%.1f".format(progressPercentage * 100)}%", // Percentage, out of 100, rounded to 1 decimal place
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
