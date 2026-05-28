package com.kostas.gohealth.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.kostas.gohealth.data.DatabaseProvider
import com.kostas.gohealth.data.daos.FoodDao
import com.kostas.gohealth.data.entities.Food
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FoodViewModel(private val foodDao: FoodDao) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val database = DatabaseProvider.getDatabase(application)
                return FoodViewModel(database.foodDao()) as T
            }
        }
    }

    // Private mutable state for the ViewModel to update, public read-only state for the UI to observe
    private val localSearchResults = MutableStateFlow<List<Food>>(emptyList())
    val searchResults: StateFlow<List<Food>> = localSearchResults.asStateFlow()

    fun searchFood(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            localSearchResults.value = foodDao.searchFood(query)
        }
    }
}
