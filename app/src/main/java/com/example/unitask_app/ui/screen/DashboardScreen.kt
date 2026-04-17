package com.example.unitask_app.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.unitask_app.data.model.Subject
import com.example.unitask_app.data.model.Task
import com.example.unitask_app.ui.component.SectionHeader
import com.example.unitask_app.ui.component.UniTaskButton
import com.example.unitask_app.ui.component.UniTaskTextField
import com.example.unitask_app.ui.viewmodel.DashboardState
import com.example.unitask_app.ui.viewmodel.DashboardViewModel

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
                        Text("Hola, Eduardo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Resumen de tus actividades", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                },
                actions = {
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
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
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
                                SubjectCard(subject)
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
                                onComplete = { viewModel.completeTask(task.id) }
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
            onAddSubject = { name -> viewModel.createSubject(name) },
            onAddTask = { title, desc, subjectId -> viewModel.createTask(title, desc, subjectId) }
        )
    }
}

@Composable
fun AddEntityDialog(
    subjects: List<Subject>,
    onDismiss: () -> Unit,
    onAddSubject: (String) -> Unit,
    onAddTask: (String, String, Int) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableIntStateOf(subjects.firstOrNull()?.id ?: 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Qué deseas agregar?") },
        text = {
            Column {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Materia", modifier = Modifier.padding(8.dp))
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Tarea", modifier = Modifier.padding(8.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    UniTaskTextField(value = name, onValueChange = { name = it }, label = "Nombre de la materia")
                } else {
                    UniTaskTextField(value = title, onValueChange = { title = it }, label = "Título de la tarea")
                    Spacer(modifier = Modifier.height(8.dp))
                    UniTaskTextField(value = desc, onValueChange = { desc = it }, label = "Descripción")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Selecciona Materia:", style = MaterialTheme.typography.bodySmall)
                    subjects.forEach { subject ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedSubjectId = subject.id }) {
                            RadioButton(selected = selectedSubjectId == subject.id, onClick = { selectedSubjectId = subject.id })
                            Text(subject.name)
                        }
                    }
                }
            }
        },
        confirmButton = {
            UniTaskButton(text = "Guardar", onClick = {
                if (selectedTab == 0) onAddSubject(name) else onAddTask(title, desc, selectedSubjectId)
                onDismiss()
            })
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun SubjectCard(subject: Subject) {
    Card(
        modifier = Modifier.width(150.dp).height(90.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp).fillMaxSize(), contentAlignment = Alignment.CenterStart) {
            Text(subject.name, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(task: Task, onClick: () -> Unit, onComplete: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onComplete) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Completar",
                    tint = if (task.isCompleted) Color.Green else Color.Gray
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, fontWeight = FontWeight.Bold, textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null)
                Text(task.dueDate.take(10), fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}
