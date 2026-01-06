package com.example.esprit.model

import com.google.gson.annotations.SerializedName

data class Favorite(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("internshipId")
    val internshipId: InternshipOffer? = null,
    val createdAt: String? = null
)

