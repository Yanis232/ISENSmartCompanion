package fr.isen.goutalguerin.isensmartcompanion

data class ChatMessage(
    val content: String,
    val isUserMessage: Boolean = false
)

