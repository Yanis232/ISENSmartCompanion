package fr.isen.goutalguerin.isensmartcompanion

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val question: String,
    val answer: String,
    val timestamp: Date = Date()
)