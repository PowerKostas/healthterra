package com.kostas.gohealth.helpers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

fun checkActivityPermissions(context: Context): Boolean {
    // In API 29 and up, check if activity recognition permissions are enabled
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
    }

    // Below API 29, they can't be turned off
    else {
        true
    }
}

fun checkAutoTimeSetting(context: Context): Boolean {
    return Settings.Global.getInt(context.contentResolver, Settings.Global.AUTO_TIME) == 1
}
