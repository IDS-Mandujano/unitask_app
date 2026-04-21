package com.example.unitask_app.ui.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitask_app.data.model.Attachment
import com.example.unitask_app.data.model.Task
import com.example.unitask_app.data.repository.AttachmentRepository
import com.example.unitask_app.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TaskDetailUiState {
    object Loading : TaskDetailUiState()
    data class Success(
        val task: Task? = null,
        val attachments: List<Attachment> = emptyList()
    ) : TaskDetailUiState()
    data class Error(val message: String) : TaskDetailUiState()
}

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val attachmentRepository: AttachmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TaskDetailUiState>(TaskDetailUiState.Loading)
    val uiState: StateFlow<TaskDetailUiState> = _uiState

    private var currentTaskId: Int = 0

    private fun requireCurrentTaskId(): Int {
        if (currentTaskId <= 0) {
            throw IllegalStateException("Carga la tarea antes de subir adjuntos")
        }
        return currentTaskId
    }

    fun loadTask(taskId: Int) {
        currentTaskId = taskId
        viewModelScope.launch {
            _uiState.value = TaskDetailUiState.Loading
            try {
                val response = taskRepository.getTasks()
                if (response.isSuccessful) {
                    val task = response.body().orEmpty().find { it.id == taskId }
                    if (task != null) {
                        val attachments = attachmentRepository.getAttachments(taskId = taskId)
                        _uiState.value = TaskDetailUiState.Success(task = task, attachments = attachments)
                    } else {
                        _uiState.value = TaskDetailUiState.Error("Tarea no encontrada")
                    }
                } else {
                    _uiState.value = TaskDetailUiState.Error("No se pudo cargar la tarea")
                }
            } catch (e: Exception) {
                _uiState.value = TaskDetailUiState.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }

    fun refreshAttachments() {
        if (currentTaskId <= 0) return
        viewModelScope.launch {
            try {
                val currentTask = (uiState.value as? TaskDetailUiState.Success)?.task
                val attachments = attachmentRepository.getAttachments(taskId = currentTaskId)
                _uiState.value = TaskDetailUiState.Success(task = currentTask, attachments = attachments)
            } catch (e: Exception) {
                _uiState.value = TaskDetailUiState.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }

    suspend fun uploadPhoto(bitmap: Bitmap): Attachment {
        val attachment = attachmentRepository.uploadBitmap(bitmap, attachmentType = "photo", taskId = requireCurrentTaskId())
        refreshAttachments()
        return attachment
    }

    suspend fun uploadDocument(uri: Uri): Attachment {
        val attachment = attachmentRepository.uploadUri(uri, attachmentType = "document", taskId = requireCurrentTaskId())
        refreshAttachments()
        return attachment
    }

    fun completeTask(taskId: Int) {
        viewModelScope.launch {
            val response = taskRepository.completeTask(taskId)
            if (response.isSuccessful) {
                loadTask(taskId)
            }
        }
    }

    fun updateTask(task: Task, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = taskRepository.updateTask(task)
                if (response.isSuccessful) {
                    loadTask(task.id)
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (_: Exception) {
                onResult(false)
            }
        }
    }

    fun setTaskCompletion(task: Task, completed: Boolean, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = if (completed) {
                    taskRepository.completeTask(task.id)
                } else {
                    taskRepository.pendingTask(task.id)
                }

                if (response.isSuccessful) {
                    loadTask(task.id)
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (_: Exception) {
                onResult(false)
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            val response = taskRepository.deleteTask(taskId)
            if (response.isSuccessful) {
                _uiState.value = TaskDetailUiState.Loading
            }
        }
    }
}