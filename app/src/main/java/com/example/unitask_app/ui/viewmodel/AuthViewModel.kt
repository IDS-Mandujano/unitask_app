package com.example.unitask_app.ui.viewmodel

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitask_app.data.local.TokenManager
import com.example.unitask_app.data.model.*
import com.example.unitask_app.domain.usecase.LoginUseCase
import com.example.unitask_app.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val tokenManager: TokenManager, // INYECTAMOS TOKEN MANAGER
    private val connectivityManager: ConnectivityManager,
    private val vibrator: Vibrator
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState

    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    fun login(email: String, password: String) {
        if (!isNetworkAvailable()) {
            _loginState.value = AuthState.Error("Sin conexión a internet.")
            return
        }
        _loginState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = loginUseCase(LoginRequest(email, password))
                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        // --- PASO CLAVE: GUARDAR EL TOKEN Y ID REAL ---
                        tokenManager.saveToken(authResponse.token, authResponse.user.id)
                        
                        _loginState.value = AuthState.Success(authResponse)
                    } ?: run {
                        _loginState.value = AuthState.Error("Respuesta vacía")
                    }
                } else {
                    _loginState.value = AuthState.Error("Credenciales incorrectas")
                }
            } catch (e: Exception) {
                _loginState.value = AuthState.Error("Error: ${e.localizedMessage}")
            }
        }
    }

    fun register(username: String, email: String, password: String, career: String, university: String) {
        if (!isNetworkAvailable()) {
            _registerState.value = AuthState.Error("Sin conexión a internet.")
            return
        }
        _registerState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = registerUseCase(RegisterRequest(username, email, password, career, university))
                if (response.isSuccessful) {
                    _registerState.value = AuthState.Success()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                } else {
                    _registerState.value = AuthState.Error("Error en registro: ${response.code()}")
                }
            } catch (e: Exception) {
                _registerState.value = AuthState.Error("Error: ${e.localizedMessage}")
            }
        }
    }
}
