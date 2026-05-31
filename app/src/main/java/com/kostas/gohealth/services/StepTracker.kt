package com.kostas.gohealth.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.kostas.gohealth.MainActivity
import com.kostas.gohealth.R
import com.kostas.gohealth.data.UserDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StepTrackerService : Service(), SensorEventListener {
    // Initializations
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    companion object { var isForegroundServiceActive = false }

    // To avoid Android killing the foreground service, as much as possible, this function uses an in memory version of the local
    // database, as to be faster by avoiding unnecessary transactions
    private var isCacheInitialized = false
    private var inMemoryStepsProgress = 0
    private var inMemoryLastSavedSteps = 0
    private var userId: Int = 0


    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
        isForegroundServiceActive = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(1, createNotification())

        // Handles weird bug
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        if (intent?.action == "RESET_STEPS_MIDNIGHT") {
            inMemoryStepsProgress = 0
            saveToDatabase()
        }

        // Only initialize the sensor and cache if it hasn't been done yet
        if (!isCacheInitialized) {
            initializeCacheAndStartSensor()
        }

        return START_STICKY
    }

    private fun initializeCacheAndStartSensor() {
        serviceScope.launch {
            val database = UserDatabase.getDatabase(applicationContext)
            val userSettings = database.settingsDao().getAll().first().firstOrNull()
            val userTrackings = database.trackingsDao().getAll().first().firstOrNull()

            if (userSettings != null && userTrackings != null) {
                isCacheInitialized = true
                inMemoryLastSavedSteps = userSettings.lastSavedSteps
                inMemoryStepsProgress = userTrackings.stepsProgress
                userId = userSettings.userId

                registerStepSensor()
            }
        }
    }

    // If the phone is asleep, it batches steps for up to 5 minutes before waking the CPU. If the phone is active, the queued steps are
    // delivered to the app immediately
    private fun registerStepSensor() {
        stepSensor?.let {
            val maxLatencyUs = 300_000_000
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL, maxLatencyUs)
        }
    }

    // Gets triggered on every new step
    override fun onSensorChanged(event: SensorEvent?) {
        if (!isCacheInitialized) return

        // Every 10 new steps, it updates stepsProgress and lastSavedSteps. The step counter in Android counts steps since the last
        // reboot, this is why we subtract the lastSavedSteps from the totalSteps to get the new steps. New steps go into
        // stepsProgress, stepsProgress gets reset every midnight and lastSavedSteps gets the value of totalSteps.
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toInt()
            val newSteps = totalSteps - inMemoryLastSavedSteps

            // Handles the first time the user opens the app
            if (inMemoryLastSavedSteps == 0) {
                inMemoryLastSavedSteps = totalSteps
                saveToDatabase()
                return
            }

            // Handles device reboots
            if (newSteps < 0) {
                inMemoryStepsProgress += totalSteps
                inMemoryLastSavedSteps = totalSteps
                saveToDatabase()
            }

            else if (newSteps >= 10) {
                inMemoryStepsProgress += newSteps
                inMemoryLastSavedSteps = totalSteps
                saveToDatabase()
            }
        }
    }

    private fun saveToDatabase() {
        serviceScope.launch {
            userId.let { uid ->
                // Because StepTracker uses a stale in-memory version of the local database, only update the steps, not the other columns
                val database = UserDatabase.getDatabase(applicationContext)
                database.settingsDao().updateLastSavedSteps(uid, inMemoryLastSavedSteps)
                database.trackingsDao().updateStepsProgress(uid, inMemoryStepsProgress)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }
    override fun onBind(intent: Intent?): IBinder? { return null }


    // Builds the foreground notification, opens the app when tapped
    private fun createNotificationChannel() {
        val channel = NotificationChannel("step_tracker_channel", "Step Tracker", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        // Prevents duplicate instances and brings the existing activity to the front by clearing the screens above it
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val openAppPendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, "step_tracker_channel")
            .setContentTitle("Step tracking active")
            .setContentText("Running smoothly in the background")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openAppPendingIntent)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }
}
