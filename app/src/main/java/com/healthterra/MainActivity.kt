package com.healthterra

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.Firebase
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.initialize
import com.healthterra.services.NotificationWorker
import com.healthterra.services.StepTrackerService
import com.healthterra.services.SyncUserWorker
import com.healthterra.services.performDailyMaintenance
import com.healthterra.ui.components.central.DrawerMenu
import com.healthterra.ui.themes.HealthterraTheme
import com.healthterra.ui.viewModels.CharacteristicsViewModel
import com.healthterra.ui.viewModels.SettingsViewModel
import com.healthterra.ui.viewModels.TrackingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

// This is where the program starts, sets basic settings and runs the custom drawer menu function, which is the center of the app
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels { SettingsViewModel.Factory }
    private val characteristicsViewModel: CharacteristicsViewModel by viewModels { CharacteristicsViewModel.Factory }
    private val trackingsViewModel: TrackingsViewModel by viewModels { TrackingsViewModel.Factory }

    // On first time open, the code doesn't wait for user input on the permissions dialog and the foreground service doesn't have the
    // permissions to run, to fix this, foreground service runs from here too
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val activityRecognitionGranted = permissions[Manifest.permission.ACTIVITY_RECOGNITION] ?: false
            if (activityRecognitionGranted) {
                val serviceIntent = Intent(this, StepTrackerService::class.java)
                StepTrackerService.isForegroundServiceActive = true
                startForegroundService(serviceIntent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            val userSettingsList by settingsViewModel.settings.collectAsState()
            val userSettings = userSettingsList.firstOrNull()

            // Theme appropriate loading screen
            if (userSettings == null) {
                val backgroundColor = if (isSystemInDarkTheme()) {
                    Color(0xFF1A1C1E)
                }

                else {
                    Color(0xFFEAE3D9)
                }

                Box(modifier = Modifier.fillMaxSize().background(backgroundColor))
                return@setContent
            }

            // Settings is the table with the primary key, it's initialized automatically. The other 2 tables, with the foreign
            // keys, get initialized here
            LaunchedEffect(userSettings.userId) {
                userSettings.userId.let { uid ->
                    characteristicsViewModel.initializeUserCharacteristics(uid)
                    trackingsViewModel.initializeUserTrackings(uid)
                }
            }

            // All the initializations must be done, after the user has clicked agree on the mandatory dialog
            LaunchedEffect(userSettings.showMandatoryDialog) {
                if (!userSettings.showMandatoryDialog) {
                    initializeAppLogic(userSettings.username, userSettings.profilePictureString)
                }
            }

            // Gets the set theme option and passes it to the function that sets the theme
            val isDarkTheme = when (userSettings.appearance) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            val useDynamicColor = userSettings.appearance == "Dynamic"

            // Enables edge to edge and makes the system bars icons white in dark mode
            val barStyle = if (isDarkTheme) {
                SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            }

            else {
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
            }

            DisposableEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = barStyle,
                    navigationBarStyle = barStyle
                )

                onDispose {}
            }

            HealthterraTheme(darkTheme = isDarkTheme, dynamicColor = useDynamicColor) {
                DrawerMenu()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            val userSettingsList = settingsViewModel.settings.first { it.isNotEmpty() }
            val userSettings = userSettingsList.first()

            // All the initializations must be done, after the user has clicked agree on the mandatory dialog
            if (userSettings.showMandatoryDialog) {
                return@launch
            }

            // Every time the app is put on the foreground, it calls the daily maintenance worker, doesn't need network. This approach
            // doesn't rely on inconsistent periodic workers, daily resetting works as intended, but for the daily leaderboards sync to
            // happen, the user has to open the app
            performDailyMaintenance(applicationContext)

            // Handles edge case where, with the app on the background, the user allows activity recognition permissions and reopens the
            // app, this opens the foreground service in that instance
            if (userSettings.stepTracking == "Enabled") {
                val serviceIntent = Intent(this@MainActivity, StepTrackerService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                        if (!StepTrackerService.isForegroundServiceActive) {
                            StepTrackerService.isForegroundServiceActive = true
                            startForegroundService(serviceIntent)
                        }
                    }
                }

                else {
                    if (!StepTrackerService.isForegroundServiceActive) {
                        StepTrackerService.isForegroundServiceActive = true
                        startForegroundService(serviceIntent)
                    }
                }
            }

        }
    }

    override fun onStop() {
        super.onStop()

        // If there was a change, syncs non-essential user data on stop to avoid many writes
        if (settingsViewModel.pendingSync) {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncUserWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this).enqueueUniqueWork(
                "SyncUserSettingsWorker",
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )

            settingsViewModel.markSyncHandled()
        }
    }


    private fun initializeAppLogic(username: String, profilePictureString: String) {
        // Checks which permissions actually need to be requested
        val permissionsToRequest = mutableListOf<String>()

        // Asks user for activity recognition and notifications permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        // Asks user only for activity recognition permissions, notifications permissions are enabled by default in this version
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        // Only launches the permission dialog if there are ungranted permissions
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }

        // Initializes Firebase App Check
        Firebase.initialize(this)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        // Authenticates the user anonymously to Firebase when the app first opens, if he doesn't already have a UID, also initializes the
        // according users document
        if (Firebase.auth.currentUser == null) {
            Firebase.auth.signInAnonymously().addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    val initialUserData = mapOf(
                        "activityLevel" to null,
                        "age" to null,
                        "appearance" to "Light",
                        "daysGoal" to 0,
                        "gender" to null,
                        "height" to null,
                        "initialWeightGoalDate" to null,
                        "kgGoal" to 0,
                        "lastSavedDate" to LocalDate.now().toString(),
                        "profilePictureString" to profilePictureString,
                        "stepTracking" to "Enabled",
                        "username" to username,
                        "weight" to null,
                        "weightGoal" to "Maintain"
                    )

                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .set(initialUserData, SetOptions.merge())
                }
            }
        }

        schedulePeriodicNotification()
        performDailyMaintenanceAppActive()

        // Runs every time the settings table changes
        lifecycleScope.launch {
            settingsViewModel.settings.collect { userSettingsList ->
                val userSettings = userSettingsList.firstOrNull()

                // Starts the foreground step tracking service, only if the step tracking setting and the physical activity permissions
                // are enabled. Steps are only counted if the foreground service is active
                if (userSettings?.stepTracking == "Enabled") {
                    val serviceIntent = Intent(this@MainActivity, StepTrackerService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                            if (!StepTrackerService.isForegroundServiceActive) {
                                StepTrackerService.isForegroundServiceActive = true
                                startForegroundService(serviceIntent)
                            }
                        }
                    }

                    // Below this version, permissions are not needed
                    else {
                        if (!StepTrackerService.isForegroundServiceActive) {
                            StepTrackerService.isForegroundServiceActive = true
                            startForegroundService(serviceIntent)
                        }
                    }
                }

                // Kills the service if the user disables the setting
                else if (userSettings?.stepTracking == "Disabled") {
                    val stopIntent = Intent(this@MainActivity, StepTrackerService::class.java)
                    stopService(stopIntent)
                }
            }
        }
    }


    // Sends the already made notification every 3 hours, doesn't need network
    private fun schedulePeriodicNotification() {
        // Testing
        //val testRequest = OneTimeWorkRequestBuilder<NotificationWorker>().build()
        //WorkManager.getInstance(this).enqueue(testRequest)

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(Duration.ofHours(3))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )

            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "periodic_notification",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    // Without this function, if the user is inside the app, the daily maintenance won't happen until he closes and re-opens the
    // app. Sets a delay till midnight, plus a 5-second buffer, and executes the according worker then, if the app closes, this dies
    // as well. It's a loop for the edge case that the user leaves the app open for multiple days
    private fun performDailyMaintenanceAppActive() {
        lifecycleScope.launch {
            while (true) {
                val now = LocalDateTime.now()
                val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
                val millisToMidnight = Duration.between(now, nextMidnight).toMillis() + 5000L

                delay(millisToMidnight.milliseconds)
                performDailyMaintenance(applicationContext)
                delay(1.minutes)
            }
        }
    }
}
