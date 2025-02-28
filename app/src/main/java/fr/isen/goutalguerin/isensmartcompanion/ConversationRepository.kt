package fr.isen.goutalguerin.isensmartcompanion

import kotlinx.coroutines.flow.Flow

class ConversationRepository(private val conversationDao: ConversationDao) {
    val allConversations: Flow<List<Conversation>> = conversationDao.getAllConversations()

    suspend fun insertConversation(question: String, answer: String) {
        val conversation = Conversation(question = question, answer = answer)
        conversationDao.insertConversation(conversation)
    }

    suspend fun deleteConversation(conversation: Conversation) {
        conversationDao.deleteConversation(conversation)
    }

    suspend fun deleteAllConversations() {
        conversationDao.deleteAllConversations()
    }
}