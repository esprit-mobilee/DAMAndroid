package com.example.esprit.model.chat

import com.google.gson.annotations.SerializedName

data class MessageDto(
    @SerializedName("_id") val id: String,
    val clubId: String? = null,
    val recipientId: String? = null,
    val senderId: ChatUserDto,
    val content: String,
    val type: String = "TEXT", // TEXT, IMAGE, VOICE, GIF
    val attachmentUrl: String? = null,
    val createdAt: String,
    val reactions: List<ReactionDto> = emptyList(),
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val replyTo: MessageDto? = null // Recursive type, or use a simplified ReplyDto if necessary. Gson handles basics.
)

data class ChatUserDto(
    @SerializedName("_id") val id: String,
    val firstName: String,
    val lastName: String,
    val imageUrl: String? = null
)

data class ReactionDto(
    val userId: String,
    val emoji: String
)

data class TypingEvent(
    val clubId: String,
    val userId: String,
    val isTyping: Boolean
)
