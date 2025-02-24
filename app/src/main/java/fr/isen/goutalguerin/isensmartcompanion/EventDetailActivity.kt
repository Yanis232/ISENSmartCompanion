package fr.isen.goutalguerin.isensmartcompanion

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.goutalguerin.isensmartcompanion.ui.theme.ISENSmartCompanionTheme

class EventsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ISENSmartCompanionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    EventsScreen()
                }
            }
        }
    }
}

@Composable
fun EventsScreen() {
    val fakeEvents = listOf(
        Event(
            id = 1,
            title = "Soirée BDE",
            description = "Soirée d'intégration avec animations et buffet",
            date = "15/09/2024",
            location = "Bar étudiant ISEN",
            category = "Party"
        ),
        Event(
            id = 2,
            title = "Gala ISEN",
            description = "Cérémonie annuelle de remise des diplômes",
            date = "20/06/2024",
            location = "Palais des Congrès",
            category = "Official"
        ),
        Event(
            id = 3,
            title = "Hackathon ISEN",
            description = "Compétition de programmation sur 48h",
            date = "10/11/2024",
            location = "Campus ISEN",
            category = "Tech"
        ),
        Event(
            id = 4,
            title = "DNF (Digital National Forum)",
            description = "Forum national sur les technologies numériques avec des conférences et des ateliers",
            date = "05/03/2025",
            location = "Paris Expo Porte de Versailles",
            category = "Tech"
        ),
        Event(
            id = 5,
            title = "Nuit de L'ISEN",
            description = "Soirée étudiante annuelle avec concerts, DJ et animations",
            date = "25/04/2025",
            location = "Campus ISEN",
            category = "Party"
        ),
        Event(
            id = 6,
            title = "Sortie ski ISEN",
            description = "Week-end au ski pour les étudiants de l'ISEN",
            date = "15/02/2025",
            location = "Les Arcs",
            category = "Sport"
        ),
        Event(
            id = 7,
            title = "WEI 2025",
            description = "Week-end d'intégration pour les nouveaux étudiants",
            date = "12/09/2025",
            location = "Bretagne",
            category = "Party"
        )
    )

    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Événements ISEN",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(fakeEvents) { event ->
                EventItem(event) {
                    val intent = Intent(context, EventDetailActivity::class.java).apply {
                        putExtra("EVENT", event)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }
}

@Composable
fun EventItem(event: Event, onClick: () -> Unit) {
    val categoryColor = when (event.category) {
        "Party" -> Color(0xFFFF6B6B)
        "Official" -> Color(0xFF4ECDC4)
        "Tech" -> Color(0xFF45B7D1)
        "Sport" -> Color(0xFF77DD77)
        else -> Color.DarkGray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = categoryColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = event.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${event.date} - ${event.location}",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

class EventDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ISENSmartCompanionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    val event = intent.getParcelableExtra<Event>("EVENT")
                    EventDetailScreen(event ?: Event(0, "", "", "", "", ""))
                }
            }
        }
    }
}

@Composable
fun EventDetailScreen(event: Event) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = event.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        DetailRow("Date", event.date)
        DetailRow("Lieu", event.location)
        DetailRow("Catégorie", event.category)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Description",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
        Text(
            text = event.description,
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label :",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}