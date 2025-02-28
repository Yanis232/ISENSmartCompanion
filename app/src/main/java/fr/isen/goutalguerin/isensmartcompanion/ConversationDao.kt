package fr.isen.goutalguerin.isensmartcompanion

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY timestamp DESC")
    fun getAllConversations(): Flow<List<Conversation>>

    @Insert
    suspend fun insertConversation(conversation: Conversation)

    @Delete
    suspend fun deleteConversation(conversation: Conversation)

    @Query("DELETE FROM conversations")
    suspend fun deleteAllConversations()
}