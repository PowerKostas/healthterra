package com.healthterra.ui.components.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ProgressHeader(color: Color, completedGoals: Int, allGoals: Int) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
        .background(MaterialTheme.colorScheme.primary)
        .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Today's",
                style = MaterialTheme.typography.headlineSmall,
                color = color.copy(alpha = 0.8f)
            )
            Text(
                text = "Progress",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = if (completedGoals == allGoals) {
                    "All goals completed! \uD83C\uDFC6"
                }

                else {
                    "${allGoals - completedGoals} ${if (allGoals - completedGoals == 1) "goal" else "goals"} remaining!"
                },

                style = MaterialTheme.typography.labelLarge,
                color = color.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { completedGoals / allGoals.toFloat() },
                modifier = Modifier.size(100.dp),
                color = color,
                strokeWidth = 8.dp,
                strokeCap = StrokeCap.Butt,
                gapSize = 0.dp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$completedGoals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )

                Text(
                    text = "of $allGoals",
                    style = MaterialTheme.typography.labelLarge,
                    color = color.copy(alpha = 0.8f)
                )
            }
        }
    }
}
