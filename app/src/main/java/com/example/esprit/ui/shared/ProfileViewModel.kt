package com.example.esprit.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.User
import com.example.esprit.repository.UserRepository
import com.example.esprit.util.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: UserRepository,
    private val store: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState


    // --------------------------------------------------------------
    //   LOAD USER  (/auth/me)
    // --------------------------------------------------------------
    fun loadMe() {
        viewModelScope.launch {
            try {
                _uiState.value = ProfileUiState(isLoading = true)

                val token = store.getToken()      // ← FIX : We read token from DataStore
                val me = repo.getMe(token)

                _uiState.value = ProfileUiState(user = me)

            } catch (e: Exception) {
                _uiState.value = ProfileUiState(error = e.message)
            }
        }
    }


    // --------------------------------------------------------------
    //   CHANGE PASSWORD  (/utilisateurs/me/password)
    // --------------------------------------------------------------
    fun changePassword(oldPwd: String, newPwd: String) {
        viewModelScope.launch {
            try {
                val token = store.getToken()      // ← OBLIGATOIRE
                repo.changeMyPassword(token, oldPwd, newPwd)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
