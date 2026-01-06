package com.example.esprit.ui.admin.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.User
import com.example.esprit.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UsersUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreating: Boolean = false,
    val createError: String? = null,
    val createSuccess: Boolean = false
)

@HiltViewModel
class AdminUsersViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsersUiState())
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val users = apiService.getAllUsers()
                _uiState.update { it.copy(users = users, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun createUser(userMap: Map<String, Any>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, createError = null, createSuccess = false) }
            try {
                apiService.createUser(userMap)
                _uiState.update { it.copy(isCreating = false, createSuccess = true) }
                loadUsers() // Refresh list
            } catch (e: Exception) {
                _uiState.update { it.copy(isCreating = false, createError = e.message) }
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            try {
                apiService.deleteUser(userId)
                // Optimistically remove from list
                val currentUsers = _uiState.value.users.filter { it.id != userId }
                _uiState.update { it.copy(users = currentUsers) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete: ${e.message}") }
            }
        }
    }
    
    fun resetCreateState() {
        _uiState.update { it.copy(createSuccess = false, createError = null) }
    }
}
