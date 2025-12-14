package com.example.esprit.repository

import com.example.esprit.model.club.ClubHomeDto
import com.example.esprit.network.ApiService
import com.example.esprit.network.safeCall
import com.example.esprit.util.UiState
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ClubRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun home(): UiState<ClubHomeDto> = safeCall {
        val user = api.getMe()
        val clubId = user.presidentOf ?: user.club ?: throw Exception("User is not associated with any club")
        api.getClub(clubId)
    }

    suspend fun updateProfile(
        clubId: String,
        name: String,
        description: String?,
        tags: String?,
        profileImage: MultipartBody.Part?,
        coverImage: MultipartBody.Part?
    ): UiState<ClubHomeDto> = safeCall {
        api.updateClubProfile(
            id = clubId,
            name = name.toPlainText(),
            description = description.toPlainText(),
            tags = tags.toPlainText(),
            profileImage = profileImage,
            coverImage = coverImage
        )
    }

    suspend fun updateProfileImage(
        clubId: String,
        profileImage: MultipartBody.Part
    ): UiState<ClubHomeDto> = safeCall {
        api.updateClubProfile(
            id = clubId,
            name = null,
            description = null,
            tags = null,
            profileImage = profileImage,
            coverImage = null
        )
    }

    suspend fun updateCoverImage(
        clubId: String,
        coverImage: MultipartBody.Part
    ): UiState<ClubHomeDto> = safeCall {
        api.updateClubProfile(
            id = clubId,
            name = null,
            description = null,
            tags = null,
            profileImage = null,
            coverImage = coverImage
        )
    }

    suspend fun toggleJoinEnabled(clubId: String): UiState<ClubHomeDto> = safeCall {
        api.toggleJoinEnabled(clubId)
    }

    private fun String?.toPlainText(): RequestBody? =
        this?.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())

    suspend fun updateSettings(
        clubId: String,
        joinEnabled: Boolean? = null,
        joinFormQuestions: List<String>? = null
    ): UiState<ClubHomeDto> = safeCall {
        val body = mutableMapOf<String, Any>()
        if (joinEnabled != null) body["joinEnabled"] = joinEnabled
        if (joinFormQuestions != null) body["joinFormQuestions"] = joinFormQuestions
        api.updateClubSettings(clubId, body)
    }

    suspend fun getPendingRequests(clubId: String): UiState<List<com.example.esprit.model.club.JoinRequestDto>> = safeCall {
        api.getPendingRequests(clubId)
    }

    suspend fun approveRequest(requestId: String): UiState<Unit> = safeCall {
        api.approveJoinRequest(requestId)
        Unit
    }

    suspend fun rejectRequest(requestId: String): UiState<Unit> = safeCall {
        api.rejectJoinRequest(requestId)
        Unit
    }
}

