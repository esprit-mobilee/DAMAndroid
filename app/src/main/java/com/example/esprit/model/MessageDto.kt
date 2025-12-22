package com.example.esprit.model

import com.google.gson.annotations.SerializedName

data class MessageDto(
    @SerializedName("_id") val id: String,
    @SerializedName("senderId") val senderId: String?,
    @SerializedName("receiverId") val receiverId: String?,
    @SerializedName("content") val content: String,
    @SerializedName("type") val type: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("reactions") val reactions: List<ReactionDto> = emptyList()
)
