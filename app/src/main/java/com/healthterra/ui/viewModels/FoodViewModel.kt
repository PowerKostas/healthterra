package com.healthterra.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.sqlite.db.SimpleSQLiteQuery
import com.healthterra.data.FoodDatabase
import com.healthterra.data.daos.FoodDao
import com.healthterra.data.entities.Food
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
                val database = FoodDatabase.getDatabase(application)
                return FoodViewModel(database.foodDao()) as T
            }
        }
    }

    // Private mutable state for the ViewModel to update, public read-only state for the UI to observe
    private val localSearchResults = MutableStateFlow<List<Food>>(emptyList())
    val searchResults: StateFlow<List<Food>> = localSearchResults.asStateFlow()

    // Same logic, but this is used to only show error messages after the loading is over
    private val localIsLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = localIsLoading.asStateFlow()

    // Splits the input in individual words and produces queries like, SELECT * FROM food WHERE item LIKE '%word1%' AND item LIKE '%word2%', if
    // both words are found, the row is returned
    fun searchFood(input: String) {
        viewModelScope.launch {
            localIsLoading.value = true

            val trimmedInput = input.trim()
            if (trimmedInput.isEmpty()) {
                localSearchResults.value = emptyList()
                localIsLoading.value = false
                return@launch
            }

            val words = trimmedInput.split("\\s+".toRegex())
            val conditions = words.joinToString(" AND ") { "item LIKE '%' || ? || '%'" }
            val query = SimpleSQLiteQuery(
                "SELECT * FROM food WHERE $conditions",
                words.toTypedArray()
            )

            localSearchResults.value = foodDao.searchFoodRaw(query)
            localIsLoading.value = false
        }
    }
}
