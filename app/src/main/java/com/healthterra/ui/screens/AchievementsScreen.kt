package com.healthterra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.FollowTheSigns
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Blind
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Moving
import androidx.compose.material.icons.filled.NaturePeople
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NordicWalking
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Paragliding
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shower
import androidx.compose.material.icons.filled.ShutterSpeed
import androidx.compose.material.icons.filled.Snowshoeing
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.StackedLineChart
import androidx.compose.material.icons.filled.StarPurple500
import androidx.compose.material.icons.filled.Tsunami
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthterra.ui.components.screen.AchievementItem

@Composable
fun AchievementsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = "Collected: 0 / 47",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(32.dp)
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface)
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Common: 0 / 10",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { AchievementItem(Icons.Filled.Shower, "Sipper", "10 Water Goals Completed", "Common", false) }
                    item { AchievementItem(Icons.Filled.LocalDining, "Mindful Eater", "10 Calorie Goals Completed", "Common", true) }
                    item { AchievementItem(Icons.AutoMirrored.Filled.DirectionsBike, "The Active One", "10 Exercise Goals Completed", "Common", false) }
                    item { AchievementItem(Icons.AutoMirrored.Filled.DirectionsRun, "Foot Soldier", "10 Step Goals Completed", "Common", false) }
                    item { AchievementItem(Icons.Filled.Explore, "Wanderer", "100000 Steps Made", "Common", false) }
                    item { AchievementItem(Icons.Filled.LocalFlorist, "Well Watered", "5-Day Water Goal Streak", "Common", false) }
                    item { AchievementItem(Icons.Filled.Balance, "Dietitian", "5-Day Calorie Goal Streak", "Common", false) }
                    item { AchievementItem(Icons.Filled.WbSunny, "Early Riser", "5-Day Exercise Goal Streak", "Common", true) }
                    item { AchievementItem(Icons.Filled.Moving, "Pacer", "5-Day Step Goal Streak", "Common", false) }
                    item { AchievementItem(Icons.Filled.NaturePeople, "Phew...", "10000 Steps in a Day", "Common", false) }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Rare: 0 / 10",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { AchievementItem(Icons.Filled.LocalCafe, "Water Regular", "100 Water Goals Completed", "Rare", false) }
                    item { AchievementItem(Icons.Filled.SoupKitchen, "Nutritionist", "100 Calorie Goals Completed", "Rare", true) }
                    item { AchievementItem(Icons.Filled.SportsTennis, "Athlete", "100 Exercise Goals Completed", "Rare", true) }
                    item { AchievementItem(Icons.Filled.Blind, "Pathfinder", "100 Step Goals Completed", "Rare", false) }
                    item { AchievementItem(Icons.Filled.Navigation, "Voyager", "1000000 Steps Made", "Rare", false) }
                    item { AchievementItem(Icons.Filled.Spa, "Oasis", "50-Day Water Goal Streak", "Rare", false) }
                    item { AchievementItem(Icons.Filled.Whatshot, "Metabolizer", "50-Day Calorie Goal Streak", "Rare", false) }
                    item { AchievementItem(Icons.Filled.MonitorHeart, "Hard-Wired", "50-Day Exercise Goal Streak", "Rare", false) }
                    item { AchievementItem(Icons.Filled.StackedLineChart, "Marathoner", "50-Day Step Goal Streak", "Rare", false) }
                    item { AchievementItem(Icons.AutoMirrored.Filled.FollowTheSigns, "Road Warrior", "20000 Steps in a Day", "Rare", false) }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Epic: 0 / 10",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { AchievementItem(Icons.Filled.Waves, "Reservoir", "365 Water Goals Completed", "Epic", true) }
                    item { AchievementItem(Icons.Filled.OutdoorGrill, "Master Chef", "365 Calorie Goals Completed", "Epic", true) }
                    item { AchievementItem(Icons.Filled.Power, "Power House", "365 Exercise Goals Completed", "Epic", false) }
                    item { AchievementItem(Icons.Filled.NordicWalking, "Trailblazer", "365 Step Goals Completed", "Epic", false) }
                    item { AchievementItem(Icons.Filled.Language, "Globetrotter", "5000000 Steps Made", "Epic", true) }
                    item { AchievementItem(Icons.Filled.Pool, "Tidal Force", "100-Day Water Goal Streak", "Epic", false) }
                    item { AchievementItem(Icons.Filled.LocalFireDepartment, "Metabolic Monster", "100-Day Calorie Goal Streak", "Epic", false) }
                    item { AchievementItem(Icons.Filled.Security, "Ironclad", "100-Day Exercise Goal Streak", "Epic", false) }
                    item { AchievementItem(Icons.Filled.Landscape, "Scout", "100-Day Step Goal Streak", "Epic", false) }
                    item { AchievementItem(Icons.Filled.Snowshoeing, "Golden Legs", "50000 Steps in a Day", "Epic", false) }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Legendary: 0 / 7",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { AchievementItem(Icons.Filled.Anchor, "Hall of Famer", "Appeared on Water Leaderboards", "Legendary", false) }
                    item { AchievementItem(Icons.Filled.Apartment, "Household Name", "Appeared on Calories Leaderboards", "Legendary", true) }
                    item { AchievementItem(Icons.Filled.StarPurple500, "Superstar", "Appeared on Exercise Leaderboards", "Legendary", true) }
                    item { AchievementItem(Icons.Filled.WorkspacePremium, "Record Holder", "Appeared on Steps Leaderboards", "Legendary", false) }
                    item { AchievementItem(Icons.Filled.Public, "Healthterra's Finest", "Crowned \"Healthiest User\"", "Legendary", true) }
                    item { AchievementItem(Icons.Filled.QuestionMark, "Secret", "???", "Legendary", false) }
                    item { AchievementItem(Icons.Filled.Science, "Early Playtester", "We couldn't have done it without you!", "Legendary", true) }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Impossible: 0 / 10",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { AchievementItem(Icons.Filled.Tsunami, "Leviathan", "1000 Water Goals Completed", "Impossible", true) }
                    item { AchievementItem(Icons.AutoMirrored.Filled.MenuBook, "Alchemist", "1000 Calorie Goals Completed", "Impossible", false) }
                    item { AchievementItem(Icons.Filled.SportsMma, "Juggernaut", "1000 Exercise Goals Completed", "Impossible", true) }
                    item { AchievementItem(Icons.Filled.Paragliding, "Griffin", "1000 Step Goals Completed", "Impossible", false) }
                    item { AchievementItem(Icons.Filled.SportsGymnastics, "Behemoth", "10000000 Steps Made", "Impossible", false) }
                    item { AchievementItem(Icons.Filled.Sailing, "Hydra", "365-Day Water Goal Streak", "Impossible", false) }
                    item { AchievementItem(Icons.Filled.ElectricBolt, "Phoenix", "365-Day Calorie Goal Streak", "Impossible", false) }
                    item { AchievementItem(Icons.Filled.ShutterSpeed, "Golem", "365-Day Exercise Goal Streak", "Impossible", false) }
                    item { AchievementItem(Icons.Filled.AllInclusive, "Minotaur", "365-Day Step Goal Streak", "Impossible", false) }
                    item { AchievementItem(Icons.Filled.RocketLaunch, "How...", "100000 Steps in a Day", "Impossible", false) }
                }
            }
        }
    }
}
