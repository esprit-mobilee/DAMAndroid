package com.example.esprit.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {
    /**
     * Formate une date ISO (ex: "2025-01-15T10:30:00.000Z") en format français lisible
     * Exemples de sortie: "15 janvier 2025" ou "15/01/2025 à 10:30"
     */
    fun formatDate(dateString: String, includeTime: Boolean = false): String {
        return try {
            // Formats ISO courants
            val isoFormats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd"
            )
            
            var parsedDate: Date? = null
            for (format in isoFormats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.US)
                    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    parsedDate = sdf.parse(dateString)
                    if (parsedDate != null) break
                } catch (e: Exception) {
                    // Essayer le format suivant
                }
            }
            
            if (parsedDate == null) {
                // Si aucun format ne fonctionne, retourner la date brute
                return dateString.split("T")[0]
            }
            
            // Formater en français
            val outputFormat = if (includeTime) {
                SimpleDateFormat("dd MMMM yyyy 'à' HH:mm", Locale.FRENCH)
            } else {
                SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
            }
            
            outputFormat.format(parsedDate)
        } catch (e: Exception) {
            // En cas d'erreur, retourner au moins la partie date
            try {
                dateString.split("T")[0]
            } catch (ex: Exception) {
                dateString
            }
        }
    }
    
    /**
     * Formate une date en format court (ex: "15/01/2025")
     */
    fun formatDateShort(dateString: String): String {
        return try {
            val isoFormats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd"
            )
            
            var parsedDate: Date? = null
            for (format in isoFormats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.US)
                    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    parsedDate = sdf.parse(dateString)
                    if (parsedDate != null) break
                } catch (e: Exception) {
                    // Essayer le format suivant
                }
            }
            
            if (parsedDate == null) {
                return dateString.split("T")[0]
            }
            
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH)
            outputFormat.format(parsedDate)
        } catch (e: Exception) {
            try {
                dateString.split("T")[0]
            } catch (ex: Exception) {
                dateString
            }
        }
    }
}

