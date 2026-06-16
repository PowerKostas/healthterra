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
import com.healthterra.data.daos.CharacteristicsDao
import com.healthterra.data.entities.Characteristics
import com.healthterra.services.SyncUserWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CharacteristicsViewModel(private val characteristicsDao: CharacteristicsDao) : ViewModel() {
    // A ViewModel factory, have to use it, otherwise the view model doesn't work
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val database = UserDatabase.getDatabase(application)
                return CharacteristicsViewModel(database.characteristicsDao()) as T
            }
        }
    }

    // The characteristics variable holds every entry in the users table, because the app only supports a single local user, there will
    // probably only be one entry. Because of this block Compose automatically redraws the screen, everytime a change in the table happens
    val characteristics: StateFlow<List<Characteristics>> = characteristicsDao.getAll().stateIn(
        scope = viewModelScope,
        initialValue = emptyList(),
        started = SharingStarted.WhileSubscribed(5000)
    )

    // SettingsViewModel calls it, and checks if a user hasn't been created yet (first time the app opens). If there are no users it
    // inserts a default user
    fun initializeUserCharacteristics(userId: Int) {
        viewModelScope.launch {
            if (characteristicsDao.getAll().first().isEmpty()) {
                val defaultCharacteristics = Characteristics(
                    userId = userId,
                )

                characteristicsDao.insert(defaultCharacteristics)
            }
        }
    }

    // Public function that the respective screens call
    fun updateUserCharacteristics(newCharacteristics: Characteristics, context: Context) {
        viewModelScope.launch {
            val oldCharacteristics = characteristics.value.firstOrNull()
            characteristicsDao.update(newCharacteristics) // Room API update

            // Also syncs user data to Firestore, if any of the data changed, needs network
            val hasChanged = oldCharacteristics != newCharacteristics
            if (hasChanged) {
                val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                val syncRequest = OneTimeWorkRequestBuilder<SyncUserWorker>()
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "SyncUserCharacteristicsWorker",
                    ExistingWorkPolicy.REPLACE,
                    syncRequest
                )
            }
        }
    }
}
