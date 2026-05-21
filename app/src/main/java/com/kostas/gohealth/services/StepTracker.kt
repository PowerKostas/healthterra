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
import com.kostas.gohealth.data.DatabaseProvider
import com.kostas.gohealth.data.entities.Settings
import com.kostas.gohealth.data.entities.Trackings
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
    private var isForegroundServiceActive = false

    private var isCacheInitialized = false
    private var inMemoryStepsProgress = 0
    private var inMemoryLastSavedSteps = 0
    private var inMemoryUserSettings: Settings? = null
    private var inMemoryUserTrackings: Trackings? = null


    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handles weird bug
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        // Only create the notification and register the sensor if the foreground service isn't already active
        if (!isForegroundServiceActive) {
            createNotificationChannel()
            startForeground(1, createNotification())
            initializeCacheAndStartSensor()
            isForegroundServiceActive = true
        }

        return START_STICKY
    }

    private fun initializeCacheAndStartSensor() {
        serviceScope.launch {
            val database = DatabaseProvider.getDatabase(applicationContext)
            inMemoryUserSettings = database.settingsDao().getAll().first().firstOrNull()
            inMemoryUserTrackings = database.trackingsDao().getAll().first().firstOrNull()

            if (inMemoryUserSettings != null && inMemoryUserTrackings != null) {
                inMemoryLastSavedSteps = inMemoryUserSettings!!.lastSavedSteps
                inMemoryStepsProgress = inMemoryUserTrackings!!.stepsProgress
                isCacheInitialized = true

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
            val database = DatabaseProvider.getDatabase(applicationContext)

            inMemoryUserSettings?.let {
                val updatedSettings = it.copy(lastSavedSteps = inMemoryLastSavedSteps)
                database.settingsDao().update(updatedSettings)
                inMemoryUserSettings = updatedSettings
            }

            inMemoryUserTrackings?.let {
                val updatedTrackings = it.copy(stepsProgress = inMemoryStepsProgress)
                database.trackingsDao().update(updatedTrackings)
                inMemoryUserTrackings = updatedTrackings
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
        val openAppIntent = Intent(this, MainActivity::class.java)
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
