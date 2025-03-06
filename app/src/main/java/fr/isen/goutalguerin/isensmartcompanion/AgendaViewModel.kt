package fr.isen.goutalguerin.isensmartcompanion // Assurez-vous que cela correspond à l'emplacement du fichier

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AgendaViewModel(application: Application) : AndroidViewModel(application) {
    private val _agendaItems = mutableStateOf<List<AgendaItem>>(emptyList())
    val agendaItems: State<List<AgendaItem>> = _agendaItems

    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()

    init {
        viewModelScope.launch {
            loadCalendarEvents()
        }
    }

    private suspend fun getCredentials(): GoogleAccountCredential? {
        return withContext(Dispatchers.IO) {
            try {
                val gso = GoogleSignInOptions.Builder()
                    .requestEmail()
                    .requestScopes(com.google.android.gms.common.api.Scope(CalendarScopes.CALENDAR_READONLY))
                    .build()

                // Supprime la variable inutilisée et utilise directement l'appel
                val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(getApplication())

                if (account == null) {
                    _agendaItems.value = listOf(AgendaItem("Connexion requise", 0L, 0L, "Veuillez vous connecter à Google"))
                    return@withContext null
                }

                GoogleAccountCredential.usingOAuth2(
                    getApplication(),
                    listOf(CalendarScopes.CALENDAR_READONLY)
                ).apply {
                    selectedAccount = account.account
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _agendaItems.value = listOf(AgendaItem("Erreur d'authentification", 0L, 0L, e.message ?: "Erreur inconnue"))
                null
            }
        }
    }

    // Rendre la fonction privée car elle n'est utilisée qu'à l'intérieur de la classe
    private fun loadCalendarEvents() {
        viewModelScope.launch {
            val credential = getCredentials() ?: return@launch

            try {
                val items = withContext(Dispatchers.IO) {
                    val calendarService = Calendar.Builder(httpTransport, jsonFactory, credential)
                        .setApplicationName("ISEN Smart Companion")
                        .build()

                    val now = DateTime(System.currentTimeMillis())
                    val events = calendarService.events().list("primary")
                        .setTimeMin(now)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute()

                    events.items.map { event ->
                        AgendaItem(
                            title = event.summary ?: "Sans titre",
                            startTime = event.start.dateTime?.value ?: event.start.date.value,
                            endTime = event.end.dateTime?.value ?: event.end.date.value,
                            description = event.description ?: ""
                        )
                    }
                }
                _agendaItems.value = items
            } catch (e: UserRecoverableAuthIOException) {
                _agendaItems.value = listOf(AgendaItem("Autorisation refusée", 0L, 0L, "L'accès au calendrier a été refusé. Vérifiez les permissions ou contactez le développeur."))
            } catch (e: Exception) {
                e.printStackTrace()
                _agendaItems.value = listOf(AgendaItem("Erreur de chargement", 0L, 0L, e.message ?: "Erreur inconnue"))
            }
        }
    }
}

data class AgendaItem(
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val description: String
)