package fr.isen.goutalguerin.isensmartcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class EventDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val eventId = intent.getStringExtra("event_id") ?: ""

        setContent {
            EventDetailScreen(eventId)
        }
    }
}

@Composable
fun EventDetailScreen(eventId: String) { // Renommé le paramètre à eventId
    val viewModel = remember { EventsViewModel() }
    val eventState by viewModel.selectedEvent.collectAsState()

    // Appeler la méthode pour charger l'événement en fonction de l'ID
    LaunchedEffect(eventId) {
        viewModel.fetchEventDetail(eventId)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            eventState.isLoading -> CircularProgressIndicator()
            eventState.error != null -> Text("Erreur : ${eventState.error}", color = MaterialTheme.colorScheme.error)
            eventState.event != null -> {
                val eventDetail = eventState.event!! // Renommé pour éviter le conflit
                Text(text = eventDetail.title, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Date : ${eventDetail.date}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Titre : ${eventDetail.title}", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Lieu : ${eventDetail.location}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Description : ${eventDetail.description}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Catégorie : ${eventDetail.category}", style = MaterialTheme.typography.bodyMedium)

            }
            }
        }
    }


