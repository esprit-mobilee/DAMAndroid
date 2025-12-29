package com.example.esprit.data.local.entities


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val senderId: String,
    val audience: String,
    val createdAt: String
)
