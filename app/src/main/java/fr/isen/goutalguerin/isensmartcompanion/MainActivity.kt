package fr.isen.goutalguerin.isensmartcompanion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.lifecycle.ViewModel // Import corrigé
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.text.style.TextOverflow
import android.widget.Toast
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.CalendarToday
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.services.calendar.CalendarScopes
import fr.isen.goutalguerin.isensmartcompanion.AgendaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    // Launcher pour la permission de notification
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission accordée
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher pour la connexion Google
    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            task.addOnSuccessListener {
                // Connexion réussie, vous pouvez recharger les événements si nécessaire
            }.addOnFailureListener {
                Toast.makeText(this, "Échec de la connexion Google", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermission()

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

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission déjà accordée
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
                    HistoryScreen(historyViewModel)
                }
                composable("agenda") {
                    val agendaViewModel: AgendaViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return AgendaViewModel(application) as T
                            }
                        }
                    )
                    AgendaScreen(agendaViewModel)
                }
            }
        }
    }

    @Composable
    fun AgendaScreen(viewModel: AgendaViewModel) {
        val agendaItems by viewModel.agendaItems
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Agenda",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Bouton de connexion si nécessaire
            if (agendaItems.any { it.title == "Connexion requise" }) {
                Button(onClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(com.google.android.gms.common.api.Scope(CalendarScopes.CALENDAR_READONLY))
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(this@MainActivity, gso)
                    signInLauncher.launch(googleSignInClient.signInIntent)
                }) {
                    Text("Se connecter à Google")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(agendaItems) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Début: ${dateFormat.format(Date(item.startTime))}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Fin: ${dateFormat.format(Date(item.endTime))}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (item.description.isNotEmpty()) {
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Factory pour GeminiViewModel
    class GeminiViewModelFactory(private val application: android.app.Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GeminiViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GeminiViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    // Factory pour HistoryViewModel
    class HistoryViewModelFactory(private val application: android.app.Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
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
        val selectedModel by viewModel.selectedModel.collectAsState()
        var question by remember { mutableStateOf("") }
        var showModelSelector by remember { mutableStateOf(false) }
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        val keyboardController = LocalSoftwareKeyboardController.current
        val context = LocalContext.current
        val clipboardManager = LocalClipboardManager.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Modèle: $selectedModel",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    Button(
                        onClick = { showModelSelector = !showModelSelector },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Changer")
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.clearConversation() },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Réinitialiser",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Réinitialiser")
                }
            }

            AnimatedVisibility(visible = showModelSelector) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        item {
                            Text(
                                text = "Choisir un modèle",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(viewModel.availableModels) { model ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setModel(model)
                                        showModelSelector = false
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = model == selectedModel,
                                    onClick = {
                                        viewModel.setModel(model)
                                        showModelSelector = false
                                    }
                                )
                                Text(
                                    text = model,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chatMessages) { message ->
                    ChatBubble(
                        message = message,
                        onCopyClicked = { text ->
                            clipboardManager.setText(AnnotatedString(text))
                            Toast.makeText(context, "Réponse copiée !", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

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

        LaunchedEffect(chatMessages.size) {
            if (chatMessages.isNotEmpty()) {
                coroutineScope.launch {
                    listState.animateScrollToItem(chatMessages.size - 1)
                }
            }
        }
    }

    @Composable
    fun ChatBubble(message: ChatMessage, onCopyClicked: (String) -> Unit = {}) {
        var isCopied by remember { mutableStateOf(false) }
        val backgroundColor = if (message.isUserMessage)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.secondaryContainer

        val textColor = if (message.isUserMessage)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onSecondaryContainer

        LaunchedEffect(isCopied) {
            if (isCopied) {
                delay(2000)
                isCopied = false
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = if (message.isUserMessage) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = backgroundColor,
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .widthIn(max = 300.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.content,
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    if (!message.isUserMessage) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isCopied = true
                                    onCopyClicked(message.content)
                                },
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isCopied) "Copié !" else "Copier",
                                color = textColor.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copier",
                                tint = textColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun BottomNavigationBar(navController: NavController) {
        NavigationBar {
            val items = listOf(
                BottomNavItem("home", "Accueil", Icons.Default.Home),
                BottomNavItem("events", "Événements", Icons.Default.Event),
                BottomNavItem("history", "Historique", Icons.Default.History),
                BottomNavItem("agenda", "Agenda", Icons.Default.CalendarToday)
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

    private fun currentRoute(navController: NavController): String? {
        val navBackStackEntry = navController.currentBackStackEntry
        return navBackStackEntry?.destination?.route
    }
}