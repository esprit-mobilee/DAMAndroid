package com.example.esprit.repository

import com.example.esprit.model.club.ClubPostDto
import com.example.esprit.network.ApiService
import com.example.esprit.network.safeCall
import com.example.esprit.util.UiState
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class ClubPostsRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun list(clubId: String): UiState<List<ClubPostDto>> =
        safeCall { api.getClubPosts(clubId) }

    suspend fun create(
        clubId: String,
        content: RequestBody,
        image: MultipartBody.Part?
    ): UiState<ClubPostDto> =
        safeCall { api.createClubPost(clubId, content, image) }

    suspend fun update(id: String, content: RequestBody, image: MultipartBody.Part?): UiState<ClubPostDto> =
        safeCall { api.updateClubPost(id, content, image) }

    suspend fun delete(id: String): UiState<Unit> = safeCall { api.deleteClubPost(id) }

    suspend fun like(id: String): UiState<ClubPostDto> = safeCall { api.likePost(id) }

    suspend fun dislike(id: String): UiState<ClubPostDto> = safeCall { api.dislikePost(id) }

    suspend fun comment(id: String, content: String): UiState<ClubPostDto> = safeCall {
        api.commentPost(id, mapOf("content" to content))
    }
}
