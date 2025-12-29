package com.example.esprit.model


import com.google.gson.annotations.SerializedName

data class AnnouncementDto(
    @SerializedName("_id") val id: String,
    val title: String,
    val content: String,
    val audience: String,
    val senderId: String,
    val createdAt: String? = null    //  ← SAFE !
)
