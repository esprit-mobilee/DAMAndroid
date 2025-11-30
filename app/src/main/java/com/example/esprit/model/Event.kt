package com.example.esprit.model

import com.google.gson.annotations.SerializedName

/**
 * Event as sent by NestJS (Mongoose).
 * Sometimes `organizerId` is just the ObjectId string,
 * sometimes it's a populated user object → so we make it `Any?`.
 */
data class Event(
    @SerializedName("_id")
    val id: String? = null,

    val title: String? = null,
    val description: String? = null,

    // ISO string from backend
    val date: String? = null,

    val location: String? = null,

    // can be "690e..." OR { _id: "690e...", firstName: ... }
    @SerializedName("organizerId")
    val organizer: Any? = null,

    val category: String? = null,

    // 👇 URL relative renvoyée par le backend (ex: "/uploads/events/xxx.jpg")
    val imageUrl: String? = null
)
