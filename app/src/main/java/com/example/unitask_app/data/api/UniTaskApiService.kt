package com.example.unitask_app.data.api

import com.example.unitask_app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface UniTaskApiService {

    // --- Módulo: Autenticación ---
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("auth/profile")
    suspend fun getProfile(@Query("id") id: Int): Response<User>

    // --- Módulo: Materias ---
    @POST("subjects")
    suspend fun createSubject(@Body request: Subject): Response<Unit>

    @GET("subjects")
    suspend fun getSubjects(@Query("user_id") userId: Int): Response<List<Subject>>

    // --- Módulo: Tareas ---
    @GET("tasks")
    suspend fun getTasks(): Response<List<Task>>

    @POST("tasks")
    suspend fun createTask(@Body request: Task): Response<Unit>

    @PUT("tasks")
    suspend fun updateTask(@Body request: Task): Response<Unit>

    @PATCH("tasks/complete")
    suspend fun completeTask(@Query("id") id: Int): Response<Unit>

    @DELETE("tasks")
    suspend fun deleteTask(@Query("id") id: Int): Response<Unit>
}
