package com.example.unitask_app.domain.usecase

import com.example.unitask_app.data.model.AuthResponse
import com.example.unitask_app.data.model.LoginRequest
import com.example.unitask_app.data.model.RegisterRequest
import com.example.unitask_app.data.model.User
import com.example.unitask_app.data.repository.UserRepository
import retrofit2.Response
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(request: LoginRequest): Response<AuthResponse> {
        return repository.login(request)
    }
}

class RegisterUseCase @Inject constructor(
    private val repository: UserRepository
) {
    // CORREGIDO: Ahora retorna Response<Unit> para coincidir con el backend de Go
    suspend operator fun invoke(request: RegisterRequest): Response<Unit> {
        return repository.register(request)
    }
}

class GetProfileUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userId: Int): Response<User> {
        return repository.getProfile(userId)
    }
}
