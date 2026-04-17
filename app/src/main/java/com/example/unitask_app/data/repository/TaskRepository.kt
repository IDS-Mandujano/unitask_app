package com.example.unitask_app.data.repository

import com.example.unitask_app.data.api.UniTaskApiService
import com.example.unitask_app.data.model.Subject
import com.example.unitask_app.data.model.Task
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val apiService: UniTaskApiService
) {
    suspend fun register(request: com.example.unitask_app.data.model.RegisterRequest) = apiService.register(request)
    suspend fun login(request: com.example.unitask_app.data.model.LoginRequest) = apiService.login(request)
    suspend fun getProfile(id: Int) = apiService.getProfile(id)

    suspend fun getSubjects(userId: Int): Response<List<Subject>> = apiService.getSubjects(userId)
    suspend fun createSubject(subject: Subject) = apiService.createSubject(subject)

    suspend fun getTasks(): Response<List<Task>> = apiService.getTasks()
    suspend fun createTask(task: Task) = apiService.createTask(task)
    suspend fun updateTask(task: Task) = apiService.updateTask(task)
    suspend fun completeTask(id: Int) = apiService.completeTask(id)
    suspend fun deleteTask(id: Int) = apiService.deleteTask(id)
}
