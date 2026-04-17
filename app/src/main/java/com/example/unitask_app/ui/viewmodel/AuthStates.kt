package com.example.unitask_app.ui.viewmodel

import com.example.unitask_app.data.model.AuthResponse
import com.example.unitask_app.data.model.User

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val data: AuthResponse? = null) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Success(val user: User) : ProfileState()
    data class Error(val message: String) : ProfileState()
}
