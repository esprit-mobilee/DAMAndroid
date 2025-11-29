package com.example.esprit.model

fun MessageDto.toDomain(currentUserId: String): Message =
    Message(
        id = id.orEmpty(),
        content = content.orEmpty(),
        senderId = senderId.orEmpty(),
        receiverId = receiverId.orEmpty(),
        timestamp = (createdAt ?: updatedAt ?: "").orEmpty(),
        isMine = senderId == currentUserId
    )
