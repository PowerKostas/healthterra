package com.healthterra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthterra.helpers.getAchievementsData
import com.healthterra.ui.components.screen.AchievementItem
import com.healthterra.ui.viewModels.AchievementsViewModel
import com.healthterra.ui.viewModels.SettingsViewModel

@Composable
fun AchievementsScreen() {
    val achievementsViewModel = viewModel<AchievementsViewModel>(factory = AchievementsViewModel.Factory)
    val otherAchievementsState by achievementsViewModel.otherAchievements.collectAsState()
    val otherAchievements = otherAchievementsState ?: return

    val leaderboardsAchievementsList by achievementsViewModel.leaderboardsAchievements.collectAsState()
    val leaderboardsAchievements = leaderboardsAchievementsList.firstOrNull() ?: return

    val settingsViewModel = viewModel<SettingsViewModel>(factory = SettingsViewModel.Factory)
    val userSettingsList by settingsViewModel.settings.collectAsState()
    val userSettings = userSettingsList.firstOrNull() ?: return

    val achievementsData = getAchievementsData(otherAchievements, leaderboardsAchievements, userSettings.username)

    val totalCommon = achievementsData.common.size
    val totalRare = achievementsData.rare.size
    val totalEpic = achievementsData.epic.size
    val totalLegendary = achievementsData.legendary.size
    val totalImpossible = achievementsData.impossible.size
    val totalOptionalVisible = achievementsData.optional.count { it.isUnlocked }
    val totalAchievementsCollected = achievementsData.flatList.count { it.isUnlocked }
    val totalAchievements = totalCommon + totalRare + totalEpic + totalLegendary + totalImpossible + totalOptionalVisible

    // Because normal Arrangement.SpaceBetween doesn't offer minimum spacing between items, a custom one is created. A 12dp horizontal padding
    // between items is only applied if everything fits on the screen, otherwise SpaceBetween is applied
    val spaceBetweenWithMinSpacing = object : Arrangement.Horizontal {
        override val spacing = 12.dp

        override fun Density.arrange(
            totalSize: Int,
            sizes: IntArray,
            layoutDirection: LayoutDirection,
            outPositions: IntArray
        ) {
            with(Arrangement.SpaceBetween) {
                arrange(totalSize, sizes, layoutDirection, outPositions)
            }
        }
    }


    // Uses a box with constraints to vertically space around the achievements in the scrollable column
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val minScrollHeight = maxHeight

        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = minScrollHeight)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Collected: $totalAchievementsCollected / $totalAchievements",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(32.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface)
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Common: ${achievementsData.common.count { it.isUnlocked }} / $totalCommon",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = spaceBetweenWithMinSpacing
                ) {
                    items(achievementsData.common) { item -> AchievementItem(item) }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Rare: ${achievementsData.rare.count { it.isUnlocked }} / $totalRare",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = spaceBetweenWithMinSpacing
                ) {
                    items(achievementsData.rare) { item -> AchievementItem(item) }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Epic: ${achievementsData.epic.count { it.isUnlocked }} / $totalEpic",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = spaceBetweenWithMinSpacing
                ) {
                    items(achievementsData.epic) { item -> AchievementItem(item) }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Legendary: ${achievementsData.legendary.count { it.isUnlocked } + totalOptionalVisible} / ${totalLegendary + totalOptionalVisible}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = spaceBetweenWithMinSpacing
                ) {
                    items(achievementsData.legendary) { item -> AchievementItem(item) }

                    val unlockedOptional = achievementsData.optional.filter { it.isUnlocked }
                    items(unlockedOptional) { item -> AchievementItem(item) }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Impossible: ${achievementsData.impossible.count { it.isUnlocked }} / $totalImpossible",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = spaceBetweenWithMinSpacing
                ) {
                    items(achievementsData.impossible) { item -> AchievementItem(item) }
                }
            }
        }
    }
}
