package com.example.esprit.model

fun MessageDto.toDomain(currentUserId: String): Message =
    Message(
        id = id,
        content = content,
        senderId = senderId.orEmpty(),
        receiverId = receiverId.orEmpty(),
        timestamp = createdAt ?: updatedAt ?: "",
        isMine = senderId == currentUserId,
        reactions = reactions.map {
            Reaction(
                userId = it.userId,
                emoji = it.emoji
            )
        }
    )
