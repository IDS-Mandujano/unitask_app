package com.example.unitask_app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.unitask_app.ui.component.UniTaskButton
import com.example.unitask_app.ui.viewmodel.ProfileState
import com.example.unitask_app.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userId: Int,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()

    LaunchedEffect(userId) {
        viewModel.getProfile(userId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = profileState) {
                is ProfileState.Loading -> CircularProgressIndicator()
                is ProfileState.Success -> {
                    val user = state.user
                    Text(user.username, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                    Text(user.email, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    ProfileOptionItem(Icons.Default.School, "Carrera", user.career)
                    ProfileOptionItem(Icons.Default.Business, "Universidad", user.university)
                }
                is ProfileState.Error -> {
                    Text("Error al cargar perfil", color = MaterialTheme.colorScheme.error)
                }
                else -> {}
            }

            Spacer(modifier = Modifier.weight(1f))

            UniTaskButton(
                text = "Cerrar Sesión",
                onClick = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                containerColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun ProfileOptionItem(icon: ImageVector, title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}
