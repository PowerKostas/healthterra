package com.kostas.gohealth.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kostas.gohealth.data.DatabaseProvider
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
                    val database = DatabaseProvider.getDatabase(context)
                    val userSettingsList = database.settingsDao().getAll().first()
                    val userSettings = userSettingsList.firstOrNull()

                    if (userSettings?.stepTracking == "Enabled") {
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