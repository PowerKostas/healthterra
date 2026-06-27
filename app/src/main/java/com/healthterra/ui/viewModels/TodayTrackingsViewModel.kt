package com.healthterra.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.healthterra.data.UserDatabase
import com.healthterra.data.daos.TodayTrackingsDao
import com.healthterra.data.entities.TodayTrackings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodayTrackingsViewModel(private val todayTrackingsDao: TodayTrackingsDao) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val database = UserDatabase.getDatabase(application)
                return TodayTrackingsViewModel(database.todayTrackingsDao()) as T
            }
        }
    }

    val todayTrackings: StateFlow<List<TodayTrackings>> = todayTrackingsDao.getAll().stateIn(
        scope = viewModelScope,
        initialValue = emptyList(),
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun initializeUserTodayTrackings(userId: Int) {
        viewModelScope.launch {
            if (todayTrackingsDao.getAll().first().isEmpty()) {
                val defaultTodayTrackings = TodayTrackings(
                    userId = userId,
                )

                todayTrackingsDao.insert(defaultTodayTrackings)
            }
        }
    }

    fun updateUserTodayTrackings(todayTrackings: TodayTrackings) {
        viewModelScope.launch {
            todayTrackingsDao.update(todayTrackings)
        }
    }
}
