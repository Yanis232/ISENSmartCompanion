package fr.isen.goutalguerin.isensmartcompanion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.isen.goutalguerin.isensmartcompanion.AppDatabase
import fr.isen.goutalguerin.isensmartcompanion.Conversation
import fr.isen.goutalguerin.isensmartcompanion.ConversationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ConversationRepository

    val allConversations: StateFlow<List<Conversation>>

    init {
        val conversationDao = AppDatabase.getDatabase(application).conversationDao()
        repository = ConversationRepository(conversationDao)
        allConversations = repository.allConversations
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )
    }

    fun deleteConversation(conversation: Conversation) {
        viewModelScope.launch {
            repository.deleteConversation(conversation)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.deleteAllConversations()
        }
    }
}