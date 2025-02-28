package fr.isen.goutalguerin.isensmartcompanion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room

class GeminiViewModel : ViewModel() {

    private val apiKey = BuildConfig.GEMINI_API_KEY
    //private val interactionDao: InteractionDao

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 1024
        }
    )

    // Liste des messages de chat
    private val _chatMessages = MutableStateFlow(
        listOf(ChatMessage("Prêt à répondre à vos questions !", false))
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // État de chargement
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        // Ajouter le message de l'utilisateur à la liste
        _chatMessages.value += ChatMessage(userMessage, true)

        // Indiquer que nous attendons une réponse
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Générer une réponse de Gemini
                val response = generativeModel.generateContent(userMessage)
                val responseText = response.text ?: "Désolé, je n'ai pas pu générer de réponse."

                // Ajouter la réponse à la liste des messages
                _chatMessages.value += ChatMessage(responseText, false)
            } catch (e: Exception) {
                // En cas d'erreur, ajouter un message d'erreur à la liste
                _chatMessages.value += ChatMessage(
                    "Erreur: ${e.localizedMessage ?: "Impossible de communiquer avec Gemini"}",
                    false
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}

