package com.example.unitask_app.data.repository

import com.example.unitask_app.data.api.UniTaskApiService
import com.example.unitask_app.data.model.Attachment
import com.example.unitask_app.data.model.AttachmentRequest
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
    suspend fun getSubject(id: Int) = apiService.getSubject(id)
    suspend fun updateSubject(id: Int, subject: Subject) = apiService.updateSubject(id, subject)
    suspend fun deleteSubject(id: Int) = apiService.deleteSubject(id)

    suspend fun getTasks(subjectId: Int? = null): Response<List<Task>> = apiService.getTasks(subjectId)
    suspend fun createTask(task: Task) = apiService.createTask(task)
    suspend fun updateTask(task: Task) = apiService.updateTask(task)
    suspend fun completeTask(id: Int) = apiService.completeTask(id)
    suspend fun pendingTask(id: Int) = apiService.pendingTask(id)
    suspend fun deleteTask(id: Int) = apiService.deleteTask(id)

    suspend fun getAttachments(taskId: Int? = null, subjectId: Int? = null): Response<List<Attachment>> = apiService.getAttachments(taskId, subjectId)
    suspend fun registerAttachment(request: AttachmentRequest) = apiService.registerAttachment(request)
    suspend fun deleteAttachment(id: Int) = apiService.deleteAttachment(id)
}
