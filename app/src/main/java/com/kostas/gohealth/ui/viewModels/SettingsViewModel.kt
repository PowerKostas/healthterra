package com.kostas.gohealth.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.kostas.gohealth.data.UserDatabase
import com.kostas.gohealth.data.daos.SettingsDao
import com.kostas.gohealth.data.entities.Settings
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

    fun updateUserSettings(settings: Settings) {
        viewModelScope.launch {
            settingsDao.update(settings)
        }
    }
}
