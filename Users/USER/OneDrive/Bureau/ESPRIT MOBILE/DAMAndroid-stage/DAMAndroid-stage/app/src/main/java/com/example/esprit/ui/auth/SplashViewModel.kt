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
            try {
                android.util.Log.d("SplashViewModel", "Checking authentication...")
                // Use synchronous getters from SharedPreferences
                val token = dataStore.getToken()
                val cachedRole = dataStore.getRole()
                android.util.Log.d("SplashViewModel", "Token found: ${!token.isNullOrEmpty()}, Role: $cachedRole")
                
                if (token.isNullOrEmpty()) {
                    android.util.Log.d("SplashViewModel", "No token, navigating to login")
                    onResult(false, null)
                } else {
                    try {
                        android.util.Log.d("SplashViewModel", "Token exists, verifying with API...")
                        val user = repo.me()
                        android.util.Log.d("SplashViewModel", "API check successful. User: ${user.name}, Role: ${user.role}")
                        val finalRole = pickBestRole(user.role, user.presidentOf != null)
                        onResult(true, finalRole)
                    } catch (e: Exception) {
                        android.util.Log.e("SplashViewModel", "API check failed", e)
                        cachedRole?.let { 
                            android.util.Log.d("SplashViewModel", "Using cached role: $it")
                            onResult(true, pickBestRole(it, false)) 
                        } ?: run {
                            android.util.Log.d("SplashViewModel", "No cached role, navigating to login")
                            onResult(false, null)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SplashViewModel", "Critical error in checkAuth", e)
                e.printStackTrace()
                onResult(false, null)
            }
        }
    }

    private fun pickBestRole(stringRole: String?, isPresident: Boolean): Role =
        when {
            stringRole.equals("CLUB", ignoreCase = true) -> Role.CLUB
            stringRole.equals("ADMIN", ignoreCase = true) -> Role.ADMIN
            stringRole.equals("TEACHER", ignoreCase = true) -> Role.TEACHER
            stringRole.equals("PARENT", ignoreCase = true) -> Role.PARENT
            isPresident -> Role.PRESIDENT
            else -> Role.STUDENT
        }
}
