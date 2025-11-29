package com.example.esprit.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Table locale des messages
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,          // id Mongo / backend
    val content: String,
    val senderId: String,
    val receiverId: String,
    val timestamp: String,   // date/heure au format String
    val isMine: Boolean      // pour afficher à droite/gauche
)
