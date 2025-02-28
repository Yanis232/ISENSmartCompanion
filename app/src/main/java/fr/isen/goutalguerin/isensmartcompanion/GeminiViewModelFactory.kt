package fr.isen.goutalguerin.isensmartcompanion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GeminiViewModelFactory(
    private val interactionDao: InteractionDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GeminiViewModel::class.java)) {
            //@Suppress("UNCHECKED_CAST")
            //return GeminiViewModel(interactionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}