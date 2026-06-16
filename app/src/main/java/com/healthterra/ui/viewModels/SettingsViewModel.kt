package com.healthterra.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.healthterra.data.UserDatabase
import com.healthterra.data.daos.SettingsDao
import com.healthterra.data.entities.Settings
import com.healthterra.services.SyncUserWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsDao: SettingsDao) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val database = UserDatabase.getDatabase(application)
                return SettingsViewModel(database.settingsDao()) as T
            }
        }
    }

    val settings: StateFlow<List<Settings>> = settingsDao.getAll().stateIn(
        scope = viewModelScope,
        initialValue = emptyList(),
        started = SharingStarted.WhileSubscribed(5000)
    )

    init {
        viewModelScope.launch {
            if (settingsDao.getAll().first().isEmpty()) {
                val defaultSettings = Settings()
                settingsDao.insert(defaultSettings)
            }
        }
    }

    // Used for non-essential syncs, handled on onStop()
    var pendingSync: Boolean = false
        private set

    fun updateUserSettings(newSettings: Settings, context: Context, syncToFirestore: Boolean = true) {
        viewModelScope.launch {
            val oldSettings = settings.value.firstOrNull()
            settingsDao.update(newSettings) // Room API update

            // Optional Firestore sync, because the settings table has some columns that don't need to be synced
            if (!syncToFirestore || oldSettings == null) {
                return@launch
            }

            // Leaderboards data
            val hasChangedEssential = oldSettings.profilePictureString != newSettings.profilePictureString || oldSettings.username != newSettings.username

            // Date only used for backup
            val hasChangedNonEssential= oldSettings.initialWeightGoalDate != newSettings.initialWeightGoalDate || oldSettings.appearance != newSettings.appearance ||
                    oldSettings.stepTracking != newSettings.stepTracking || oldSettings.lastSavedDate != newSettings.lastSavedDate

            // Syncs user data to Firestore, if any of the leaderboards data changed, needs network
            if (hasChangedEssential) {
                val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                val syncRequest = OneTimeWorkRequestBuilder<SyncUserWorker>()
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "SyncUserSettingsWorker",
                    ExistingWorkPolicy.REPLACE,
                    syncRequest
                )

                pendingSync = false
            }

            else if (hasChangedNonEssential) {
                pendingSync = true
            }
        }
    }

    fun markSyncHandled() {
        pendingSync = false
    }
}
