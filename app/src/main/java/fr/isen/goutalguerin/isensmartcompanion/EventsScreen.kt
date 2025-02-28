package fr.isen.goutalguerin.isensmartcompanion

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun EventsScreen(viewModel: EventsViewModel) {
    val eventsState by viewModel.events.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Événements",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            eventsState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            eventsState.error != null -> {
                Text("Erreur : ${eventsState.error}", color = MaterialTheme.colorScheme.error)
            }
            else -> {
                LazyColumn {
                    items(eventsState.events) { event ->
                        EventCard(event) { selectedEvent ->
                            // Change here: pass the entire event object instead of just the ID
                            context.startActivity(
                                Intent(context, EventDetailActivity::class.java).apply {
                                    putExtra("event", selectedEvent)  // Pass the whole Event object
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: Event, onClick: (Event) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(event) },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.titleMedium)
            Text(text = "Date : ${event.date}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Lieu : ${event.location}", style = MaterialTheme.typography.bodySmall)
            Text(text = "id : ${event.id}", style = MaterialTheme.typography.bodySmall)
        }
    }
}