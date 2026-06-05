package com.kostas.gohealth.ui.components.central

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kostas.gohealth.R
import com.kostas.gohealth.helpers.calculateCaloriesGoal
import com.kostas.gohealth.helpers.calculateExerciseGoal
import com.kostas.gohealth.helpers.calculateStepsGoal
import com.kostas.gohealth.helpers.calculateWaterGoal
import com.kostas.gohealth.ui.screens.CategoriesScreen
import com.kostas.gohealth.ui.screens.HomeScreen
import com.kostas.gohealth.ui.screens.LeaderboardsScreen
import com.kostas.gohealth.ui.screens.ProfileScreen
import com.kostas.gohealth.ui.screens.StepsScreen
import com.kostas.gohealth.ui.viewModels.CharacteristicsViewModel
import com.kostas.gohealth.ui.viewModels.TrackingsViewModel
import kotlinx.coroutines.launch

// Drawer menu is the central screen of the app. It has the Scaffold, the top bar that remains static and is in charge of switching the
// different screens
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerMenu() {
    // Initiates the database
    val trackingsViewModel = viewModel<TrackingsViewModel>(factory = TrackingsViewModel.Factory)
    val userTrackingsList by trackingsViewModel.trackings.collectAsState()
    val userTrackings = userTrackingsList.firstOrNull()

    val characteristicsViewModel = viewModel<CharacteristicsViewModel>(factory = CharacteristicsViewModel.Factory)
    val userCharacteristicsList by characteristicsViewModel.characteristics.collectAsState()
    val userCharacteristics = userCharacteristicsList.firstOrNull()

    // drawerState is used to handle the opening and closing of the menu, scope is for the opening and closing animation. Because this is the
    // central screen, this is where the main focus manager goes
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // NavController is used for navigating between the screens
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentScreen = navBackStackEntry?.destination?.route ?: "Home"

    // Helper function for navigating from the drawer to specific screens
    val navigateToScreen = { screen: String ->
        // Only navigate if the selected screen is different from the current one
        if (currentScreen != screen) {
            navController.navigate(screen) {
                launchSingleTop = true // Avoids multiple copies of the same destination
                restoreState = true // Restores state when re-selecting a previously selected item
            }
        }

        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen, // Disables swipe gestures to open the drawer menu, but can still tap out of it when already opened

        drawerContent = {
            ModalDrawerSheet (
                drawerContainerColor = MaterialTheme.colorScheme.surface,

                // Make the drawer menu take 70% of the screen
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // App logo is put in a box with a fixed height, so the horizontal divider here and in the screens match heights
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .height(64.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = Color(0xFF059669))) { append("Health") }
                                withStyle(style = SpanStyle(color = Color(0xFF55403E))) { append("terra") }
                            },

                            fontSize = 28.sp,
                            fontFamily = FontFamily(Font(R.font.fredoka_bold))
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface)

                    DrawerMenuItem(
                        appIcon = R.drawable.home,
                        title = "Home",
                        currentScreen = currentScreen,
                        onItemClick = { navigateToScreen("Home") }
                    )

                    DrawerMenuItem(
                        appIcon = R.drawable.water,
                        title = "Water",
                        currentScreen = currentScreen,
                        onItemClick = { navigateToScreen("Water") }
                    )

                    DrawerMenuItem(
                        appIcon = R.drawable.calories,
                        title = "Calories",
                        currentScreen = currentScreen,
                        onItemClick = { navigateToScreen("Calories") }
                    )

                    DrawerMenuItem(
                        appIcon = R.drawable.exercise,
                        title = "Exercise",
                        currentScreen = currentScreen,
                        onItemClick = { navigateToScreen("Exercise") }
                    )

                    DrawerMenuItem(
                        appIcon = R.drawable.steps,
                        title = "Steps",
                        currentScreen = currentScreen,
                        onItemClick = { navigateToScreen("Steps") }
                    )

                    DrawerMenuItem(
                        appIcon = R.drawable.profile,
                        title = "Profile",
                        currentScreen = currentScreen,
                        onItemClick = { navigateToScreen("Profile") }
                    )

                    DrawerMenuItem(
                        appIcon = R.drawable.leaderboards,
                        title = "Leaderboards",
                        currentScreen = currentScreen,
                        onItemClick = { navigateToScreen("Leaderboards") }
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() })},

            topBar = {
                TopBar(
                    title = currentScreen,
                    onMenuClick = {
                        focusManager.clearFocus() // To close the keyboard, if the user is typing when clicking the button
                        scope.launch { drawerState.open() }
                    },

                    onLogoClick = {
                        focusManager.clearFocus()
                        navigateToScreen("Home")
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = "Home",
                    enterTransition = { fadeIn(animationSpec = tween(0)) },
                    exitTransition = { fadeOut(animationSpec = tween(0)) },
                    popEnterTransition = { fadeIn(animationSpec = tween(0)) },
                    popExitTransition = { fadeOut(animationSpec = tween(0)) }
                ) {
                    composable("Home") {
                        HomeScreen(
                            onNavigate = { destinationScreen ->
                                navController.navigate(destinationScreen)
                            }
                        )
                    }

                    composable("Water") {
                        CategoriesScreen("Water", R.drawable.water, Color(0xFF2196F3), userTrackings?.waterProgress?.sum() ?: 0, calculateWaterGoal(userCharacteristics), "mL", listOf(R.drawable.water_low, R.drawable.water_full, R.drawable.water_bottle, R.drawable.water_bottle_large), listOf("+100mL", "+280mL", "+500mL", "+1000mL"), 12.sp)
                    }

                    composable("Calories") {
                        CategoriesScreen("Calories", R.drawable.calories, Color(0xFF8B4513), userTrackings?.caloriesProgress?.sum() ?: 0, calculateCaloriesGoal(userCharacteristics), "kcal", null, listOf("+10", "+100", "+1000"), 16.sp)
                    }

                    composable("Exercise") {
                        CategoriesScreen("Exercise", R.drawable.exercise, Color.Black, userTrackings?.exerciseProgress?.sum() ?: 0, calculateExerciseGoal(userCharacteristics), "reps", null, listOf("+1", "+5", "+10"), 16.sp)
                    }

                    composable("Steps") {
                        StepsScreen(userTrackings?.stepsProgress ?: 0, calculateStepsGoal(userCharacteristics))
                    }

                    composable("Profile") {
                        ProfileScreen()
                    }

                    composable("Leaderboards") {
                        LeaderboardsScreen()
                    }
                }
            }
        }
    }
}
