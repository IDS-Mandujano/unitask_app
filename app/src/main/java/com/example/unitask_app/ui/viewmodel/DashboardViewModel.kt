package com.example.unitask_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitask_app.data.local.TokenManager
import com.example.unitask_app.data.model.Subject
import com.example.unitask_app.data.model.Task
import com.example.unitask_app.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val tasks: List<Task>, val subjects: List<Subject>) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TaskRepository,
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

                if (tasksResponse.isSuccessful && subjectsResponse.isSuccessful) {
                    _uiState.value = DashboardState.Success(
                        tasks = tasksResponse.body() ?: emptyList(),
                        subjects = subjectsResponse.body() ?: emptyList()
                    )
                } else {
                    _uiState.value = DashboardState.Error("Error al cargar datos del servidor")
                }
            } catch (e: Exception) {
                _uiState.value = DashboardState.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }

    fun completeTask(taskId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.completeTask(taskId)
                if (response.isSuccessful) {
                    loadData(currentUserId)
                }
            } catch (e: Exception) { }
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

    fun createSubject(name: String) {
        viewModelScope.launch {
            try {
                val response = repository.createSubject(Subject(name = name, userId = currentUserId))
                if (response.isSuccessful) {
                    loadData(currentUserId)
                }
            } catch (e: Exception) { }
        }
    }

    fun createTask(title: String, desc: String, subjectId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.createTask(
                    Task(title = title, description = desc, subjectId = subjectId, dueDate = "2024-12-31T23:59:59Z")
                )
                if (response.isSuccessful) {
                    loadData(currentUserId)
                }
            } catch (e: Exception) { }
        }
    }
}
