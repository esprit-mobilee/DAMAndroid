package com.example.esprit.model.chat

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonElement

data class MessageDto(
    @SerializedName("_id") val id: String,
    val clubId: String? = null,
    val recipientId: String? = null,
    val senderId: ChatUserDto,
    val content: String,
    @SerializedName("type") val type: String = "TEXT",
    @SerializedName(value = "attachmentUrl", alternate = ["attachment_url", "fileUrl", "file_url"]) val attachmentUrl: String? = null,
    val createdAt: String,
    val reactions: List<ReactionDto> = emptyList(),
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val replyTo: ReplyDto? = null,
    val readBy: List<ReadReceiptDto> = emptyList()
)

data class ReadReceiptDto(
    val userId: String,
    val readAt: String
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

data class ReplyDto(
    @SerializedName("_id") val id: String,
    val content: String,
    val type: String = "TEXT",
    val senderId: JsonElement // Can be String (ID) or Object (ChatUserDto)
)

data class TranslateRequest(
    val messageId: String,
    val targetLang: String
)

data class TranslationResponse(
    val original: String,
    val translated: String,
    val lang: String
)
