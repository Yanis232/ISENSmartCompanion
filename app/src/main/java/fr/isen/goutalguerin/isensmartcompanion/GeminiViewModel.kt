package fr.isen.goutalguerin.isensmartcompanion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GeminiViewModel(application: Application) : AndroidViewModel(application) {

    private val apiKey = BuildConfig.GEMINI_API_KEY

    // Liste des modèles disponibles
    val availableModels = listOf(
        "gemini-2.0-pro-exp-02-05",
        "gemini-2.0-flash-thinking-exp-01-21",
        "gemini-2.0-flash-exp",
        "gemini-2.0-flash-lite",
        "gemini-1.5-pro",
        "gemini-1.5-pro-latest",
        "gemini-1.5-flash",

    )

    // État pour stocker le modèle actuellement sélectionné
    private val _selectedModel = MutableStateFlow("gemini-2.0-pro-exp-02-05")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    // Modèle génératif
    private var generativeModel = createGenerativeModel(_selectedModel.value)

    // Session de chat (maintient l'historique de la conversation)
    private var chatSession = generativeModel.startChat()

    // Repository pour les conversations
    private val repository: ConversationRepository

    // Liste des messages de chat
    private val _chatMessages = MutableStateFlow(
        listOf(ChatMessage("Prêt à répondre à vos questions !", false))
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // État de chargement
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        val conversationDao = AppDatabase.getDatabase(application).conversationDao()
        repository = ConversationRepository(conversationDao)
    }

    // Fonction pour créer un modèle génératif avec le nom de modèle spécifié
    private fun createGenerativeModel(modelName: String): GenerativeModel {
        return GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.6f
                topK = 50
                topP = 0.92f
                maxOutputTokens = 4096
            }
        )
    }

    // Fonction pour changer le modèle
    fun setModel(modelName: String) {
        if (modelName in availableModels) {
            _selectedModel.value = modelName
            generativeModel = createGenerativeModel(modelName)

            // Réinitialiser la session de chat avec le nouveau modèle
            chatSession = generativeModel.startChat()

            // Ajouter un message indiquant le changement de modèle
            _chatMessages.value += ChatMessage("Modèle changé pour $modelName. L'historique de conversation a été réinitialisé.", false)
        }
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        // Ajouter le message de l'utilisateur à la liste
        _chatMessages.value += ChatMessage(userMessage, true)

        // Indiquer que nous attendons une réponse
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Utiliser la session de chat pour envoyer le message et obtenir une réponse
                val response = chatSession.sendMessage(userMessage)
                val responseText = response.text ?: "Désolé, je n'ai pas pu générer de réponse."

                // Ajouter la réponse à la liste des messages
                _chatMessages.value += ChatMessage(responseText, false)

                // Sauvegarder la conversation dans la base de données
                repository.insertConversation(userMessage, responseText)
            } catch (e: Exception) {
                val errorMessage = "Erreur: ${e.localizedMessage ?: "Impossible de communiquer avec Gemini"}"
                // Ajouter un message d'erreur à la liste
                _chatMessages.value += ChatMessage(errorMessage, false)

                // Sauvegarder également l'erreur dans la base de données
                repository.insertConversation(userMessage, errorMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fonction pour effacer l'historique de conversation
    fun clearConversation() {
        // Réinitialiser la session de chat
        chatSession = generativeModel.startChat()

        // Réinitialiser les messages affichés
        _chatMessages.value = listOf(ChatMessage("Conversation réinitialisée. Prêt à répondre à vos questions !", false))
    }
}