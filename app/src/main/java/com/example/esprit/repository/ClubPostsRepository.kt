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

    suspend fun getPost(id: String): UiState<ClubPostDto> =
        safeCall { api.getClubPost(id) }

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

    suspend fun updateComment(postId: String, commentId: String, content: String): UiState<ClubPostDto> = safeCall {
        api.updateComment(postId, commentId, mapOf("content" to content))
    }

    suspend fun deleteComment(postId: String, commentId: String): UiState<ClubPostDto> = safeCall {
        api.deleteComment(postId, commentId)
    }

    suspend fun reactToComment(postId: String, commentId: String, emoji: String): UiState<ClubPostDto> = safeCall {
        api.reactToComment(postId, commentId, mapOf("emoji" to emoji))
    }

    suspend fun replyToComment(postId: String, commentId: String, content: String): UiState<ClubPostDto> = safeCall {
        api.replyToComment(postId, commentId, mapOf("content" to content))
    }
}
