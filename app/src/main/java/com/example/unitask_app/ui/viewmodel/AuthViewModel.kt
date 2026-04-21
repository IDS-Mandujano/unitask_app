package com.example.unitask_app.ui.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitask_app.data.local.TokenManager
import com.example.unitask_app.data.model.*
import com.example.unitask_app.data.repository.UserRepository
import com.example.unitask_app.domain.usecase.LoginUseCase
import com.example.unitask_app.domain.usecase.RegisterUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager,
    private val connectivityManager: ConnectivityManager,
    private val vibrator: Vibrator
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState

    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState

    val biometricEnabled = tokenManager.isBiometricEnabled()
    val savedToken = tokenManager.getToken()
    val savedUserId = tokenManager.getUserId()

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    private fun vibrateError() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(180, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun validateTextField(value: String, fieldName: String): String? {
        if (value.isBlank()) return "$fieldName es requerido"
        val regex = Regex("^[\\p{L}0-9 .,_'-]+$")
        if (!regex.matches(value)) return "$fieldName contiene caracteres inválidos"
        if (!value.any { it.isLetter() }) return "$fieldName no puede ser solo números"
        return null
    }

    private fun validateEmail(email: String): String? {
        if (email.isBlank()) return "Correo es requerido"
        val regex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!regex.matches(email.trim())) return "Ingresa un correo válido"
        return null
    }

    private fun validatePassword(password: String): String? {
        if (password.length < 8) return "La contraseña debe tener al menos 8 caracteres"
        if (!password.any { it.isUpperCase() }) return "La contraseña debe incluir una letra mayúscula"
        if (!password.any { it.isDigit() }) return "La contraseña debe incluir un número"
        if (!password.any { !it.isLetterOrDigit() }) return "La contraseña debe incluir un símbolo"
        return null
    }

    private fun resolveDeviceId(): String {
        val androidId = Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
        return if (androidId.isNullOrBlank()) Build.MODEL ?: "" else androidId
    }

    private suspend fun syncDeviceTokenIfPossible(deviceToken: String) {
        if (deviceToken.isBlank()) return
        try {
            userRepository.registerDeviceToken(deviceToken = deviceToken, deviceId = resolveDeviceId())
        } catch (_: Exception) {
            // Se ignora para no bloquear autenticación por problemas temporales de red.
        }
    }

    fun onFcmTokenReceived(token: String) {
        viewModelScope.launch {
            tokenManager.saveFcmToken(token)
            val jwt = tokenManager.getToken().firstOrNull()
            if (!jwt.isNullOrBlank()) {
                syncDeviceTokenIfPossible(token)
            }
        }
    }

    fun login(email: String, password: String) {
        if (!isNetworkAvailable()) {
            _loginState.value = AuthState.Error("Sin conexión a internet.")
            vibrateError()
            return
        }

        validateEmail(email)?.let {
            _loginState.value = AuthState.Error(it)
            vibrateError()
            return
        }

        if (password.isBlank()) {
            _loginState.value = AuthState.Error("La contraseña es requerida")
            vibrateError()
            return
        }

        _loginState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = loginUseCase(LoginRequest(email, password))
                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        tokenManager.saveToken(authResponse.token, authResponse.user.id)
                        tokenManager.setBiometricEnabled(true)
                        val fcmToken = tokenManager.getFcmToken().firstOrNull()
                        if (!fcmToken.isNullOrBlank()) {
                            syncDeviceTokenIfPossible(fcmToken)
                        }
                        _loginState.value = AuthState.Success(authResponse)
                    } ?: run {
                        _loginState.value = AuthState.Error("Respuesta vacía")
                        vibrateError()
                    }
                } else {
                    _loginState.value = AuthState.Error("Credenciales incorrectas")
                    vibrateError()
                }
            } catch (e: Exception) {
                _loginState.value = AuthState.Error("Error: ${e.localizedMessage}")
                vibrateError()
            }
        }
    }

    // Función para manejar el éxito del login biométrico
    fun biometricLoginSuccess() {
        viewModelScope.launch {
            val token = tokenManager.getToken().firstOrNull()
            val lastUserId = tokenManager.getUserId().firstOrNull()
            if (!token.isNullOrBlank() && lastUserId != null && lastUserId != -1) {
                tokenManager.setBiometricEnabled(true)
                _loginState.value = AuthState.Success(
                    AuthResponse(token, User(lastUserId, "Usuario", "", "", ""))
                )
            } else {
                _loginState.value = AuthState.Error("Primero debes iniciar sesión manualmente una vez.")
                vibrateError()
            }
        }
    }

    fun register(username: String, email: String, password: String, career: String, university: String) {
        if (!isNetworkAvailable()) {
            _registerState.value = AuthState.Error("Sin conexión a internet.")
            vibrateError()
            return
        }

        validateTextField(username, "Nombre de usuario")?.let {
            _registerState.value = AuthState.Error(it)
            vibrateError()
            return
        }

        validateEmail(email)?.let {
            _registerState.value = AuthState.Error(it)
            vibrateError()
            return
        }

        validateTextField(career, "Carrera")?.let {
            _registerState.value = AuthState.Error(it)
            vibrateError()
            return
        }

        validateTextField(university, "Universidad")?.let {
            _registerState.value = AuthState.Error(it)
            vibrateError()
            return
        }

        validatePassword(password)?.let {
            _registerState.value = AuthState.Error(it)
            vibrateError()
            return
        }

        _registerState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = registerUseCase(RegisterRequest(username, email, password, career, university))
                if (response.isSuccessful) {
                    _registerState.value = AuthState.Success()
                    vibrateError()
                } else {
                    _registerState.value = AuthState.Error("Error en registro: ${response.code()}")
                    vibrateError()
                }
            } catch (e: Exception) {
                _registerState.value = AuthState.Error("Error: ${e.localizedMessage}")
                vibrateError()
            }
        }
    }
}
