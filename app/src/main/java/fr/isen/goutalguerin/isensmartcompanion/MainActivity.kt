package fr.isen.goutalguerin.isensmartcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import fr.isen.goutalguerin.isensmartcompanion.ui.theme.ISENSmartCompanionTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ISENSmartCompanionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp(application)
                }
            }
        }
    }
}

@Composable
fun MainApp(application: android.app.Application) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                val geminiViewModel: GeminiViewModel = viewModel(
                    factory = GeminiViewModelFactory(application)
                )
                MainScreen(geminiViewModel)
            }
            composable("events") {
                val eventsViewModel: EventsViewModel = viewModel()
                EventsScreen(viewModel = eventsViewModel)
            }
            composable("history") {
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModelFactory(application)
                )
                // Utiliser la fonction HistoryScreen du fichier HistoryScreen.kt
                HistoryScreen(historyViewModel)
            }
        }
    }
}

// Factory pour créer le GeminiViewModel avec l'application
class GeminiViewModelFactory(private val application: android.app.Application) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(GeminiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GeminiViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Factory pour créer le HistoryViewModel avec l'application
class HistoryViewModelFactory(private val application: android.app.Application) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun MainScreen(viewModel: GeminiViewModel) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var question by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo et titre
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Black,
            modifier = Modifier.size(100.dp)
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_isen_app),
                contentDescription = "Logo ISEN",
                modifier = Modifier.padding(8.dp)
            )
        }

        Text(
            text = "ISEN Smart Companion",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Liste des messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatMessages) { message ->
                ChatBubble(message = message)
            }
        }

        // Scroll to bottom when new message is added
        LaunchedEffect(chatMessages.size) {
            if (chatMessages.isNotEmpty()) {
                coroutineScope.launch {
                    listState.animateScrollToItem(chatMessages.size - 1)
                }
            }
        }

        // Champ de texte et bouton d'envoi
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                placeholder = { Text("Posez votre question") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (question.isNotBlank() && !isLoading) {
                            viewModel.sendMessage(question)
                            question = ""
                            keyboardController?.hide()
                        }
                    }
                ),
                singleLine = true
            )

            Button(
                onClick = {
                    if (question.isNotBlank() && !isLoading) {
                        viewModel.sendMessage(question)
                        question = ""
                        keyboardController?.hide()
                    }
                },
                enabled = !isLoading && question.isNotBlank(),
                contentPadding = PaddingValues(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Envoyer"
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val backgroundColor = if (message.isUserMessage)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.secondaryContainer

    val textColor = if (message.isUserMessage)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSecondaryContainer

    val bubbleAlignment = if (message.isUserMessage) Alignment.CenterEnd else Alignment.CenterStart


    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = bubbleAlignment
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = backgroundColor,
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .widthIn(max = 300.dp)
        ) {
            Text(
                text = message.content,
                color = textColor,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        val items = listOf(
            BottomNavItem("home", "Accueil", Icons.Default.Home),
            BottomNavItem("events", "Événements", Icons.Default.Event),
            BottomNavItem("history", "Historique", Icons.Default.History)
        )

        val currentRoute = currentRoute(navController)

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { navController.navigate(item.route) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

fun currentRoute(navController: NavController): String? {
    val navBackStackEntry = navController.currentBackStackEntry
    return navBackStackEntry?.destination?.route
}