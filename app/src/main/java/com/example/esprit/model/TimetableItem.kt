package com.example.esprit.model

data class TimetableItem(
    val id: String,
    val course: String,
    val room: String,
    val start: String,
    val end: String,
    val teacher: String
)