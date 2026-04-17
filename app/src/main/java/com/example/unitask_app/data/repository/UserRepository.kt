package com.example.unitask_app.data.repository

import com.example.unitask_app.data.api.UniTaskApiService
import com.example.unitask_app.data.model.AuthResponse
import com.example.unitask_app.data.model.LoginRequest
import com.example.unitask_app.data.model.RegisterRequest
import com.example.unitask_app.data.model.User
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: UniTaskApiService
) {

    suspend fun login(request: LoginRequest): Response<AuthResponse> {
        return apiService.login(request)
    }

    suspend fun register(request: RegisterRequest): Response<Unit> {
        return apiService.register(request)
    }

    suspend fun getProfile(userId: Int): Response<User> {
        return apiService.getProfile(userId)
    }
}
