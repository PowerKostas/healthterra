package com.kostas.gohealth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.Firebase
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.auth
import com.google.firebase.initialize
import com.kostas.gohealth.services.NotificationWorker
import com.kostas.gohealth.services.StepTrackerService
import com.kostas.gohealth.services.performDailyMaintenance
import com.kostas.gohealth.ui.components.central.DrawerMenu
import com.kostas.gohealth.ui.themes.GoHealthTheme
import com.kostas.gohealth.ui.viewModels.CharacteristicsViewModel
import com.kostas.gohealth.ui.viewModels.SettingsViewModel
import com.kostas.gohealth.ui.viewModels.TrackingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

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
                startForegroundService(serviceIntent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Initializes Firebase App Check
        Firebase.initialize(this)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        // Authenticates the user anonymously to Firebase when the app first opens
        Firebase.auth.signInAnonymously()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        // Asks user for activity recognition and notifications permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACTIVITY_RECOGNITION))
        }

        // Asks user only for activity recognition permissions, notifications permissions are enabled by default in this version
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION))
        }

        schedulePeriodicNotification()
        performDailyMaintenanceAppActive()

        // Runs every time the settings table changes
        lifecycleScope.launch {
            settingsViewModel.settings.collect { userSettingsList ->
                val userSettings = userSettingsList.firstOrNull()

                // Settings is the table with the primary key, it's initialized automatically. The other 2 tables, with the foreign
                // keys, get initialized here
                userSettings?.userId?.let { uid ->
                    characteristicsViewModel.initializeUserCharacteristics(uid)
                    trackingsViewModel.initializeUserTrackings(uid)
                }

                // Starts the foreground step tracking service, only if the step tracking setting and the physical activity permissions
                // are enabled. Steps are only counted if the foreground service is active
                if (userSettings?.stepTracking == "Enabled") {
                    val serviceIntent = Intent(this@MainActivity, StepTrackerService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                            startForegroundService(serviceIntent)
                        }
                    }

                    // Below this version, permissions are not needed
                    else {
                        startForegroundService(serviceIntent)
                    }
                }

                // Kills the service if the user disables the setting
                else if (userSettings?.stepTracking == "Disabled") {
                    val stopIntent = Intent(this@MainActivity, StepTrackerService::class.java)
                    stopService(stopIntent)
                }
            }
        }

        setContent {
            val userSettingsList by settingsViewModel.settings.collectAsState()
            val userSettings = userSettingsList.firstOrNull()

            // Gets the set theme option and passes it to the function that sets the theme
            if (userSettings != null) {
                val isDarkTheme = when (userSettings.appearance) {
                    "Light" -> false
                    "Dark" -> true
                    else -> isSystemInDarkTheme()
                }

                val useDynamicColor = userSettings.appearance == "Dynamic"

                GoHealthTheme(darkTheme = isDarkTheme, dynamicColor = useDynamicColor) {
                    DrawerMenu()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Every time the app is put on the foreground, it calls the daily maintenance worker, doesn't need network. This approach doesn't
        // rely on inconsistent periodic workers, daily resetting works as intended, but for the daily leaderboards sync to happen, the user
        // has to open the app
        lifecycleScope.launch {
            performDailyMaintenance(applicationContext)
        }

        // Handles edge case where, with the app on the background, the user allows activity recognition permissions and reopens the
        // app, this opens the foreground service in that instance
        lifecycleScope.launch {
            val userSettingsList = settingsViewModel.settings.first()
            val userSettings = userSettingsList.firstOrNull()

            if (userSettings?.stepTracking == "Enabled") {
                val serviceIntent = Intent(this@MainActivity, StepTrackerService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                        startForegroundService(serviceIntent)
                    }
                }

                else {
                    startForegroundService(serviceIntent)
                }
            }
        }
    }


    // Sends the already made notification every 3 hours, doesn't need network
    private fun schedulePeriodicNotification() {
        // Testing
        //val testRequest = OneTimeWorkRequestBuilder<NotificationWorker>().build()
        //WorkManager.getInstance(this).enqueue(testRequest)

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(3, TimeUnit.HOURS)
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

                delay(millisToMidnight)
                performDailyMaintenance(applicationContext)
                delay(60000)
            }
        }
    }
}
