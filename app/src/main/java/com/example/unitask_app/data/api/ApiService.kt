package com.example.unitask_app.data.api

import com.example.unitask_app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("auth/profile")
    suspend fun getProfile(@Query("id") userId: Int): Response<User>

    // Subjects (Materias)
    @GET("subjects")
    suspend fun getSubjects(): Response<List<Subject>>

    @POST("subjects")
    suspend fun createSubject(@Body subject: Subject): Response<Subject>

    // Tasks (Tareas)
    @GET("tasks")
    suspend fun getTasks(@Query("subjectId") subjectId: Int? = null): Response<List<Task>>

    @POST("tasks")
    suspend fun createTask(@Body task: Task): Response<Task>

    @PUT("tasks/{id}")
    suspend fun updateTask(@Path("id") id: Int, @Body task: Task): Response<Task>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Int): Response<Unit>

    @PATCH("tasks/{id}/complete")
    suspend fun toggleTaskComplete(@Path("id") id: Int): Response<Task>
}