package com.example.esprit.model.club

import com.example.esprit.model.Location
import com.google.gson.annotations.SerializedName

data class ClubHomeDto(
    @SerializedName("id", alternate = ["_id"])
    val id: String,
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val coverImageUrl: String? = null,
    val totalEvents: Int = 0,
    val totalMembers: Int = 0,
    val tags: List<String>? = null,
    val joinEnabled: Boolean = true,
    val joinFormQuestions: List<String>? = emptyList(),
    val membershipStatus: String? = null, // "MEMBER", "PENDING", "NOT_MEMBER", "PRESIDENT"
    val members: List<ClubMemberDto>? = emptyList(),
    val latestPost: ClubPostDto? = null
)

data class ClubEventDto(
    @SerializedName("id", alternate = ["_id"])
    val id: String,
    val title: String,
    val date: String? = null,
    val location: Location? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val category: String? = null,
    val registrationOpen: Boolean = false,
    val responsibleId: String? = null,
    val registrations: List<EventRegistrationDto> = emptyList()
)

data class ClubPostDto(
    @SerializedName("id", alternate = ["_id"])
    val id: String,
    @SerializedName("text")
    val content: String,
    val imageUrl: String? = null,
    val createdAt: String,
    val likes: List<String>? = emptyList(),
    val dislikes: List<String>? = emptyList(),
    val comments: List<CommentDto> = emptyList(),
    val isLiked: Boolean = false,
    val isDisliked: Boolean = false,
    val clubName: String? = null,
    val clubImage: String? = null
)

data class CommentDto(
    @SerializedName("id", alternate = ["_id"])
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String? = null,
    val content: String,
    val createdAt: String
)

data class EventRegistrationDto(
    val userId: String? = null,
    val name: String? = null,
    val identifiant: String? = null,
    val email: String? = null,
    val message: String? = null,
    val createdAt: String? = null
)

data class ClubMemberDto(
    @SerializedName("id", alternate = ["_id"])
    val id: String,
    val name: String,
    val identifiant: String,
    val role: String,
    val avatar: String? = null
)

data class JoinRequestDto(
    @SerializedName("id", alternate = ["_id"])
    val id: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val answers: List<JoinAnswerDto> = emptyList(),
    val status: String,
    val createdAt: String
)

data class JoinAnswerDto(
    val question: String,
    val answer: String
)
