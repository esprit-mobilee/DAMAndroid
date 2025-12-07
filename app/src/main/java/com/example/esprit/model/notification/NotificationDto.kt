package com.example.esprit.model.notification

import com.google.gson.annotations.SerializedName

data class NotificationDto(
    @SerializedName("id", alternate = ["_id"])
    val id: String,
    val clubId: String,
    val type: String,
    val userId: NotificationUserDto,
    val message: String,
    val read: Boolean,
    val createdAt: String
)

data class NotificationUserDto(
    @SerializedName("id", alternate = ["_id"])
    val id: String,
    val firstName: String,
    val lastName: String,
    val identifiant: String,
    val avatar: String? = null
)
