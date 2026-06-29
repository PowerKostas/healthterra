package com.healthterra.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.healthterra.data.UserDatabase
import com.healthterra.data.daos.AchievementsDao
import com.healthterra.data.entities.Achievements
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AchievementsViewModel(private val achievementsDao: AchievementsDao) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val database = UserDatabase.getDatabase(application)
                return AchievementsViewModel(database.achievementsDao()) as T
            }
        }
    }

    val achievements: StateFlow<List<Achievements>> = achievementsDao.getAll().stateIn(
        scope = viewModelScope,
        initialValue = emptyList(),
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun initializeUserAchievements(userId: Int) {
        viewModelScope.launch {
            if (achievementsDao.getAll().first().isEmpty()) {
                val defaultAchievements = Achievements(
                    userId = userId,
                )

                achievementsDao.insert(defaultAchievements)
            }
        }
    }

    var pendingSync: Boolean = false
        private set

    fun updateUserAchievements(newAchievements: Achievements) {
        viewModelScope.launch {
            val oldAchievements = achievements.value.firstOrNull()
            achievementsDao.update(newAchievements)

            if (oldAchievements == null) {
                return@launch
            }

            if (oldAchievements != newAchievements) {
                pendingSync = true
            }
        }
    }

    fun markSyncHandled() {
        pendingSync = false
    }
}
