package com.kostas.gohealth.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kostas.gohealth.MainActivity
import com.kostas.gohealth.R
import com.kostas.gohealth.data.UserDatabase
import com.kostas.gohealth.helpers.calculateExerciseGoal
import kotlinx.coroutines.flow.first
import java.time.LocalTime

private const val CHANNEL_ID = "periodic_channel"
private val uniqueNotificationId = System.currentTimeMillis().toInt()

private val randomTitles = arrayOf("Daily Exercise Progress", "Exercise Goal", "Exercise Break", "Exercise Reminder", "New Exercise Session")
private val randomTexts = arrayOf("Time for your exercise set ⏰", "Stay on track with a quick set of reps \uD83D\uDCAA", "A short set of exercises will help maintain your momentum ⚡", "Ready for your next set?", "Your future self will thank you!")

class NotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    // Only sends notifications between 12pm and 12am and only if the user hasn't completed the exercise goal
    override suspend fun doWork(): Result {
        val database = UserDatabase.getDatabase(applicationContext)
        val userTrackings = database.trackingsDao().getAll().first().firstOrNull()
        val userCharacteristics = database.characteristicsDao().getAll().first().firstOrNull()

        if (userTrackings == null || userCharacteristics == null) {
            return Result.success()
        }

        if (LocalTime.now() >= LocalTime.of(12, 0) && userTrackings.exerciseProgress.sum() < calculateExerciseGoal(userCharacteristics)) {
            sendNotification()
        }

        return Result.success()
    }

    // Builds high importance stackable notifications, opens the app when tapped
    private fun sendNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(CHANNEL_ID, "Periodic Notifications", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val openAppIntent = Intent(applicationContext, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
        val openAppPendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(randomTitles.random())
            .setContentText(randomTexts.random())
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .build()

        notificationManager.notify(uniqueNotificationId, notification)
    }
}
