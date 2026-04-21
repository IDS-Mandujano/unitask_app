package com.example.unitask_app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.unitask_app.ui.component.UniTaskButton
import com.example.unitask_app.ui.component.UniTaskTextField
import com.example.unitask_app.ui.viewmodel.AuthState
import com.example.unitask_app.ui.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val registerState by viewModel.registerState.collectAsState()
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var career by remember { mutableStateOf("") }
    var university by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Crear Cuenta",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Únete a la comunidad UniTask", 
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                UniTaskTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Nombre de Usuario",
                    leadingIcon = Icons.Default.Person
                )
                Spacer(modifier = Modifier.height(12.dp))
                UniTaskTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo Institucional",
                    leadingIcon = Icons.Default.Email
                )
                Spacer(modifier = Modifier.height(12.dp))
                UniTaskTextField(
                    value = career,
                    onValueChange = { career = it },
                    label = "Carrera (ej. Ing. Software)",
                    leadingIcon = Icons.Default.School
                )
                Spacer(modifier = Modifier.height(12.dp))
                UniTaskTextField(
                    value = university,
                    onValueChange = { university = it },
                    label = "Universidad",
                    leadingIcon = Icons.Default.Business
                )
                Spacer(modifier = Modifier.height(12.dp))
                UniTaskTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Contraseña",
                    isPassword = true,
                    leadingIcon = Icons.Default.Lock
                )
                Spacer(modifier = Modifier.height(24.dp))

                UniTaskButton(
                    text = "Registrarme",
                    onClick = { 
                        // CORREGIDO: Pasamos todos los parámetros
                        viewModel.register(username, email, password, career, university) 
                    },
                    enabled = registerState !is AuthState.Loading
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.popBackStack() }) {
            Text("¿Ya tienes cuenta? Inicia sesión", color = MaterialTheme.colorScheme.primary)
        }

        if (registerState is AuthState.Error) {
            Text(
                text = (registerState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        if (registerState is AuthState.Success) {
            LaunchedEffect(Unit) {
                navController.navigate("login") {
                    popUpTo("register") { inclusive = true }
                }
            }
        }
    }
}