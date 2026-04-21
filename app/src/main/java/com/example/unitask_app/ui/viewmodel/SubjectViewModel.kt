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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SubjectUiState {
    object Loading : SubjectUiState()
    data class Success(
        val subjects: List<Subject> = emptyList(),
        val selectedSubject: Subject? = null,
        val tasks: List<Task> = emptyList()
    ) : SubjectUiState()
    data class Error(val message: String) : SubjectUiState()
}

@HiltViewModel
class SubjectViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SubjectUiState>(SubjectUiState.Loading)
    val uiState: StateFlow<SubjectUiState> = _uiState

    private var currentUserId: Int = 0

    fun loadSubjects() {
        viewModelScope.launch {
            _uiState.value = SubjectUiState.Loading
            try {
                currentUserId = tokenManager.getUserId().firstOrNull() ?: 0
                val response = repository.getSubjects(currentUserId)
                if (response.isSuccessful) {
                    _uiState.value = SubjectUiState.Success(subjects = response.body() ?: emptyList())
                } else {
                    _uiState.value = SubjectUiState.Error("No se pudieron cargar las materias")
                }
            } catch (e: Exception) {
                _uiState.value = SubjectUiState.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }

    fun loadSubject(subjectId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getSubject(subjectId)
                val tasksResponse = repository.getTasks(subjectId)
                if (response.isSuccessful && tasksResponse.isSuccessful) {
                    val currentSubjects = (uiState.value as? SubjectUiState.Success)?.subjects ?: emptyList()
                    _uiState.value = SubjectUiState.Success(
                        subjects = currentSubjects,
                        selectedSubject = response.body(),
                        tasks = tasksResponse.body() ?: emptyList()
                    )
                } else {
                    _uiState.value = SubjectUiState.Error("Materia no encontrada")
                }
            } catch (e: Exception) {
                _uiState.value = SubjectUiState.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }

    fun createSubject(name: String, teacherName: String = "", teacherEmail: String = "") {
        if (name.isBlank()) {
            _uiState.value = SubjectUiState.Error("El nombre de la materia es requerido")
            return
        }
        if (!Regex("^[\\p{L}0-9 ._-]+$").matches(name)) {
            _uiState.value = SubjectUiState.Error("La materia solo puede contener letras y números")
            return
        }

        viewModelScope.launch {
            try {
                val response = repository.createSubject(
                    Subject(
                        userId = currentUserId,
                        name = name,
                        teacherName = teacherName.ifBlank { null },
                        teacherEmail = teacherEmail.ifBlank { null }
                    )
                )
                if (response.isSuccessful) {
                    loadSubjects()
                } else {
                    _uiState.value = SubjectUiState.Error("No se pudo crear la materia")
                }
            } catch (e: Exception) {
                _uiState.value = SubjectUiState.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }

    fun updateSubject(subjectId: Int, name: String, teacherName: String = "", teacherEmail: String = "") {
        if (name.isBlank()) {
            _uiState.value = SubjectUiState.Error("El nombre de la materia es requerido")
            return
        }
        if (!Regex("^[\\p{L}0-9 ._-]+$").matches(name)) {
            _uiState.value = SubjectUiState.Error("La materia solo puede contener letras y números")
            return
        }

        viewModelScope.launch {
            try {
                val response = repository.updateSubject(
                    subjectId,
                    Subject(
                        id = subjectId,
                        userId = currentUserId,
                        name = name,
                        teacherName = teacherName.ifBlank { null },
                        teacherEmail = teacherEmail.ifBlank { null }
                    )
                )
                if (response.isSuccessful) {
                    loadSubjects()
                    loadSubject(subjectId)
                } else {
                    _uiState.value = SubjectUiState.Error("No se pudo actualizar la materia")
                }
            } catch (e: Exception) {
                _uiState.value = SubjectUiState.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }

    fun deleteSubject(subjectId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.deleteSubject(subjectId)
                if (response.isSuccessful) {
                    loadSubjects()
                } else {
                    _uiState.value = SubjectUiState.Error("No se pudo eliminar la materia")
                }
            } catch (e: Exception) {
                _uiState.value = SubjectUiState.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }
}