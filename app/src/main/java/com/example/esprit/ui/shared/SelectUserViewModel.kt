package com.example.esprit.ui.shared

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.User
import com.example.esprit.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectUserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var users = mutableStateListOf<User>()
        private set

    var isLoading = mutableStateOf(false)
        private set

    fun loadUsers(currentUserId: String?) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val allUsers = userRepository.getAllUsers()
                users.clear()
                users.addAll(
                    allUsers.filter { it.id != currentUserId }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }
}
