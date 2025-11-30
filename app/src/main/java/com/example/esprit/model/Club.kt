package com.example.esprit.model

import com.google.gson.annotations.SerializedName

/**
 * Club as sent by NestJS.
 * president can be populated (object) or just an id, so we keep it as Any?.
 */
data class Club(
    @SerializedName("_id")
    val id: String? = null,

    val name: String? = null,
    val description: String? = null,

    // can be an ObjectId string OR a populated user
    val president: Any? = null,

    val members: List<Any>? = emptyList(),
    val tags: List<String>? = emptyList(),
    // URL (relative) of the logo, e.g. "/uploads/clubs/xxx.jpg"
    val imageUrl: String? = null,

    val foundedAt: String? = null,

    @SerializedName("isActive")
    val isActive: Boolean? = null
)
