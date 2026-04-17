package com.example.unitask_app.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.unitask_app.ui.component.UniTaskButton
import com.example.unitask_app.ui.viewmodel.DashboardState
import com.example.unitask_app.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    navController: NavController, 
    taskId: Int,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    val task = (uiState as? DashboardState.Success)?.tasks?.find { it.id == taskId }

    LaunchedEffect(Unit) {
        if (uiState !is DashboardState.Success) {
            viewModel.loadData()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Tarea", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        viewModel.deleteTask(taskId)
                        navController.popBackStack()
                        Toast.makeText(context, "Tarea eliminada", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        if (task == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(task.title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(task.dueDate.take(10), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Descripción", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(task.description, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (!task.isCompleted) {
                    UniTaskButton(
                        text = "Marcar como Completada",
                        onClick = { 
                            viewModel.completeTask(taskId)
                            navController.popBackStack()
                            Toast.makeText(context, "¡Tarea completada!", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Esta tarea ya fue completada", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    }
                }
            }
        }
    }
}
