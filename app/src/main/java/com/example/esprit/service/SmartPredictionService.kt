package com.example.esprit.service

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.esprit.model.DocumentRequestItem
import kotlinx.coroutines.runBlocking
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Enhanced Smart Prediction Service with AI integration
 * Falls back to heuristics if AI is unavailable
 */
@Singleton
class SmartPredictionService @Inject constructor(
    private val geminiService: GeminiPredictionService
) {

    // Mock average processing times in hours (fallback)
    private val averageTimes = mapOf(
        "attestation" to 2.0,
        "attestation_inscription" to 2.0,
        "relevé" to 24.0,
        "releve_notes" to 24.0,
        "convention" to 48.0,
        "convention_stage" to 48.0
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun getEstimatedProcessingTime(type: String): String {
        return try {
            // Try AI prediction first
            runBlocking {
                val prediction = geminiService.getPredictedProcessingTime(type)
                prediction.estimatedText
            }
        } catch (e: Exception) {
            // Fallback to heuristic
            getHeuristicEstimate(type)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getEstimatedProcessingTimeWithDetails(
        type: String,
        recentRequests: List<DocumentRequestItem> = emptyList()
    ): PredictionResult {
        return try {
            geminiService.getPredictedProcessingTime(type, recentRequests)
        } catch (e: Exception) {
            // Fallback
            val hours = (averageTimes[type.lowercase()] ?: 24.0) * getLoadMultiplier()
            PredictionResult(
                estimatedHours = hours,
                estimatedText = formatDuration(hours),
                confidence = "medium",
                explanation = "Estimation basée sur les temps moyens",
                tip = "Soumettez votre demande tôt dans la journée"
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getSmartTip(): SmartMessage {
        return try {
            runBlocking {
                geminiService.getSmartTip()
            }
        } catch (e: Exception) {
            getHeuristicTip()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getSmartTipWithContext(
        documentType: String?,
        userHistory: List<DocumentRequestItem> = emptyList()
    ): SmartMessage {
        return try {
            geminiService.getSmartTip(documentType, userHistory)
        } catch (e: Exception) {
            getHeuristicTip()
        }
    }

    // Heuristic fallback methods
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getHeuristicEstimate(type: String): String {
        val baseTime = averageTimes[type.lowercase()] ?: 24.0
        val multiplier = getLoadMultiplier()
        val estimatedHours = baseTime * multiplier
        return formatDuration(estimatedHours)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getHeuristicTip(): SmartMessage {
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
                    "Votre demande sera traitée dès demain matin à 8h."
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getLoadMultiplier(): Double {
        val now = LocalDateTime.now()
        // Simulate busy times
        if (now.dayOfWeek == DayOfWeek.FRIDAY && now.hour > 12) return 2.5
        if (now.hour < 8 || now.hour > 18) return 1.5
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

