package com.example.unitask_app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "UniTask",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Gestiona tus tareas académicas",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontSize = 16.sp,
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
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo Institucional",
                    leadingIcon = Icons.Default.Email
                )
                Spacer(modifier = Modifier.height(16.dp))
                UniTaskTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Contraseña",
                    isPassword = true,
                    leadingIcon = Icons.Default.Lock
                )
                Spacer(modifier = Modifier.height(24.dp))

                UniTaskButton(
                    text = "Iniciar Sesión",
                    onClick = { viewModel.login(email, password) },
                    enabled = loginState !is AuthState.Loading
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("register") }) {
            Text("¿No tienes cuenta? Regístrate aquí", color = MaterialTheme.colorScheme.primary)
        }

        // CORRECCIÓN: Manejo de nulidad seguro
        if (loginState is AuthState.Success) {
            val response = (loginState as AuthState.Success).data
            response?.user?.let { user ->
                LaunchedEffect(user.id) {
                    navController.navigate("dashboard/${user.id}") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
        }

        if (loginState is AuthState.Error) {
            Text(
                text = (loginState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
