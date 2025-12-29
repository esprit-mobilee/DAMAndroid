<<<<<<< HEAD
package com.example.esprit.ui.admin.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.Application
import com.example.esprit.model.User
import com.example.esprit.repository.ApplicationRepository
import com.example.esprit.repository.UserRepository
import com.example.esprit.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminApplicationsUiState(
    val isLoading: Boolean = false,
    val applications: List<Application> = emptyList(),
    val error: String? = null,
    val isUpdating: Boolean = false,
    val userNames: Map<String, String> = emptyMap() // Cache des noms d'utilisateurs
)

@HiltViewModel
class AdminApplicationsViewModel @Inject constructor(
    private val repository: ApplicationRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdminApplicationsUiState())
    val uiState: StateFlow<AdminApplicationsUiState> = _uiState
    
    fun loadAllApplications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val all = repository.getAllApplications()) {
                is Resource.Success -> {
                    val applications = all.data ?: emptyList()
                    // Charger les noms des utilisateurs
                    val userNamesMap = mutableMapOf<String, String>()
                    applications.forEach { app ->
                        app.userId?.let { userId ->
                            if (!userNamesMap.containsKey(userId)) {
                                try {
                                    val user = userRepository.getUserById(userId)
                                    userNamesMap[userId] = user.fullName
                                } catch (e: Exception) {
                                    userNamesMap[userId] = userId // Fallback sur l'ID si erreur
                                }
                            }
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        applications = applications,
                        userNames = userNamesMap,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = all.message
                    )
                }
                else -> {}
            }
        }
    }
    
    fun getUserName(userId: String?): String {
        if (userId == null) return "Étudiant inconnu"
        return _uiState.value.userNames[userId] ?: userId
    }
    
    fun updateApplicationStatus(applicationId: String, status: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)
            when (val res = repository.updateApplicationStatus(applicationId, status = status)) {
                is Resource.Success -> {
                    // Recharger la liste
                    loadAllApplications()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = res.message
                    )
                }
                else -> {}
            }
        }
    }
    
    fun deleteApplication(applicationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)
            when (val res = repository.deleteApplication(applicationId)) {
                is Resource.Success -> {
                    // Recharger la liste
                    loadAllApplications()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = res.message
                    )
                }
                else -> {}
            }
        }
    }
}

=======
package com.example.esprit.ui.admin.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.Application
import com.example.esprit.model.User
import com.example.esprit.repository.ApplicationRepository
import com.example.esprit.repository.UserRepository
import com.example.esprit.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminApplicationsUiState(
    val isLoading: Boolean = false,
    val applications: List<Application> = emptyList(),
    val error: String? = null,
    val isUpdating: Boolean = false,
    val userNames: Map<String, String> = emptyMap() // Cache des noms d'utilisateurs
)

@HiltViewModel
class AdminApplicationsViewModel @Inject constructor(
    private val repository: ApplicationRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdminApplicationsUiState())
    val uiState: StateFlow<AdminApplicationsUiState> = _uiState
    
    fun loadAllApplications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val all = repository.getAllApplications()) {
                is Resource.Success -> {
                    val applications = all.data ?: emptyList()
                    // Charger les noms des utilisateurs
                    val userNamesMap = mutableMapOf<String, String>()
                    applications.forEach { app ->
                        app.userId?.let { userId ->
                            if (!userNamesMap.containsKey(userId)) {
                                try {
                                    val user = userRepository.getUserById(userId)
                                    userNamesMap[userId] = user.fullName
                                } catch (e: Exception) {
                                    userNamesMap[userId] = userId // Fallback sur l'ID si erreur
                                }
                            }
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        applications = applications,
                        userNames = userNamesMap,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = all.message
                    )
                }
                else -> {}
            }
        }
    }
    
    fun getUserName(userId: String?): String {
        if (userId == null) return "Étudiant inconnu"
        return _uiState.value.userNames[userId] ?: userId
    }
    
    fun updateApplicationStatus(applicationId: String, status: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)
            when (val res = repository.updateApplicationStatus(applicationId, status = status)) {
                is Resource.Success -> {
                    // Recharger la liste
                    loadAllApplications()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = res.message
                    )
                }
                else -> {}
            }
        }
    }
    
    fun deleteApplication(applicationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)
            when (val res = repository.deleteApplication(applicationId)) {
                is Resource.Success -> {
                    // Recharger la liste
                    loadAllApplications()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = res.message
                    )
                }
                else -> {}
            }
        }
    }
}

>>>>>>> origin/messaging-announcement
