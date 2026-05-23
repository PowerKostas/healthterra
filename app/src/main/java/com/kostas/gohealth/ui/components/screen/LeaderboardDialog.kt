package com.kostas.gohealth.ui.components.screen

import android.icu.text.CompactDecimalFormat
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kostas.gohealth.data.documents.LeaderboardEntry
import java.util.Locale

@Composable
fun LeaderboardDialog(categoryString: String, categoryLeaderboard: List<LeaderboardEntry>, avatarMap: Map<String, Int>, onDismiss: () -> Unit) {
    val correctCategoryString = when (categoryString) {
        "waterGoalsCompleted" -> "Daily Water Goals Completed"
        "caloriesGoalsCompleted" -> " Daily Calories Goals Completed"
        "exerciseGoalsCompleted" -> "Daily Exercise Goals Completed"
        "stepsGoalsCompleted" -> "Daily Steps Goals Completed"
        "totalSteps" -> "Total Steps"
        else -> ""
    }

    if (!categoryLeaderboard.isEmpty()) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    // The dialog takes 90% of the screen size
                    .fillMaxWidth(0.9f)
                    .heightIn(max = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() } * 0.9f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 24.dp)
                ) {
                    Text(
                        text = correctCategoryString,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD4AF37),
                        textAlign = TextAlign.Center,
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        itemsIndexed(categoryLeaderboard) { index, user -> // Loops through the leaderboard and gets index and user
                            val score = when (categoryString) {
                                "waterGoalsCompleted" -> user.waterGoalsCompleted
                                "caloriesGoalsCompleted" -> user.caloriesGoalsCompleted
                                "exerciseGoalsCompleted" -> user.exerciseGoalsCompleted
                                "stepsGoalsCompleted" -> user.stepsGoalsCompleted
                                "totalSteps" -> user.totalSteps
                                else -> 0
                            }

                            val rowColor = when (index) {
                                0 -> Color(0xFFE5C100) // Gold
                                1 -> Color(0xFFC0C0C0) // Silver
                                2 -> Color(0xFFCD7F32) // Bronze
                                else -> MaterialTheme.colorScheme.onSurface // Default color for everyone else
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${
                                        (index + 1).toString().padStart(2, '0')
                                    }", // Turns "1" to "01"
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = rowColor
                                )

                                Icon(
                                    painter = painterResource(id = avatarMap.getValue(user.profilePictureString)),
                                    contentDescription = "Leaderboard Icon",
                                    tint = Color.Unspecified,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .border(
                                            width = 1.dp,
                                            color = rowColor,
                                            shape = CircleShape
                                        )
                                )

                                Text(
                                    text = user.username,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                // Turns "173938" to "173.9K"
                                val formatter = CompactDecimalFormat.getInstance(
                                    Locale.getDefault(),
                                    CompactDecimalFormat.CompactStyle.SHORT
                                ).apply {
                                    maximumFractionDigits = 1
                                }

                                Text(
                                    text = formatter.format(score),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD4AF37)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
