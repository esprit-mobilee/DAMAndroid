package com.example.esprit.ui.club

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.club.ClubHomeDto
import com.example.esprit.repository.ClubRepository
import com.example.esprit.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.create
import javax.inject.Inject
import com.example.esprit.repository.NotificationsRepository

data class ClubHomeUiState(
    val loading: Boolean = false,
    val club: ClubHomeDto? = null,
    val unreadCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class ClubHomeViewModel @Inject constructor(
    private val repo: ClubRepository,
    private val notificationsRepo: NotificationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClubHomeUiState(loading = true))
    val uiState: StateFlow<ClubHomeUiState> = _uiState

    init {
        Log.d("ClubHomeViewModel", "ViewModel initialized")
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            Log.d("ClubHomeViewModel", "Starting refresh...")
            _uiState.value = _uiState.value.copy(loading = true, error = null)

            try {
                val res = repo.home()
                Log.d("ClubHomeViewModel", "Repository result: $res")

                when (res) {
                    is UiState.Success<*> -> {
                        val data = res.data as? ClubHomeDto
                        Log.d("ClubHomeViewModel", "Success! Club: ${data?.name}")
                        _uiState.value = ClubHomeUiState(club = data, loading = false)
                        
                        data?.let { clubData ->
                            when (val unreadRes = notificationsRepo.getUnreadCount(clubData.id)) {
                                is UiState.Success<*> -> {
                                    _uiState.value = _uiState.value.copy(
                                        unreadCount = (unreadRes.data as? Map<String, Int>)?.get("unreadCount") ?: 0
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                    is UiState.Error -> {
                        Log.e("ClubHomeViewModel", "Error: ${res.message}")
                        _uiState.value = ClubHomeUiState(error = res.message, loading = false)
                    }
                    UiState.Loading -> {
                        Log.d("ClubHomeViewModel", "Still loading...")
                        _uiState.value = ClubHomeUiState(loading = true)
                    }
                }
            } catch (e: Exception) {
                Log.e("ClubHomeViewModel", "Exception in refresh", e)
                _uiState.value = ClubHomeUiState(
                    error = "Exception: ${e.message}",
                    loading = false
                )
            }
        }
    }

    suspend fun updateProfileImage(
        clubId: String,
        imageUri: android.net.Uri,
        context: android.content.Context
    ): UiState<ClubHomeDto> {
        return try {
            val imagePart = uriToMultipartBody(imageUri, context, "profileImage")
            val result = repo.updateProfileImage(clubId, imagePart)
            
            if (result is UiState.Success<*>) {
                _uiState.value = _uiState.value.copy(club = result.data as? ClubHomeDto)
            }
            
            result
        } catch (e: Exception) {
            Log.e("ClubHomeViewModel", "Error updating profile image", e)
            UiState.Error(e.message ?: "Failed to update profile image")
        }
    }

    suspend fun updateCoverImage(
        clubId: String,
        imageUri: android.net.Uri,
        context: android.content.Context
    ): UiState<ClubHomeDto> {
        return try {
            val imagePart = uriToMultipartBody(imageUri, context, "coverImage")
            val result = repo.updateCoverImage(clubId, imagePart)
            
            if (result is UiState.Success<*>) {
                _uiState.value = _uiState.value.copy(club = result.data as? ClubHomeDto)
            }
            
            result
        } catch (e: Exception) {
            Log.e("ClubHomeViewModel", "Error updating cover image", e)
            UiState.Error(e.message ?: "Failed to update cover image")
        }
    }

    private fun uriToMultipartBody(
        uri: android.net.Uri,
        context: android.content.Context,
        partName: String
    ): okhttp3.MultipartBody.Part {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open image")
        
        val bytes = inputStream.readBytes()
        inputStream.close()
        
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val requestBody = okhttp3.RequestBody.Companion.create(
            mimeType.toMediaTypeOrNull(),
            bytes
        )
        
        val fileName = "image_${System.currentTimeMillis()}.jpg"
        return okhttp3.MultipartBody.Part.createFormData(partName, fileName, requestBody)
    }
}