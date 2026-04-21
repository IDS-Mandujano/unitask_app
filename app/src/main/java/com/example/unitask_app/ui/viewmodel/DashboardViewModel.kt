package com.example.unitask_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitask_app.data.local.TokenManager
import com.example.unitask_app.data.repository.AttachmentRepository
import com.example.unitask_app.data.model.Subject
import com.example.unitask_app.data.model.Task
import com.example.unitask_app.data.model.User
import com.example.unitask_app.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(
        val tasks: List<Task>, 
        val subjects: List<Subject>,
        val user: User? = null
    ) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val attachmentRepository: AttachmentRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val uiState: StateFlow<DashboardState> = _uiState

    private var currentUserId: Int = 0

    fun loadData(userId: Int? = null) {
        viewModelScope.launch {
            _uiState.value = DashboardState.Loading
            try {
                val id = userId ?: tokenManager.getUserId().first() ?: 0
                currentUserId = id
                
                val tasksResponse = repository.getTasks()
                val subjectsResponse = repository.getSubjects(id)
                val profileResponse = repository.getProfile(id)

                if (tasksResponse.isSuccessful && subjectsResponse.isSuccessful) {
                    _uiState.value = DashboardState.Success(
                        tasks = tasksResponse.body() ?: emptyList(),
                        subjects = subjectsResponse.body() ?: emptyList(),
                        user = profileResponse.body()
                    )
                } else {
                    _uiState.value = DashboardState.Error("Error al cargar datos del servidor")
                }
            } catch (e: Exception) {
                _uiState.value = DashboardState.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            try {
                val response = if (!task.isCompleted) {
                    repository.completeTask(task.id)
                } else {
                    repository.pendingTask(task.id)
                }
                if (response.isSuccessful) {
                    loadData(currentUserId)
                } else {
                    _uiState.value = DashboardState.Error("No se pudo actualizar el estado de la tarea")
                }
            } catch (e: Exception) {
                _uiState.value = DashboardState.Error(e.localizedMessage ?: "No se pudo actualizar la tarea")
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.deleteTask(taskId)
                if (response.isSuccessful) {
                    loadData(currentUserId)
                }
            } catch (e: Exception) { }
        }
    }

    fun createSubject(name: String, teacherName: String = "", teacherEmail: String = "") {
        if (name.isBlank()) {
            _uiState.value = DashboardState.Error("El nombre de la materia es requerido")
            return
        }
        if (!Regex("^[\\p{L}0-9 ._-]+$").matches(name)) {
            _uiState.value = DashboardState.Error("La materia solo puede contener letras y números")
            return
        }

        viewModelScope.launch {
            try {
                val response = repository.createSubject(
                    Subject(
                        name = name,
                        userId = currentUserId,
                        teacherName = teacherName.ifBlank { null },
                        teacherEmail = teacherEmail.ifBlank { null }
                    )
                )
                if (response.isSuccessful) {
                    loadData(currentUserId)
                } else {
                    _uiState.value = DashboardState.Error("No se pudo crear la materia")
                }
            } catch (e: Exception) {
                _uiState.value = DashboardState.Error(e.localizedMessage ?: "No se pudo crear la materia")
            }
        }
    }

    fun createTask(title: String, desc: String, subjectId: Int, dueDateIso: String) {
        if (title.isBlank()) {
            _uiState.value = DashboardState.Error("El título de la tarea es requerido")
            return
        }
        if (desc.isBlank()) {
            _uiState.value = DashboardState.Error("La descripción de la tarea es requerida")
            return
        }
        if (subjectId <= 0) {
            _uiState.value = DashboardState.Error("Selecciona una materia válida")
            return
        }
        if (dueDateIso.isBlank()) {
            _uiState.value = DashboardState.Error("Selecciona fecha y hora de entrega")
            return
        }

        viewModelScope.launch {
            try {
                val response = repository.createTask(
                    Task(title = title, description = desc, subjectId = subjectId, dueDate = dueDateIso)
                )
                if (response.isSuccessful) {
                    loadData(currentUserId)
                } else {
                    _uiState.value = DashboardState.Error("No se pudo crear la tarea. Verifica fecha/hora y materia")
                }
            } catch (e: Exception) {
                _uiState.value = DashboardState.Error(e.localizedMessage ?: "No se pudo crear la tarea")
            }
        }
    }

    suspend fun uploadQuickPhoto(bitmap: Bitmap) = attachmentRepository.uploadBitmap(bitmap, attachmentType = "photo")

    suspend fun uploadQuickDocument(uri: Uri) = attachmentRepository.uploadUri(uri, attachmentType = "document")
}
