package com.example.esprit.model.chat

import com.example.esprit.model.User
import com.google.gson.annotations.SerializedName

data class ConversationDto(
    @SerializedName("_id") val partnerId: String, // The group _id is the partner ID logic from aggregation
    val lastMessage: MessageDto,
    val partner: User // We'll map the flattened partner fields to a User-like object or specific DTO
)
