package fr.isen.goutalguerin.isensmartcompanion

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.isen.goutalguerin.isensmartcompanion.ui.theme.ISENSmartCompanionTheme
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.widget.Toast
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Permission de notification refusée", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Event Reminders"
            val descriptionText = "Notifications pour les événements de l'agenda"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("event_reminders", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {}
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
                HistoryScreen(historyViewModel)
            }
            composable("agenda") {
                AgendaScreen()
            }
        }
    }
}

class GeminiViewModelFactory(private val application: android.app.Application) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(GeminiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GeminiViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class HistoryViewModelFactory(private val application: android.app.Application) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

val EventColors = listOf(
    Color(0xFF2196F3), // Bleu
    Color(0xFFFFEB3B), // Jaune
    Color(0xFF4CAF50), // Vert
    Color(0xFFF44336), // Rouge
    Color(0xFF9C27B0), // Violet
)

@Serializable
data class SerializableAgendaEvent(
    val timestamp: Long,
    val title: String,
    val isCourse: Boolean = false,
    val startTime: String? = null,
    val endTime: String? = null,
    val color: String? = null,
    val reminderEnabled: Boolean = false
) {
    fun toAgendaEvent(): AgendaEvent {
        return AgendaEvent(
            date = Calendar.getInstance().apply { timeInMillis = timestamp },
            title = title,
            isCourse = isCourse,
            startTime = startTime,
            endTime = endTime,
            color = try {
                color?.let { Color(android.graphics.Color.parseColor(it)) } ?: EventColors[0]
            } catch (e: IllegalArgumentException) {
                EventColors[0] // Couleur par défaut en cas d'erreur
            },
            reminderEnabled = reminderEnabled
        )
    }
}

data class AgendaEvent(
    val date: Calendar,
    val title: String,
    val isCourse: Boolean = false,
    val startTime: String? = null,
    val endTime: String? = null,
    val color: Color = EventColors[0],
    val reminderEnabled: Boolean = false
) {
    fun toSerializable(): SerializableAgendaEvent {
        return SerializableAgendaEvent(
            timestamp = date.timeInMillis,
            title = title,
            isCourse = isCourse,
            startTime = startTime,
            endTime = endTime,
            color = String.format("#%06X", (0xFFFFFF and color.value.toInt())),
            reminderEnabled = reminderEnabled
        )
    }

    fun getFormattedTimeRange(): String = when {
        startTime != null && endTime != null -> "$startTime-$endTime"
        startTime != null -> "à $startTime"
        else -> "Horaire non défini"
    }
}

@Composable
fun AgendaScreen() {
    val context = LocalContext.current
    val events = remember { mutableStateListOf<AgendaEvent>() }
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<AgendaEvent?>(null) }

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.FRENCH)
    val dayFormat = SimpleDateFormat("EEEE d MMMM", Locale.FRENCH)

    LaunchedEffect(Unit) {
        loadEvents(context, events)
    }

    LaunchedEffect(events.toList()) {
        saveEvents(context, events)
        events.forEach { NotificationScheduler.scheduleNotification(context, it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text(
                text = "Agenda",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            currentMonth = Calendar.getInstance().apply {
                                time = currentMonth.time
                                add(Calendar.MONTH, -1)
                            }
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                "Mois précédent",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = monthFormat.format(currentMonth.time).replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = {
                            currentMonth = Calendar.getInstance().apply {
                                time = currentMonth.time
                                add(Calendar.MONTH, 1)
                            }
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                "Mois suivant",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    CalendarGrid(
                        currentMonth = currentMonth,
                        events = events,
                        selectedDate = selectedDate,
                        onDayClick = { selectedDate = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = dayFormat.format(selectedDate.time).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            DailySchedule(
                events = events,
                selectedDate = selectedDate,
                onEdit = { event ->
                    eventToEdit = event
                    showAddDialog = true
                },
                onDelete = { events.remove(it) }
            )
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(64.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(
                Icons.Default.Add,
                "Ajouter un événement",
                modifier = Modifier.size(32.dp)
            )
        }
    }

    AnimatedVisibility(
        visible = showAddDialog,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        AddEditEventDialog(
            existingEvent = eventToEdit,
            selectedDate = selectedDate,
            onDismiss = {
                showAddDialog = false
                eventToEdit = null
            },
            onSave = { event ->
                if (eventToEdit != null) events.remove(eventToEdit)
                events.add(event)
                NotificationScheduler.scheduleNotification(context, event)
                showAddDialog = false
                eventToEdit = null
            }
        )
    }
}

@Composable
fun CalendarGrid(
    currentMonth: Calendar,
    events: List<AgendaEvent>,
    selectedDate: Calendar,
    onDayClick: (Calendar) -> Unit
) {
    val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = currentMonth.clone() as Calendar
    firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = (firstDayOfMonth.get(Calendar.DAY_OF_WEEK) + 5) % 7

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 8.dp)
    ) {
        val weekdays = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
        weekdays.forEach { day ->
            Text(
                text = day,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    LazyColumn(modifier = Modifier.height(280.dp)) {
        items((daysInMonth + firstDayOfWeek + 6) / 7) { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayOfWeek in 0 until 7) {
                    val dayPosition = week * 7 + dayOfWeek - firstDayOfWeek + 1
                    if (dayPosition in 1..daysInMonth) {
                        val dayCalendar = currentMonth.clone() as Calendar
                        dayCalendar.set(Calendar.DAY_OF_MONTH, dayPosition)
                        val dayEvents = events.filter { isSameDay(it.date, dayCalendar) }
                        val isToday = isSameDay(Calendar.getInstance(), dayCalendar)
                        val isSelected = isSameDay(dayCalendar, selectedDate)
                        val dominantColor = dayEvents.firstOrNull()?.color ?: Color.Transparent

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                        dayEvents.isNotEmpty() -> dominantColor.copy(alpha = 0.3f)
                                        isToday -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { onDayClick(dayCalendar) }
                                .border(
                                    width = if (isToday && !isSelected) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayPosition.toString(),
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                            if (dayEvents.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(dominantColor, CircleShape)
                                        .align(Alignment.BottomEnd)
                                        .offset((-4).dp, (-4).dp)
                                )
                            }
                        }
                    } else {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun DailySchedule(
    events: List<AgendaEvent>,
    selectedDate: Calendar,
    onEdit: (AgendaEvent) -> Unit,
    onDelete: (AgendaEvent) -> Unit
) {
    val dailyEvents = events.filter { isSameDay(it.date, selectedDate) }
        .sortedBy { it.startTime ?: "00:00" }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (dailyEvents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun événement prévu",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            items(dailyEvents) { event ->
                EventCard(event, onEdit, onDelete)
            }
        }
    }
}

@Composable
fun EventCard(
    event: AgendaEvent,
    onEdit: (AgendaEvent) -> Unit,
    onDelete: (AgendaEvent) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(56.dp)
                    .background(event.color, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.getFormattedTimeRange(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (event.isCourse) "Cours" else "Événement",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { onEdit(event) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        "Modifier",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (!event.isCourse) {
                    IconButton(
                        onClick = { onDelete(event) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            "Supprimer",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditEventDialog(
    existingEvent: AgendaEvent?,
    selectedDate: Calendar,
    onDismiss: () -> Unit,
    onSave: (AgendaEvent) -> Unit
) {
    var title by remember { mutableStateOf(existingEvent?.title ?: "") }
    var startHour by remember { mutableStateOf(existingEvent?.startTime?.split(":")?.get(0) ?: "") }
    var startMinute by remember { mutableStateOf(existingEvent?.startTime?.split(":")?.get(1) ?: "") }
    var endHour by remember { mutableStateOf(existingEvent?.endTime?.split(":")?.get(0) ?: "") }
    var endMinute by remember { mutableStateOf(existingEvent?.endTime?.split(":")?.get(1) ?: "") }
    var isCourse by remember { mutableStateOf(existingEvent?.isCourse ?: false) }
    var selectedColor by remember { mutableStateOf(existingEvent?.color ?: EventColors[0]) }
    var reminderEnabled by remember { mutableStateOf(existingEvent?.reminderEnabled ?: false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun validateTimes(): Boolean {
        if (startHour.isEmpty() || startMinute.isEmpty() || endHour.isEmpty() || endMinute.isEmpty()) return true
        val start = startHour.toInt() * 60 + startMinute.toInt()
        val end = endHour.toInt() * 60 + endMinute.toInt()
        return if (end <= start) {
            errorMessage = "L'heure de fin doit être après l'heure de début"
            false
        } else {
            errorMessage = null
            true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        title = {
            Text(
                text = if (existingEvent != null) "Modifier l'événement" else "Nouvel événement",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre de l'événement") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                TimeInputField(
                    hour = startHour,
                    minute = startMinute,
                    onHourChange = { if (it.length <= 2 && (it.isEmpty() || it.toIntOrNull() in 0..23)) startHour = it },
                    onMinuteChange = { if (it.length <= 2 && (it.isEmpty() || it.toIntOrNull() in 0..59)) startMinute = it },
                    label = "Heure de début"
                )
                Spacer(modifier = Modifier.height(16.dp))

                TimeInputField(
                    hour = endHour,
                    minute = endMinute,
                    onHourChange = { if (it.length <= 2 && (it.isEmpty() || it.toIntOrNull() in 0..23)) endHour = it },
                    onMinuteChange = { if (it.length <= 2 && (it.isEmpty() || it.toIntOrNull() in 0..59)) endMinute = it },
                    label = "Heure de fin"
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CheckboxWithLabel("Cours", isCourse) { isCourse = it }
                    CheckboxWithLabel("Ajouter un rappel", reminderEnabled) { reminderEnabled = it }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Couleur de l'événement",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EventColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = color }
                                .border(
                                    width = if (selectedColor == color) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && validateTimes()) {
                        val startTime = if (startHour.isNotEmpty() && startMinute.isNotEmpty()) {
                            String.format(Locale.FRENCH, "%02d:%02d", startHour.toInt(), startMinute.toInt())
                        } else null
                        val endTime = if (endHour.isNotEmpty() && endMinute.isNotEmpty()) {
                            String.format(Locale.FRENCH, "%02d:%02d", endHour.toInt(), endMinute.toInt())
                        } else null
                        val eventDate = (existingEvent?.date ?: selectedDate).clone() as Calendar
                        onSave(AgendaEvent(eventDate, title, isCourse, startTime, endTime, selectedColor, reminderEnabled))
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Enregistrer", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}

@Composable
fun TimeInputField(
    hour: String,
    minute: String,
    onHourChange: (String) -> Unit,
    onMinuteChange: (String) -> Unit,
    label: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = hour,
                onValueChange = onHourChange,
                label = { Text("HH") },
                modifier = Modifier.weight(1f),
                placeholder = { Text("00", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            Text(
                text = ":",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            OutlinedTextField(
                value = minute,
                onValueChange = onMinuteChange,
                label = { Text("MM") },
                modifier = Modifier.weight(1f),
                placeholder = { Text("00", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Composable
fun CheckboxWithLabel(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
            cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
}

private fun saveEvents(context: Context, events: SnapshotStateList<AgendaEvent>) {
    val prefs = context.getSharedPreferences("AgendaPrefs", Context.MODE_PRIVATE)
    val editor = prefs.edit()
    val serializableEvents = events.map { it.toSerializable() }
    val jsonString = Json.encodeToString(serializableEvents)
    editor.putString("events", jsonString)
    editor.apply()
}

private fun loadEvents(context: Context, events: SnapshotStateList<AgendaEvent>) {
    val prefs = context.getSharedPreferences("AgendaPrefs", Context.MODE_PRIVATE)
    val jsonString = prefs.getString("events", null)
    jsonString?.let { json ->
        try {
            val serializableEvents = Json.decodeFromString<List<SerializableAgendaEvent>>(json)
            events.clear()
            events.addAll(serializableEvents.map { it.toAgendaEvent() })
        } catch (e: Exception) {
            Toast.makeText(context, "Erreur lors du chargement des événements: ${e.message}", Toast.LENGTH_LONG).show()
        }
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
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
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
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
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
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Changer")
                }
            }

            OutlinedButton(
                onClick = { viewModel.clearConversation() },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.padding(start = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, "Réinitialiser", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Réinitialiser")
            }
        }

        AnimatedVisibility(visible = showModelSelector) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .padding(horizontal = 8.dp),
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
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                placeholder = { Text("Posez votre question ici...") },
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
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            IconButton(
                onClick = {
                    if (question.isNotBlank() && !isLoading) {
                        viewModel.sendMessage(question)
                        question = ""
                        keyboardController?.hide()
                    }
                },
                enabled = !isLoading && question.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        "Envoyer",
                        tint = MaterialTheme.colorScheme.onPrimary
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
    val backgroundColor = if (message.isUserMessage) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val textColor = if (message.isUserMessage) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

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
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                if (!message.isUserMessage) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isCopied = true
                                onCopyClicked(message.content)
                            }
                            .padding(top = 4.dp),
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
                            Icons.Default.ContentCopy,
                            "Copier",
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
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
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
                icon = { Icon(item.icon, item.label) },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                alwaysShowLabel = true // Affiche toujours les labels
            )
        }
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

fun currentRoute(navController: NavController): String? {
    return navController.currentBackStackEntry?.destination?.route
}