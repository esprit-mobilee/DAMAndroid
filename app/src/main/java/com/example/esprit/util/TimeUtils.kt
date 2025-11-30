package com.example.esprit.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatTime(iso: String): String {
    return try {
        val instant = Instant.parse(iso)
        val local = instant.atZone(ZoneId.systemDefault())
        DateTimeFormatter.ofPattern("HH:mm").format(local)
    } catch (e: Exception) {
        ""
    }
}
