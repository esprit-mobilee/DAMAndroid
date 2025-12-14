package com.example.esprit.model

data class AiProfile(
    val summary: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val skills: List<String> = emptyList(),
    val experience: List<String> = emptyList(),
    val education: List<String> = emptyList()
)
