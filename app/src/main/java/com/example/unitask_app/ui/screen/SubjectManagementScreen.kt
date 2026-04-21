package com.example.unitask_app.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.unitask_app.data.model.Subject
import com.example.unitask_app.ui.component.UniTaskButton
import com.example.unitask_app.ui.component.UniTaskTextField
import com.example.unitask_app.ui.viewmodel.SubjectUiState
import com.example.unitask_app.ui.viewmodel.SubjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectManagementScreen(
    navController: NavController,
    viewModel: SubjectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadSubjects()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Materias", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar materia")
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is SubjectUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is SubjectUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(12.dp))
                    UniTaskButton(text = "Reintentar", onClick = { viewModel.loadSubjects() })
                }
            }
            is SubjectUiState.Success -> {
                if (state.subjects.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No tienes materias registradas todavía")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.subjects) { subject ->
                            SubjectCard(
                                subject = subject,
                                onClick = { navController.navigate("subject_detail/${subject.id}") }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        SubjectFormDialog(
            title = "Nueva materia",
            subjectName = "",
            teacherName = "",
            teacherEmail = "",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, teacherName, teacherEmail ->
                viewModel.createSubject(name, teacherName, teacherEmail)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun SubjectCard(
    subject: Subject,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(subject.name, fontWeight = FontWeight.Bold)
                if (!subject.teacherName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(subject.teacherName, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text("ID: ${subject.id}", style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    navController: NavController,
    subjectId: Int,
    viewModel: SubjectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var editMode by remember { mutableStateOf(false) }

    LaunchedEffect(subjectId) {
        viewModel.loadSubject(subjectId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalle de materia", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is SubjectUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is SubjectUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is SubjectUiState.Success -> {
                val subject = state.selectedSubject
                if (subject == null) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Materia no encontrada")
                    }
                    return@Scaffold
                }

                var editedName by remember(subject.name) { mutableStateOf(subject.name) }
                var editedTeacherName by remember(subject.teacherName) { mutableStateOf(subject.teacherName.orEmpty()) }
                var editedTeacherEmail by remember(subject.teacherEmail) { mutableStateOf(subject.teacherEmail.orEmpty()) }

                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Materia #${subject.id}", style = MaterialTheme.typography.labelLarge)
                    if (editMode) {
                        UniTaskTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            label = "Nombre de la materia"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        UniTaskTextField(
                            value = editedTeacherName,
                            onValueChange = { editedTeacherName = it },
                            label = "Docente"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        UniTaskTextField(
                            value = editedTeacherEmail,
                            onValueChange = { editedTeacherEmail = it },
                            label = "Correo del docente"
                        )
                    } else {
                        Card(shape = RoundedCornerShape(18.dp)) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(subject.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                if (!subject.teacherName.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Docente: ${subject.teacherName}")
                                }
                                if (!subject.teacherEmail.isNullOrBlank()) {
                                    Text("Correo: ${subject.teacherEmail}")
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        FilledTonalButton(
                            onClick = {
                                if (editMode) {
                                    viewModel.updateSubject(subject.id, editedName, editedTeacherName, editedTeacherEmail)
                                }
                                editMode = !editMode
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (editMode) "Guardar" else "Editar")
                        }
                        Button(
                            onClick = {
                                viewModel.deleteSubject(subject.id)
                                navController.popBackStack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Eliminar")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tareas de esta materia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (state.tasks.isEmpty()) {
                        Text("Todavía no hay tareas para esta materia", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.tasks) { task ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { navController.navigate("task_detail/${task.id}") },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(task.title, fontWeight = FontWeight.Bold)
                                        Text(task.dueDate.take(16).replace("T", " "), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectFormDialog(
    title: String,
    subjectName: String,
    teacherName: String,
    teacherEmail: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(subjectName) }
    var teacher by remember { mutableStateOf(teacherName) }
    var email by remember { mutableStateOf(teacherEmail) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                UniTaskTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre de la materia"
                )
                Spacer(modifier = Modifier.height(8.dp))
                UniTaskTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = "Docente (opcional)"
                )
                Spacer(modifier = Modifier.height(8.dp))
                UniTaskTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo del docente (opcional)"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, teacher, email) }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}