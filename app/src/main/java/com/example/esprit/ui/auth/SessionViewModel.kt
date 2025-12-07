package com.example.esprit.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.util.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val dataStore: DataStoreManager
) : ViewModel() {

    fun logout(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                dataStore.clearToken()
            } finally {
                onComplete()
            }
        }
    }
}
