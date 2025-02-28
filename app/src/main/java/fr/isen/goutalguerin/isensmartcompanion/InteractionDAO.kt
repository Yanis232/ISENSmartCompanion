package fr.isen.goutalguerin.isensmartcompanion

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InteractionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: Interaction)

    @Query("SELECT * FROM interactions ORDER BY date DESC")
    fun getAllInteractions(): Flow<List<Interaction>>

    @Delete
    suspend fun deleteInteraction(interaction: Interaction)

    @Query("DELETE FROM interactions")
    suspend fun deleteAllInteractions()
}
