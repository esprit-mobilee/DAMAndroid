package com.example.esprit.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.Role
import com.example.esprit.repository.AuthRepository
import com.example.esprit.util.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val dataStore: DataStoreManager,
    private val repo: AuthRepository
) : ViewModel() {

    fun checkAuth(onResult: (Boolean, Role?) -> Unit) {
        viewModelScope.launch {
            val token = dataStore.tokenFlow.first()
            if (token.isNullOrEmpty()) {
                onResult(false, null)
            } else {
                try {
                    val user = repo.me(token)

                    // ✅ Map the backend string role -> our enum Role
                    val role = when (user.role.uppercase()) {
                        "STUDENT" -> Role.STUDENT
                        "TEACHER" -> Role.TEACHER
                        "PARENT"  -> Role.PARENT
                        "ADMIN"   -> Role.ADMIN
                        else      -> Role.STUDENT // fallback if unknown
                    }

                    onResult(true, role)
                } catch (e: Exception) {
                    onResult(false, null)
                }
            }
        }
    }
}
