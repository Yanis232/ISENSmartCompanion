package fr.isen.goutalguerin.isensmartcompanion


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "interactions")
data class Interaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val answer: String,
    val date: Long = System.currentTimeMillis()
)