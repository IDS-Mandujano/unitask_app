package com.example.unitask_app.ui.screen

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.unitask_app.ui.component.UniTaskButton
import com.example.unitask_app.ui.component.UniTaskTextField
import com.example.unitask_app.ui.viewmodel.AuthState
import com.example.unitask_app.ui.viewmodel.AuthViewModel
import com.google.firebase.messaging.FirebaseMessaging

private fun refreshAndRegisterFcmToken(viewModel: AuthViewModel) {
    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
        if (!token.isNullOrBlank()) {
            viewModel.onFcmTokenReceived(token)
        }
    }
}

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState(initial = false)
    val savedToken by viewModel.savedToken.collectAsState(initial = null)
    val savedUserId by viewModel.savedUserId.collectAsState(initial = null)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var biometricPromptRequested by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    // Detectar si el hardware soporta biométricos
    val biometricManager = remember { BiometricManager.from(context) }
    val canAuthenticate = remember {
        biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    LaunchedEffect(Unit) {
        // Registra token actual al iniciar pantalla (sin invalidarlo).
        refreshAndRegisterFcmToken(viewModel)
    }

    val hasSavedBiometricSession = biometricEnabled && !savedToken.isNullOrEmpty() && savedUserId?.let { it != -1 } == true

    LaunchedEffect(canAuthenticate, hasSavedBiometricSession, loginState) {
        if (canAuthenticate && hasSavedBiometricSession && loginState is AuthState.Idle && !biometricPromptRequested) {
            biometricPromptRequested = true
            showBiometricPrompt(context as FragmentActivity, viewModel)
        }
    }

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

                // BOTÓN DE HUELLA: Solo aparece si ya existe una sesión biométrica guardada.
                if (canAuthenticate && hasSavedBiometricSession) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            showBiometricPrompt(context as FragmentActivity, viewModel)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Ingresar con Huella")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("register") }) {
            Text("¿No tienes cuenta? Regístrate aquí", color = MaterialTheme.colorScheme.primary)
        }

        if (loginState is AuthState.Success) {
            val response = (loginState as AuthState.Success).data
            response?.user?.let { user ->
                LaunchedEffect(user.id) {
                    refreshAndRegisterFcmToken(viewModel)
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

private fun showBiometricPrompt(
    activity: FragmentActivity,
    viewModel: AuthViewModel
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // Aquí podrías implementar login automático si ya tienes credenciales guardadas
                // Por ahora, simulamos el éxito del login
                viewModel.biometricLoginSuccess()
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Inicio de Sesión Biométrico")
        .setSubtitle("Usa tu huella para acceder")
        .setNegativeButtonText("Cancelar")
        .build()

    biometricPrompt.authenticate(promptInfo)
}
