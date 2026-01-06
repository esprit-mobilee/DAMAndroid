package com.example.esprit.model.chat

import com.google.gson.annotations.SerializedName

data class UnifiedConversationDto(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String, // "private" or "group"
    @SerializedName("name") val name: String,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("lastMessage") val lastMessage: String,
    @SerializedName("lastMessageTime") val lastMessageTime: String,
    @SerializedName("role") val role: String?,
    @SerializedName("userId") val userId: String? // For private chats, to know who it is
)
