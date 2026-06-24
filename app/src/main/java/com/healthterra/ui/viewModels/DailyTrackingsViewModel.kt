package com.healthterra.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.healthterra.data.UserDatabase
import com.healthterra.data.daos.DailyTrackingsDao
import com.healthterra.data.entities.DailyTrackings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// This view model doesn't get initialized because a new row doesn't need to be created, if the user doesn't log anything that day
class DailyTrackingsViewModel(private val dailyTrackingsDao: DailyTrackingsDao) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val database = UserDatabase.getDatabase(application)
                return DailyTrackingsViewModel(database.dailyTrackingsDao()) as T
            }
        }
    }

    fun dailyTrackings(limit: Int): Flow<List<DailyTrackings>> {
        return dailyTrackingsDao.getDailyTrackings(limit)
    }

    fun upsertUserDailyTrackings(dailyTrackings: DailyTrackings) {
        viewModelScope.launch {
            dailyTrackingsDao.upsert(dailyTrackings)
        }
    }

    fun deleteUserDailyTrackings() {
        viewModelScope.launch {
            dailyTrackingsDao.delete()
        }
    }
}
