package com.example.esprit.model

import com.google.gson.annotations.SerializedName

/**
 * Location data class matching backend structure
 * Used for events and internship offers
 */
data class Location(
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
