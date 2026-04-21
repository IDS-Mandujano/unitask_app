package com.example.unitask_app.ui.screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.unitask_app.data.model.Subject
import com.example.unitask_app.data.model.Task
import com.example.unitask_app.ui.component.SectionHeader
import com.example.unitask_app.ui.component.UniTaskButton
import com.example.unitask_app.ui.component.UniTaskTextField
import com.example.unitask_app.ui.util.formatDueDateForDisplay
import com.example.unitask_app.ui.viewmodel.DashboardState
import com.example.unitask_app.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    userId: Int,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadData(userId)
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        val userName = (uiState as? DashboardState.Success)?.user?.username ?: "Usuario"
                        Text("Hola, $userName", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Resumen de tus actividades",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("subjects") }) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Materias", modifier = Modifier.size(28.dp))
                    }
                    IconButton(onClick = { navController.navigate("profile/$userId") }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is DashboardState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is DashboardState.Success -> {
                    SectionHeader("Materias")
                    if (state.subjects.isEmpty()) {
                        Text("No hay materias", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.subjects) { subject ->
                                DashboardSubjectCard(
                                    subject = subject,
                                    onClick = { navController.navigate("subject_detail/${subject.id}") }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    SectionHeader("Tareas Pendientes")
                    if (state.tasks.isEmpty()) {
                        Text("No hay tareas", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        state.tasks.forEach { task ->
                            TaskItem(
                                task = task,
                                onClick = { navController.navigate("task_detail/${task.id}") },
                                onComplete = { viewModel.toggleTaskCompletion(task) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                is DashboardState.Error -> {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    UniTaskButton(text = "Reintentar", onClick = { viewModel.loadData(userId) })
                }
            }
        }
    }

    if (showAddDialog) {
        val subjects = (uiState as? DashboardState.Success)?.subjects ?: emptyList()
        AddEntityDialog(
            subjects = subjects,
            onDismiss = { showAddDialog = false },
            onAddSubject = { name, teacherName, teacherEmail ->
                viewModel.createSubject(name, teacherName, teacherEmail)
            },
            onAddTask = { title, desc, subjectId, dueDateIso ->
                viewModel.createTask(title, desc, subjectId, dueDateIso)
            }
        )
    }
}

@Composable
fun AddEntityDialog(
    subjects: List<Subject>,
    onDismiss: () -> Unit,
    onAddSubject: (String, String, String) -> Unit,
    onAddTask: (String, String, Int, String) -> Unit
) {
    val context = LocalContext.current
    val hasSubjects = subjects.isNotEmpty()
    var selectedTab by remember(subjects.size) { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("") }
    var teacherEmail by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableIntStateOf(subjects.firstOrNull()?.id ?: 0) }

    val now = remember { Calendar.getInstance() }
    var dueYear by remember { mutableIntStateOf(now.get(Calendar.YEAR)) }
    var dueMonth by remember { mutableIntStateOf(now.get(Calendar.MONTH)) }
    var dueDay by remember { mutableIntStateOf(now.get(Calendar.DAY_OF_MONTH)) }
    var dueHour by remember { mutableIntStateOf(now.get(Calendar.HOUR_OF_DAY)) }
    var dueMinute by remember { mutableIntStateOf(now.get(Calendar.MINUTE)) }

    fun selectedDueCalendar(): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, dueYear)
            set(Calendar.MONTH, dueMonth)
            set(Calendar.DAY_OF_MONTH, dueDay)
            set(Calendar.HOUR_OF_DAY, dueHour)
            set(Calendar.MINUTE, dueMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    fun formatDateLabel(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDueCalendar().time)
    }

    fun formatTimeLabel(): String {
        val pattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "hh:mm a"
        return SimpleDateFormat(pattern, Locale.getDefault()).format(selectedDueCalendar().time)
    }

    fun showDatePicker() {
        val minDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                dueYear = year
                dueMonth = month
                dueDay = dayOfMonth
            },
            dueYear,
            dueMonth,
            dueDay
        ).apply {
            datePicker.minDate = minDate.timeInMillis
        }.show()
    }

    fun showTimePicker() {
        val is24Hour = DateFormat.is24HourFormat(context)
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                dueHour = hourOfDay
                dueMinute = minute
            },
            dueHour,
            dueMinute,
            is24Hour
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Qué deseas agregar?") },
        text = {
            Column {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Materia", modifier = Modifier.padding(8.dp))
                    }
                    Tab(selected = selectedTab == 1, onClick = {
                        if (!hasSubjects) {
                            Toast.makeText(context, "Primero crea una materia", Toast.LENGTH_SHORT).show()
                            selectedTab = 0
                        } else {
                            selectedTab = 1
                        }
                    }) {
                        Text("Tarea", modifier = Modifier.padding(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    UniTaskTextField(value = name, onValueChange = { name = it }, label = "Nombre de la materia")
                    Spacer(modifier = Modifier.height(8.dp))
                    UniTaskTextField(value = teacherName, onValueChange = { teacherName = it }, label = "Docente (opcional)")
                    Spacer(modifier = Modifier.height(8.dp))
                    UniTaskTextField(value = teacherEmail, onValueChange = { teacherEmail = it }, label = "Correo del docente (opcional)")
                } else {
                    if (!hasSubjects) {
                        Text(
                            text = "Necesitas al menos una materia para crear tareas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    } else {
                        UniTaskTextField(value = title, onValueChange = { title = it }, label = "Título de la tarea")
                        Spacer(modifier = Modifier.height(8.dp))
                        UniTaskTextField(value = desc, onValueChange = { desc = it }, label = "Descripción")
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(onClick = { showDatePicker() }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fecha: ${formatDateLabel()}")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { showTimePicker() }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hora: ${formatTimeLabel()}")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Selecciona Materia:", style = MaterialTheme.typography.bodySmall)
                        subjects.forEach { subject ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { selectedSubjectId = subject.id }
                            ) {
                                RadioButton(selected = selectedSubjectId == subject.id, onClick = { selectedSubjectId = subject.id })
                                Text(subject.name)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            UniTaskButton(text = "Guardar", onClick = {
                if (selectedTab == 0) {
                    onAddSubject(name, teacherName, teacherEmail)
                } else {
                    if (!hasSubjects) {
                        Toast.makeText(context, "Primero crea una materia", Toast.LENGTH_SHORT).show()
                        return@UniTaskButton
                    }
                    val dueCalendar = selectedDueCalendar()
                    val nowCalendar = Calendar.getInstance()
                    if (dueCalendar.timeInMillis < nowCalendar.timeInMillis) {
                        Toast.makeText(context, "La fecha y hora no pueden ser anteriores a ahora", Toast.LENGTH_SHORT).show()
                        return@UniTaskButton
                    }
                    val dueDateIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(dueCalendar.time)
                    onAddTask(title, desc, selectedSubjectId, dueDateIso)
                }
                onDismiss()
            }, enabled = selectedTab == 0 || hasSubjects)
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun DashboardSubjectCard(subject: Subject, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(160.dp).wrapContentHeight().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(subject.name, fontWeight = FontWeight.Bold, maxLines = 2)
            if (!subject.teacherName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(subject.teacherName, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(task: Task, onClick: () -> Unit, onComplete: () -> Unit) {
    val context = LocalContext.current
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onComplete) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (task.isCompleted) "Marcar pendiente" else "Marcar completada",
                    tint = if (task.isCompleted) Color.Green else Color.Gray
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )
                Text(
                    formatDueDateForDisplay(task.dueDate, context),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}
