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

    // --- Módulo: Notificaciones ---
    @POST("notifications/device-token")
    suspend fun registerDeviceToken(@Body request: DeviceTokenRequest): Response<Unit>

    @POST("notifications/device-token/remove")
    suspend fun unregisterDeviceToken(@Body request: DeviceTokenRequest): Response<Unit>

    // --- Módulo: Materias ---
    @POST("subjects")
    suspend fun createSubject(@Body request: Subject): Response<Unit>

    @GET("subjects")
    suspend fun getSubjects(@Query("user_id") userId: Int): Response<List<Subject>>

    @GET("subjects/{id}")
    suspend fun getSubject(@Path("id") id: Int): Response<Subject>

    @PUT("subjects/{id}")
    suspend fun updateSubject(@Path("id") id: Int, @Body request: Subject): Response<Unit>

    @DELETE("subjects/{id}")
    suspend fun deleteSubject(@Path("id") id: Int): Response<Unit>

    // --- Módulo: Tareas ---
    @GET("tasks")
    suspend fun getTasks(@Query("subject_id") subjectId: Int? = null): Response<List<Task>>

    @POST("tasks")
    suspend fun createTask(@Body request: Task): Response<Unit>

    @PUT("tasks")
    suspend fun updateTask(@Body request: Task): Response<Unit>

    @PATCH("tasks/complete")
    suspend fun completeTask(@Query("id") id: Int): Response<Unit>

    @DELETE("tasks")
    suspend fun deleteTask(@Query("id") id: Int): Response<Unit>

    // --- Módulo: Adjuntos ---
    @GET("attachments")
    suspend fun getAttachments(
        @Query("task_id") taskId: Int? = null,
        @Query("subject_id") subjectId: Int? = null
    ): Response<List<Attachment>>

    @POST("attachments")
    suspend fun registerAttachment(@Body request: AttachmentRequest): Response<Attachment>

    @DELETE("attachments")
    suspend fun deleteAttachment(@Query("id") id: Int): Response<Unit>
}
