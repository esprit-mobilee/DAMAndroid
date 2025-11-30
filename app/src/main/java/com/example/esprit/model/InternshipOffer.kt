package com.example.esprit.model

import com.google.gson.annotations.SerializedName

data class InternshipOffer(
    @SerializedName("_id")
    val id: String? = null,
    val title: String,
    val company: String,
    val description: String,
    val location: String? = null,
    val duration: Int,
    val salary: Int? = null,
    val logoUrl: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    // New fields
    val tags: List<String>? = null,
    val internshipType: String? = null,
    val procedure: String? = null,
    val interviewProcess: String? = null,
    val startDate: String? = null,
    val interviewDetails: String? = null,
    val positionsAvailable: Int? = null,
    val applicationsCount: Int? = null,
)
