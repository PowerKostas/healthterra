package com.healthterra.services

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.healthterra.data.UserDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Restarts the foreground service, if the user restarts their device
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val pendingResult = goAsync() // Keeps the receiver alive long enough for the coroutine to finish

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                            return@launch
                        }
                    }

                    val database = UserDatabase.getDatabase(context)
                    val userSettingsList = database.settingsDao().getAll().first()
                    val userSettings = userSettingsList.firstOrNull()

                    if (userSettings != null && !userSettings.showMandatoryDialog && userSettings.stepTracking == "Enabled") {
                        StepTrackerService.isForegroundServiceActive = true
                        context.startForegroundService(Intent(context, StepTrackerService::class.java))
                    }
                }

                finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
