package com.example.esprit.service

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.esprit.BuildConfig
import com.example.esprit.model.DocumentRequestItem
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI-Powered Prediction Service using Gemini
 * Provides intelligent processing time predictions and contextual tips
 */
@Singleton
class GeminiPredictionService @Inject constructor() {

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 200
            }
        )
    }

    /**
     * Get AI-powered processing time prediction
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getPredictedProcessingTime(
        documentType: String,
        recentRequests: List<DocumentRequestItem> = emptyList()
    ): PredictionResult {
        return withContext(Dispatchers.IO) {
            try {
                val context = buildContextPrompt(documentType, recentRequests)
                val prompt = """
                    Tu es un assistant intelligent pour ESPRIT (école d'ingénieurs en Tunisie).
                    
                    Contexte actuel :
                    $context
                    
                    Tâche : Estime le TEMPS DE TRAITEMENT pour une demande de "$documentType".
                    
                    Réponds UNIQUEMENT au format JSON suivant (sans code markdown) :
                    {
                        "estimatedHours": <nombre_heures_decimal>,
                        "estimatedText": "<texte_duree_lisible>",
                        "confidence": "<high|medium|low>",
                        "explanation": "<explication_courte_50_mots_max>",
                        "tip": "<conseil_personnalise_40_mots_max>"
                    }
                    
                    Exemples de estimatedText : "2-4h", "1-2 jours", "30 min - 1h"
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val responseText = response.text?.trim() ?: ""
                
                parsePredictionResponse(responseText, documentType)
            } catch (e: Exception) {
                // Fallback to heuristic prediction
                getFallbackPrediction(documentType)
            }
        }
    }

    /**
     * Get AI-powered smart tip based on current context
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getSmartTip(
        documentType: String? = null,
        userHistory: List<DocumentRequestItem> = emptyList()
    ): SmartMessage {
        return withContext(Dispatchers.IO) {
            try {
                val now = LocalDateTime.now()
                val timeContext = buildTimeContext(now)
                val historyContext = buildHistoryContext(userHistory)

                val prompt = """
                    Tu es un assistant ESPRIT. Donne un conseil intelligent à un étudiant.
                    
                    Contexte :
                    - Type de document : ${documentType ?: "non spécifié"}
                    - Moment : $timeContext
                    - Historique : $historyContext
                    
                    Réponds en JSON (sans code markdown) :
                    {
                        "icon": "<emoji_pertinent>",
                        "title": "<titre_court_4_mots_max>",
                        "message": "<conseil_personnalise_30_mots_max>"
                    }
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val responseText = response.text?.trim() ?: ""
                
                parseSmartTipResponse(responseText)
            } catch (e: Exception) {
                // Fallback to heuristic tip
                getFallbackSmartTip()
            }
        }
    }

    /**
     * Build context prompt for AI
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildContextPrompt(
        documentType: String,
        recentRequests: List<DocumentRequestItem>
    ): String {
        val now = LocalDateTime.now()
        val dayOfWeek = now.dayOfWeek.name
        val hour = now.hour
        val dateStr = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

        val pendingCount = recentRequests.count { it.status == "pending" }
        val avgProcessingTime = calculateAverageProcessingTime(recentRequests)

        return """
            Date/Heure : $dateStr ($dayOfWeek à ${hour}h)
            Type de document demandé : $documentType
            Demandes en attente dans le système : $pendingCount
            Temps moyen récent : $avgProcessingTime
            Période académique : Janvier 2026 (après vacances)
        """.trimIndent()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildTimeContext(now: LocalDateTime): String {
        val day = now.dayOfWeek
        val hour = now.hour
        return when {
            day == DayOfWeek.FRIDAY && hour > 14 -> "Vendredi après-midi (avant weekend)"
            hour < 8 -> "Tôt le matin (hors heures bureau)"
            hour > 18 -> "Soirée (hors heures bureau)"
            hour in 10..12 -> "Milieu de matinée (période active)"
            else -> "Heures de bureau normales"
        }
    }

    private fun buildHistoryContext(history: List<DocumentRequestItem>): String {
        if (history.isEmpty()) return "Pas d'historique"
        val recent = history.take(3)
        val approved = recent.count { it.status == "approved" }
        return "Dernières demandes: ${recent.size} (${approved} approuvées)"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateAverageProcessingTime(requests: List<DocumentRequestItem>): String {
        val completed = requests.filter { it.status in listOf("approved", "rejected") }
        if (completed.isEmpty()) return "Non disponible"
        // Simplified calculation - in real scenario, calculate from createdAt to updatedAt
        return "2-3 jours"
    }

    /**
     * Parse AI response to PredictionResult
     */
    private fun parsePredictionResponse(responseText: String, documentType: String): PredictionResult {
        return try {
            // Remove markdown code blocks if present
            val cleanJson = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            // Simple JSON parsing (in production, use Gson or kotlinx.serialization)
            val estimatedHours = extractJsonValue(cleanJson, "estimatedHours")?.toDoubleOrNull() ?: 24.0
            val estimatedText = extractJsonValue(cleanJson, "estimatedText") ?: formatDuration(estimatedHours)
            val explanation = extractJsonValue(cleanJson, "explanation") ?: "Estimation basée sur l'historique"
            val tip = extractJsonValue(cleanJson, "tip") ?: "Vérifiez régulièrement le statut de votre demande"
            val confidence = extractJsonValue(cleanJson, "confidence") ?: "medium"

            PredictionResult(
                estimatedHours = estimatedHours,
                estimatedText = estimatedText,
                confidence = confidence,
                explanation = explanation,
                tip = tip
            )
        } catch (e: Exception) {
            getFallbackPrediction(documentType)
        }
    }

    private fun parseSmartTipResponse(responseText: String): SmartMessage {
        return try {
            val cleanJson = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val icon = extractJsonValue(cleanJson, "icon") ?: "💡"
            val title = extractJsonValue(cleanJson, "title") ?: "Info"
            val message = extractJsonValue(cleanJson, "message") ?: "Votre demande sera traitée rapidement"

            SmartMessage(
                title = "$icon $title",
                body = message
            )
        } catch (e: Exception) {
            getFallbackSmartTip()
        }
    }

    /**
     * Simple JSON value extractor
     */
    private fun extractJsonValue(json: String, key: String): String? {
        val regex = """"$key"\s*:\s*"([^"]*)"""".toRegex()
        val match = regex.find(json)
        if (match != null) return match.groupValues[1]
        
        // Try numeric values
        val numRegex = """"$key"\s*:\s*([0-9.]+)""".toRegex()
        val numMatch = numRegex.find(json)
        return numMatch?.groupValues?.get(1)
    }

    /**
     * Fallback predictions when AI is unavailable
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getFallbackPrediction(documentType: String): PredictionResult {
        val baseHours = when (documentType.lowercase()) {
            "attestation", "attestation_inscription" -> 2.0
            "relevé", "releve_notes" -> 24.0
            "convention", "convention_stage" -> 48.0
            else -> 24.0
        }

        val multiplier = getLoadMultiplier()
        val estimatedHours = baseHours * multiplier

        return PredictionResult(
            estimatedHours = estimatedHours,
            estimatedText = formatDuration(estimatedHours),
            confidence = "medium",
            explanation = "Estimation basée sur les temps moyens historiques",
            tip = "Soumettez votre demande le matin pour un traitement plus rapide"
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getFallbackSmartTip(): SmartMessage {
        val now = LocalDateTime.now()
        return when {
            now.dayOfWeek == DayOfWeek.FRIDAY && now.hour > 14 -> SmartMessage(
                "⚠️ Attention Weekend",
                "Les demandes du vendredi après-midi sont traitées lundi"
            )
            now.hour < 10 -> SmartMessage(
                "⚡ Bon moment",
                "Les matinées sont idéales pour soumettre une demande"
            )
            else -> SmartMessage(
                "📊 Info",
                "Votre demande sera traitée dans les meilleurs délais"
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getLoadMultiplier(): Double {
        val now = LocalDateTime.now()
        if (now.dayOfWeek == DayOfWeek.FRIDAY && now.hour > 12) return 2.5
        if (now.hour < 8 || now.hour > 18) return 1.5
        return 1.0
    }

    private fun formatDuration(hours: Double): String {
        if (hours < 1) {
            val minutes = (hours * 60).toInt()
            return "$minutes min"
        }
        val h = hours.toInt()
        return if (h > 24) {
            val days = h / 24
            "$days jour${if (days > 1) "s" else ""}"
        } else {
            "$h h"
        }
    }
}

/**
 * Result of AI prediction
 */
data class PredictionResult(
    val estimatedHours: Double,
    val estimatedText: String,
    val confidence: String, // high, medium, low
    val explanation: String,
    val tip: String
)
