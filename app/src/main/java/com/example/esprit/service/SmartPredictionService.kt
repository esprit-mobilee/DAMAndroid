package com.example.esprit.service

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class SmartPredictionService @Inject constructor() {

    // Mock average processing times in hours
    private val averageTimes = mapOf(
        "ATTESTATION_INSCRIPTION" to 2.0,
        "RELEVE_NOTES" to 24.0,
        "ATTESTATION_PRESENCE" to 4.0,
        "CONVENTION_STAGE" to 48.0
    )

    fun getEstimatedProcessingTime(type: String): String {
        val baseTime = averageTimes[type] ?: 24.0
        val multiplier = getLoadMultiplier()
        val estimatedHours = baseTime * multiplier
        
        return formatDuration(estimatedHours)
    }

    fun getSmartTip(): SmartMessage {
        val now = LocalDateTime.now()
        val day = now.dayOfWeek
        val time = now.toLocalTime()

        return when {
            day == DayOfWeek.FRIDAY && time.isAfter(LocalTime.of(14, 0)) -> {
                SmartMessage(
                    "⚠️ Trafic élevé",
                    "Les demandes faites le vendredi après-midi peuvent prendre plus de temps (weekend)."
                )
            }
            time.isBefore(LocalTime.of(10, 0)) -> {
                SmartMessage(
                    "⚡ Moment idéal",
                    "Les admins sont généralement très réactifs le matin."
                )
            }
            time.isAfter(LocalTime.of(18, 0)) -> {
                SmartMessage(
                    "🌙 Hors horaires",
                    "Votre demande sera traitée dès demain matin 8h."
                )
            }
            else -> {
                SmartMessage(
                    "📊 Info",
                    "Le temps de traitement moyen est actuellement normal."
                )
            }
        }
    }

    private fun getLoadMultiplier(): Double {
        val now = LocalDateTime.now()
        // Simulate busy times (e.g., Friday afternoon or exam periods)
        if (now.dayOfWeek == DayOfWeek.FRIDAY && now.hour > 12) return 2.5
        if (now.hour < 8 || now.hour > 18) return 1.5 // Overnight markup
        return 1.0
    }

    private fun formatDuration(hours: Double): String {
        if (hours < 1) {
            val minutes = (hours * 60).roundToInt()
            return "$minutes min"
        }
        val h = hours.toInt()
        return if (h > 24) {
            "${h / 24} jours"
        } else {
            "$h h"
        }
    }
}

data class SmartMessage(
    val title: String,
    val body: String
)
