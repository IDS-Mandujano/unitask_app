package com.example.unitask_app.ui.viewmodel

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitask_app.domain.usecase.GetProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val connectivityManager: ConnectivityManager
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    fun getProfile(userId: Int) {
        if (!isNetworkAvailable()) {
            _profileState.value = ProfileState.Error("Sin conexión a internet. Verifica tu conexión.")
            return
        }
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                // Ahora pasamos el userId al caso de uso
                val response = getProfileUseCase(userId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _profileState.value = ProfileState.Success(it)
                    } ?: run {
                        _profileState.value = ProfileState.Error("Respuesta del servidor vacía")
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Sesión expirada. Inicia sesión nuevamente."
                        404 -> "Perfil no encontrado"
                        500 -> "Error interno del servidor"
                        else -> "Error del servidor: ${response.message()}"
                    }
                    _profileState.value = ProfileState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Error de conexión: ${e.localizedMessage}")
            }
        }
    }
}