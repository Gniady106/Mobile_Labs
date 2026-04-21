package pl.wsei.pam.lab06

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pl.wsei.pam.lab06.ui.theme.Lab06Theme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.jvm.java


enum class Priority { High, Medium, Low }

data class TodoTask(
    val title: String,
    val deadline: LocalDate,
    val isDone: Boolean,
    val priority: Priority
)



class TodoViewModel : ViewModel() {


    val tasks = mutableStateListOf(
        TodoTask("Programming", LocalDate.of(2024, 4, 18), false, Priority.Low),
        TodoTask("Teaching",    LocalDate.of(2024, 5, 12), false, Priority.High),
        TodoTask("Learning",    LocalDate.of(2024, 6, 28), false,  Priority.Low),
        TodoTask("Cooking",     LocalDate.of(2024, 8, 18), false, Priority.Medium),
    )

    fun addTask(task: TodoTask) {
        tasks.add(task)
    }
}



class lab06Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Lab06Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}



@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val todoViewModel: TodoViewModel = viewModel()

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            ListScreen(navController = navController, viewModel = todoViewModel)
        }
        composable("form") {
            FormScreen(navController = navController, viewModel = todoViewModel)
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    navController: NavController,
    title: String,
    showBackIcon: Boolean,
    route: String
) {
    val context = LocalContext.current

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        title = { Text(text = title) },
        navigationIcon = {
            if (showBackIcon) {
                IconButton(onClick = { navController.navigate(route) }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            if (route != "form") {
                OutlinedButton(onClick = { navController.navigate("list") }) {
                    Text(text = "Zapisz", fontSize = 18.sp)
                }
            } else {
                IconButton(onClick = { }) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                }
                IconButton(onClick = {
                    val intent = Intent(
                        context,
                        pl.wsei.pam.lab06.lab06Activity::class.java
                    )
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    context.startActivity(intent)
                }) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                }
            }
        }
    )
}



fun priorityColor(priority: Priority): Color = when (priority) {
    Priority.High   -> Color(0xFFE53935)
    Priority.Medium -> Color(0xFFFB8C00)
    Priority.Low    -> Color(0xFF43A047)
}



@Composable
fun ListItem(item: TodoTask, modifier: Modifier = Modifier) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .width(6.dp)
                    .height(72.dp),
                color = priorityColor(item.priority),
                shape = MaterialTheme.shapes.small
            ) {}

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Termin: ${item.deadline.format(formatter)}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Priorytet: ${item.priority.name}",
                    fontSize = 13.sp,
                    color = priorityColor(item.priority)
                )
            }

            Checkbox(
                checked = item.isDone,
                onCheckedChange = null
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(navController: NavController, viewModel: TodoViewModel) {
    Scaffold(
        topBar = {
            AppTopBar(
                navController = navController,
                title = "List",
                showBackIcon = false,
                route = "form"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                shape = CircleShape,
                onClick = { navController.navigate("form") }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add task",
                    modifier = Modifier.scale(1.5f)
                )
            }
        },
        content = { paddingValues ->
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                    items(items = viewModel.tasks) { item ->
                    ListItem(item = item)
                }
            }
        }
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(navController: NavController, viewModel: TodoViewModel) {
    var title          by remember { mutableStateOf("") }
    var isDone         by remember { mutableStateOf(false) }
    var priority       by remember { mutableStateOf(Priority.Low) }
    var showDatePicker by remember { mutableStateOf(false) }
    var titleError     by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = today
            .atStartOfDay(java.time.ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    )


    val selectedDate: LocalDate = datePickerState.selectedDateMillis?.let { millis ->
        java.time.Instant.ofEpochMilli(millis)
            .atZone(java.time.ZoneOffset.UTC)
            .toLocalDate()
    } ?: today

    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Anuluj") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    fun saveTask() {
        if (title.isBlank()) {
            titleError = true
            return
        }
        viewModel.addTask(
            TodoTask(
                title    = title.trim(),
                deadline = selectedDate,
                isDone   = isDone,
                priority = priority
            )
        )

        navController.navigate("list") {
            popUpTo("list") { inclusive = false }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                navController = navController,
                title = "Form",
                showBackIcon = true,
                route = "list"
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = false
                    },
                    label = { Text("Tytuł zadania") },
                    isError = titleError,
                    supportingText = {
                        if (titleError) Text(
                            "Tytuł nie może być pusty",
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Termin: ${selectedDate.format(formatter)}", fontSize = 16.sp)
                }

                Text("Priorytet:", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Priority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = priorityColor(p).copy(alpha = 0.2f),
                                selectedLabelColor     = priorityColor(p)
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Ukończone", modifier = Modifier.weight(1f), fontSize = 16.sp)
                    Switch(checked = isDone, onCheckedChange = { isDone = it })
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { saveTask() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Zapisz", fontSize = 18.sp)
                }
            }
        }
    )
}