package com.example.esprit.util

object BadWordFilter {
    private val BAD_WORDS = listOf(
        // English
        "fuck", "shit", "bitch", "asshole", "dick", "pussy", "whore", "slut", "bastard",
        
        // French
        "merde", "putain", "connard", "salaud", "salope", "batard", "foutre", "bite", "conne",
        
        // Tunisian / Arabic Dialect
        "bhim", "bhim", "bhima", // Donkey/Stupid
        "kahba", "9ahba", "9a7ba", // Whore
        "zebi", "zob", "3asba","iziibi", // Male organ
        "nayek", "mnayek", // Fucker/Fucked up
        "tahan", "t7an", // Rat/Traitor
        "sorm", "sormomok", // Ass
        "msatek", "msetek"// Stupid
        
    )

    fun sanitize(text: String): String {
        var sanitizedText = text
        // Sort by length descending to replace longer phrases first (if we had phrases)
        // For words, it prevents "scunthorpe" problem if we matched partials, 
        // but here we will try to match whole words or use a simple replacement.
        
        // Simple case-insensitive replacement
        BAD_WORDS.forEach { word ->
            val pattern = "(?i)\\b$word\\b".toRegex()
            if (pattern.containsMatchIn(sanitizedText)) {
                val replacement = "*".repeat(word.length)
                sanitizedText = pattern.replace(sanitizedText, replacement)
            }
        }
        return sanitizedText
    }
}
