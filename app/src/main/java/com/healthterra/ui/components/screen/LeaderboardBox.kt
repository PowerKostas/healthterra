package com.healthterra.ui.components.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.healthterra.ui.components.general.CustomSurface

@Composable
fun LeaderboardBox(modifier: Modifier, profilePictureId: Int, username: String, categoryId: Int, categoryName: String, score: String, currentUserScore: String?) {
    CustomSurface(modifier, topPadding = 8.dp, bottomPadding = 8.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 0.dp)
            ) {
                AsyncImage(
                    model = profilePictureId,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(72.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            shape = CircleShape
                        )
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gets all the available space
                        Text(
                            text = username,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Adjusts the text's size, if it doesn't fit the screen
                        BasicText(
                            text = score,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD4AF37),
                                textAlign = TextAlign.End
                            ),

                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = 1.sp,
                                maxFontSize = 24.sp
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = categoryId),
                                contentDescription = "Category",
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .size(24.dp)
                                    .offset(x = (-4).dp)
                            )

                            Text(
                                text = categoryName,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }

                        // Doesn't need to draw the "Mine: X" text, if the top user is the current user
                        if (currentUserScore != null) {
                            Text(
                                text = "Mine: $currentUserScore",
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Arrow Down",
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .size(24.dp)
            )
        }
    }
}
