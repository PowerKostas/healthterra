package com.healthterra.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.healthterra.data.UserDatabase
import com.healthterra.data.daos.TrackingsDao
import com.healthterra.data.entities.Trackings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrackingsViewModel(private val trackingsDao: TrackingsDao) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val database = UserDatabase.getDatabase(application)
                return TrackingsViewModel(database.trackingsDao()) as T
            }
        }
    }

    val trackings: StateFlow<List<Trackings>> = trackingsDao.getAll().stateIn(
        scope = viewModelScope,
        initialValue = emptyList(),
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun initializeUserTrackings(userId: Int) {
        viewModelScope.launch {
            if (trackingsDao.getAll().first().isEmpty()) {
                val defaultTrackings = Trackings(
                    userId = userId,
                )

                trackingsDao.insert(defaultTrackings)
            }
        }
    }

    fun updateUserTrackings(trackings: Trackings) {
        viewModelScope.launch {
            trackingsDao.update(trackings)
        }
    }
}
