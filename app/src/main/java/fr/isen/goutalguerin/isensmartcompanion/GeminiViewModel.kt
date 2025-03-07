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

    // Liste des modèles disponibles - je les ai eu en cherchant sur le lien suivant: https://ai.google.dev/gemini-api/docs/models/experimental-models?hl=fr#available-models
    // les modeles pro sont soumis à une limite de requetes par jour
    val availableModels = listOf(
        "gemini-2.0-flash-exp",
        "gemini-2.0-pro-exp-02-05",
        "gemini-2.0-flash-thinking-exp-01-21",
        "gemini-2.0-flash-lite",
        "learnlm-1.5-pro-experimental",
        "gemini-1.5-pro",
        "gemini-1.5-pro-latest",
        "gemini-1.5-flash",

    )

    //ai par defaut
    private val _selectedModel = MutableStateFlow("gemini-2.0-flash-exp")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()
    private var generativeModel = createGenerativeModel(_selectedModel.value)

    //permet de garder la conversation (n'oublie pas le context et les input users precedents)
    private var chatSession = generativeModel.startChat()

    private val repository: ConversationRepository

    // Liste des messages de chat
    private val _chatMessages = MutableStateFlow(
        listOf(ChatMessage("Prêt à répondre à vos questions !", false))
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        val conversationDao = AppDatabase.getDatabase(application).conversationDao()
        repository = ConversationRepository(conversationDao)
    }

    // modeles param pour donner des reponses plus detaillées
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

            chatSession = generativeModel.startChat()

            _chatMessages.value += ChatMessage("Modèle changé pour $modelName. L'historique de conversation a été réinitialisé.", false)
        }
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        // Ajouter le message de l'utilisateur à la liste
        _chatMessages.value += ChatMessage(userMessage, true)

        _isLoading.value = true

        viewModelScope.launch {
            try {

                val response = chatSession.sendMessage(userMessage)
                val responseText = response.text ?: "Désolé, je n'ai pas pu générer de réponse."

                _chatMessages.value += ChatMessage(responseText, false)

                // Sauvegarder la conversation dans la base de données
                repository.insertConversation(userMessage, responseText)
            } catch (e: Exception) {
                val errorMessage = "Erreur: ${e.localizedMessage ?: "Impossible de communiquer avec Gemini"}"
                _chatMessages.value += ChatMessage(errorMessage, false)

                // Sauvegarder également l'erreur dans la base de données
                repository.insertConversation(userMessage, errorMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearConversation() {
        chatSession = generativeModel.startChat()

        _chatMessages.value = listOf(ChatMessage("Conversation réinitialisée. Prêt à répondre à vos questions !", false))
    }
}