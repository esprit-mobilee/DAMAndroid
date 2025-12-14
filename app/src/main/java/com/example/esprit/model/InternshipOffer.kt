package com.example.esprit.model

import com.google.gson.annotations.SerializedName

data class InternshipOffer(
    @SerializedName("_id")
    val id: String? = null,
    val title: String,
    val company: String,
    val description: String,
    val location: Location? = null,
    val duration: Int,
    val salary: Int? = null,
    val logoUrl: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)
